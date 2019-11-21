package com.sytesapp.sytes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    private SearchView searchView;
    private ArrayList<String> displayedFavorites;
    private ArrayList<String> searchList;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        searchList = (ArrayList)MainActivity.currentFavorites.clone();
        displayedFavorites = (ArrayList)searchList.clone();
        RecyclerView favoritesView = findViewById(R.id.favoritesView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mAdapter = new MyAdapter(this, displayedFavorites);
        ((MyAdapter) mAdapter).setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!((TextView) view.findViewById(R.id.idText)).getText().toString().equals("Ad")) {
                    MainActivity.goingToPoint = true;
                    MainActivity.currentId = ((TextView) view.findViewById(R.id.idText)).getText().toString();
                    startHomeActivity(null);
                }
            }
        });

        favoritesView.setHasFixedSize(true);
        favoritesView.setLayoutManager(layoutManager);
        favoritesView.setAdapter(mAdapter);

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
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                searchQuery = query.toLowerCase();
                searchInitialize();
                mAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    private void searchInitialize() {
        displayedFavorites.clear();
        for (int i = 0; i < searchList.size(); i++) {
            if (searchQuery != null) {
                if (searchList.get(i).split("\n")[1].toLowerCase().contains(searchQuery)) {
                    displayedFavorites.add(searchList.get(i));
                }
            }
            else {
                displayedFavorites.add(searchList.get(i));
                ExtraneousMethods.listAds.get(1).loadAd(ExtraneousMethods.adRequest);
            }
        }
    }

    public void startHomeActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    //TODO add settings button functionality
    public void startSettingsActivity(View view) {
//        Intent intent = new Intent(this, FavoriteActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onResume() {
        ExtraneousMethods.ResetListAds();
        searchList = (ArrayList)MainActivity.currentFavorites.clone();
        searchInitialize();
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        searchView.clearFocus();
    }
}
