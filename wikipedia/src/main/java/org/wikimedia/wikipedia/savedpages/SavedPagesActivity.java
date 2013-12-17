package org.wikimedia.wikipedia.savedpages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.wikimedia.wikipedia.PageActivity;
import org.wikimedia.wikipedia.R;
import org.wikimedia.wikipedia.WikipediaApp;
import org.wikimedia.wikipedia.history.HistoryEntry;
import org.wikimedia.wikipedia.pageimages.PageImage;

import java.text.DateFormat;
import java.util.Date;

public class SavedPagesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private ListView savedPagesList;
    private SavedPagesAdapter adapter;

    private WikipediaApp app;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (WikipediaApp)getApplicationContext();

        setContentView(R.layout.activity_history);
        savedPagesList = (ListView) findViewById(R.id.history_entry_list);

        adapter = new SavedPagesAdapter(this, null, true);
        savedPagesList.setAdapter(adapter);

        savedPagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SavedPage savedPage = (SavedPage) view.getTag();
                HistoryEntry newEntry = new HistoryEntry(savedPage.getTitle(), HistoryEntry.SOURCE_SAVED_PAGE);

                Intent intent = new Intent();
                intent.setClass(SavedPagesActivity.this, PageActivity.class);
                intent.setAction(PageActivity.ACTION_PAGE_FOR_TITLE);
                intent.putExtra(PageActivity.EXTRA_PAGETITLE, savedPage.getTitle());
                intent.putExtra(PageActivity.EXTRA_HISTORYENTRY, newEntry);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                this,
                Uri.parse(SavedPage.persistanceHelper.getBaseContentURI().toString() + "/" + PageImage.persistanceHelper.getTableName()),
                null,
                null,
                null,
                "timestamp DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoaderLoader, Cursor cursorLoader) {
        adapter.swapCursor(cursorLoader);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoaderLoader) {
        adapter.changeCursor(null);
    }

    private class SavedPagesAdapter extends CursorAdapter {
        public SavedPagesAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return getLayoutInflater().inflate(R.layout.item_saved_page_entry, viewGroup, false);
        }

        private String getDateString(Date date) {
            return DateFormat.getDateInstance().format(date);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.saved_page_title);
            ImageView thumbnail = (ImageView) view.findViewById(R.id.saved_page_thumbnail);
            SavedPage entry = SavedPage.persistanceHelper.fromCursor(cursor);
            title.setText(entry.getTitle().getDisplayText());
            view.setTag(entry);

            Picasso.with(SavedPagesActivity.this)
                    .load(cursor.getString(4))
                    .placeholder(R.drawable.ic_pageimage_placeholder)
                    .error(R.drawable.ic_pageimage_placeholder)
                    .into(thumbnail);

            // Check the previous item, see if the times differe enough
            // If they do, display the section header.
            // Always do it this is the first item.
            String curTime, prevTime = "";
            if (cursor.getPosition() != 0) {
                Cursor prevCursor = (Cursor) getItem(cursor.getPosition() - 1);
                SavedPage prevEntry = SavedPage.persistanceHelper.fromCursor(prevCursor);
                prevTime = getDateString(prevEntry.getTimestamp());
            }
            curTime = getDateString(entry.getTimestamp());
            TextView sectionHeader = (TextView) view.findViewById(R.id.saved_page_section_header_text);
            if (!curTime.equals(prevTime)) {
                sectionHeader.setText(curTime);
                sectionHeader.setVisibility(View.VISIBLE);
            } else {
                sectionHeader.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_saved_pages, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_clear_all_saved_pages:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_title_clear_saved_pages)
                        .setMessage(R.string.dialog_message_clear_saved_pages);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear Saved Pages!
                        app.getPersister(SavedPage.class).deleteAll();
                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Uh, do nothing?
                    }
                });
                builder.create().show();
                return true;
            default:
                throw new RuntimeException("Unknown menu item clicked!");
        }
    }
}