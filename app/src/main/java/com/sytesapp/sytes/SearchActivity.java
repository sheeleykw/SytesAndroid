package com.sytesapp.sytes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    private SearchView searchView;
    public static ArrayList<String> searchList = new ArrayList<>();
    public static String searchQuery;
    public static String selectedPosition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final RecyclerView searchListView = findViewById(R.id.searchListView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mAdapter = new MyAdapter(searchList);
        ((MyAdapter) mAdapter).setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!((TextView) view.findViewById(R.id.idText)).getText().toString().equals("Ad")) {
                    MainActivity.currentId = ((TextView) view.findViewById(R.id.idText)).getText().toString();
                    if (MainActivity.currentId.equals("0")) {
                        selectedPosition = ((TextView) view.findViewById(R.id.latLongText)).getText().toString();
                    }
                    if (!MainActivity.findingCity) {
                        MainActivity.goingToPoint = true;
                    }
                    else {
                        searchQuery = ((TextView) view.findViewById(R.id.nameText)).getText().toString().trim();
                    }
                    startMapActivity(null);
                }
            }
        });

        searchListView.setHasFixedSize(true);
        searchListView.setLayoutManager(layoutManager);
        searchListView.setAdapter(mAdapter);

        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search Database");
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
        if (MainActivity.findingCity) {
            ExtraneousMethods.GetCitySearched(searchQuery);
        }
        else {
            ExtraneousMethods.GetSearched(searchQuery);
        }
        TextView noItems = findViewById(R.id.noItems);
        if (searchList.size() < 1) {
            noItems.setVisibility(View.VISIBLE);
        }
        else {
            noItems.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void startMapActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        searchQuery = MainActivity.searchQuery;
        searchView.setQuery(searchQuery, true);

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        searchView.clearFocus();
    }
}
