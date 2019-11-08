package com.sytesapp.sytes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    public static ArrayList<String> displayedFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        displayedFavorites = MainActivity.currentFavorites;
        RecyclerView favoritesView = findViewById(R.id.favoritesView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        mAdapter = new MyAdapter(displayedFavorites);
        ((MyAdapter) mAdapter).setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MainActivity.goingToPoint = true;
                MainActivity.currentId = ((TextView) view.findViewById(R.id.idText)).getText().toString();
                startMapActivity(null);
            }
        });

        favoritesView.setHasFixedSize(true);
        favoritesView.setLayoutManager(layoutManager);
        favoritesView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        displayedFavorites = MainActivity.currentFavorites;
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
