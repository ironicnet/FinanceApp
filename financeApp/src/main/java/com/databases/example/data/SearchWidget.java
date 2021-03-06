package com.databases.example.data;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.databases.example.app.Search;

public class SearchWidget extends SherlockFragmentActivity {
    public SearchWidget(final Context context, final View abSearch) {
        final SearchView searchView = (SearchView) abSearch;
        searchView.setQueryHint("Search!");

        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.v("SearchWidget-onQueryTextChange", "newText=" + newText);
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SearchWidget-onQueryTextSubmit", "query=" + query);

                searchView.clearFocus();
                Intent intentSearch = new Intent(context, Search.class);
                intentSearch.putExtra("query", query);
                intentSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intentSearch);
                return true;
            }
        });

    }

}