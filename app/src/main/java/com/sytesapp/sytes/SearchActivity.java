package com.sytesapp.sytes;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    private SearchView searchView;
    private ArrayList<String> searchList = new ArrayList<>();
    public static String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final RecyclerView searchListView = findViewById(R.id.searchListView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        mAdapter = new MyAdapter(searchList);
        ((MyAdapter) mAdapter).setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MainActivity.goingToPoint = true;
                MainActivity.currentId = ((TextView) view.findViewById(R.id.idText)).getText().toString();
                startMapActivity(null);
            }
        });

        searchListView.setHasFixedSize(true);
        searchListView.setLayoutManager(layoutManager);
        searchListView.setAdapter(mAdapter);

        searchView = findViewById(R.id.searchView);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                searchInitialize();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });
    }

    private void searchInitialize() {
        searchList.clear();

        System.out.println(searchQuery);
        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_9, ItemDetails.COL_11 };
        String selection = ItemDetails.COL_3 + " LIKE ?";
        String[] selectionArgs = { "%" + searchQuery + "%" };

        Cursor cursor = MainActivity.itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);
        System.out.println(cursor.getCount());
        while (cursor.moveToNext()) {
            searchList.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_9)) + ", " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_11)));
        }

        cursor.close();

        mAdapter.notifyDataSetChanged();
    }

    public void startMapActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

//    //TODO add settings button functionality
//    public void startSettingsActivity(View view) {
//        Intent intent = new Intent(this, FavoriteActivity.class);
//        startActivity(intent);
//    }

    @Override
    protected void onResume() {
        searchQuery = MainActivity.searchQuery;
        searchView.setQuery(searchQuery, true);
//        searchView.setIconified(false);
//        searchView.setIconified(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        searchView.clearFocus();
    }
}
