package com.sytesapp.sytes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView favoritesView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        favoritesView = findViewById(R.id.favoritesView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        favoritesView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        favoritesView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        ArrayList<String> myDataset = MainActivity.currentFavorites;
        mAdapter = new MyAdapter(myDataset);
        favoritesView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        ArrayList<String> myDataset = MainActivity.currentFavorites;
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    public void startMapActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

//    public void startSettingsActivity(View view) {
////        Intent intent = new Intent(this, FavoriteActivity.class);
////        startActivity(intent);
//    }

}
