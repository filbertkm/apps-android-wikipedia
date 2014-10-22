package org.wikipedia.search;

import org.wikipedia.R;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/** Displays a list of recent searches */
public class RecentSearchesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SearchArticlesFragment searchFragment;
    private View container;
    private ListView recentSearchesList;
    private RecentSearchesAdapter adapter;

    public RecentSearchesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_recent, container, false);
        searchFragment = (SearchArticlesFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        this.container = rootView.findViewById(R.id.recent_searches_container);
        recentSearchesList = (ListView) rootView.findViewById(R.id.recent_searches_list);
        return rootView;
    }

    public void show() {
        container.setVisibility(View.VISIBLE);
    }

    public void hide() {
        container.setVisibility(View.GONE);
    }

    public boolean isShowing() {
        return container.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new RecentSearchesAdapter(getActivity(), null, true);
        recentSearchesList.setAdapter(adapter);

        recentSearchesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecentSearch entry = (RecentSearch) view.getTag();
                searchFragment.switchToTitleSearch(entry.getText());
            }
        });

        getActivity().getSupportLoaderManager().initLoader(0, null, this);
        getActivity().getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onDestroyView() {
        getActivity().getSupportLoaderManager().destroyLoader(0);
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (!isAdded()) {
            return null;
        }
        return new CursorLoader(
                getActivity(),
                RecentSearch.PERSISTANCE_HELPER.getBaseContentURI(),
                null,
                null,
                null,
                "timestamp DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoaderLoader, Cursor cursorLoader) {
        if (!isAdded()) {
            return;
        }
        adapter.swapCursor(cursorLoader);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoaderLoader) {
        adapter.changeCursor(null);
    }

    public void updateList() {
        adapter.notifyDataSetChanged();
    }

    private class RecentSearchesAdapter extends CursorAdapter {
        public RecentSearchesAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return getActivity().getLayoutInflater().inflate(R.layout.item_search_recent, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView textView = (TextView) view.findViewById(R.id.text1);
            RecentSearch entry = getEntry(cursor);
            textView.setText(entry.getText());
            view.setTag(entry);
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {
            return getEntry(cursor).getText();
        }

        public RecentSearch getEntry(Cursor cursor) {
            return RecentSearch.PERSISTANCE_HELPER.fromCursor(cursor);
        }
    }
}