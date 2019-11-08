package com.sytesapp.sytes;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener {

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private ItemDatabase dbHelper;
    private SQLiteDatabase itemDatabase;
    private BiMap<String, Marker> markerHashMap = HashBiMap.create();
    private ObjectAnimator titleUpAnimation;
    private ObjectAnimator titleDownAnimation;
    private ObjectAnimator detailUpAnimation;
    private ObjectAnimator detailDownAnimation;
    private TableLayout detailView;
    private TextView detailText;
    private TextView titleText;
    private ImageButton favoriteButton;
    private String currentId;
    private String currentFavorited;
    public static ArrayList<String> currentFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detailView  = findViewById(R.id.detailView);
        detailText  = findViewById(R.id.detailText);
        titleText = findViewById(R.id.titleText);
        favoriteButton = findViewById(R.id.favoriteButton);

        detailUpAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 0);
        detailUpAnimation.setDuration(600);
        detailDownAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 500 * getApplicationContext().getResources().getDisplayMetrics().density);
        detailUpAnimation.setDuration(400);

        titleDownAnimation = ObjectAnimator.ofFloat(titleText, "translationY", 0);
        titleDownAnimation.setDuration(400);
        titleUpAnimation = ObjectAnimator.ofFloat(titleText, "translationY", -500 * getApplicationContext().getResources().getDisplayMetrics().density);
        titleUpAnimation.setDuration(400);


        dbHelper = new ItemDatabase(this);

        try {
            dbHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        try {
            itemDatabase = dbHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView = findViewById((R.id.mapView));
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            throw new Error("Unable to process map style");
        }

        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);
        map.setMaxZoomPreference(18);
        map.setMinZoomPreference(8);
        map.setOnCameraIdleListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowCloseListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.538384, -81.385555), 18));
    }

    @Override
    public void onCameraIdle() {
        VisibleRegion vr = map.getProjection().getVisibleRegion();
        ArrayList<String> currentStrings = new ArrayList<>();
        ArrayList<String> removeStrings = new ArrayList<>();

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_4 + " < ?  AND " + ItemDetails.COL_4 + " > ? AND " + ItemDetails.COL_5 + " < ? AND " + ItemDetails.COL_5 + " > ?";
        String[] selectionArgs = {  String.valueOf(vr.latLngBounds.northeast.latitude),
                                    String.valueOf(vr.latLngBounds.southwest.latitude),
                                    String.valueOf(vr.latLngBounds.northeast.longitude),
                                    String.valueOf(vr.latLngBounds.southwest.longitude) };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            currentStrings.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)));
            if(markerHashMap.get(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1))) == null) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .title(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)))
                        .snippet(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)))
                        .position(new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5))))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.s)));
                markerHashMap.put(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)), marker);
            }
        }

        for (HashMap.Entry<String, Marker> entry : markerHashMap.entrySet()) {
            if (!currentStrings.contains(entry.getKey())) {
                removeStrings.add(entry.getKey());
            }
        }
        for (String removal : removeStrings) {
            markerHashMap.get(removal).remove();
            markerHashMap.remove(removal);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String[] projection = { ItemDetails.COL_2, ItemDetails.COL_3, ItemDetails.COL_6, ItemDetails.COL_7, ItemDetails.COL_8, ItemDetails.COL_9, ItemDetails.COL_10, ItemDetails.COL_11, ItemDetails.COL_12, ItemDetails.COL_13 };
        String selection = ItemDetails.COL_1 + " = ?";
        currentId = markerHashMap.inverse().get(marker);
        String[] selectionArgs = { currentId };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        cursor.moveToNext();

        detailText.setText( "Category: " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)) +
                            "\nReference Number: " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2)) +
                            "\nDate added to register: " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_6)) +
                            "\nReported Street Address: " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_8)) +
                            "\nLocation: " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_9)) + ", " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_11)) +
                            "\nCounty: " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_10)) +
                            "\nArchitects/Builders: " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_12)));
        if (cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_13)).equals("TRUE")) {
            favoriteButton.setImageResource(R.drawable.bluehearticon);
            currentFavorited = "TRUE";
        }
        else {
            favoriteButton.setImageResource(R.drawable.bluehearticonhollow);
            currentFavorited = "FALSE";
        }
        titleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)));
        detailUpAnimation.start();
        titleDownAnimation.start();
        return false;
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        detailDownAnimation.start();
        titleUpAnimation.start();
    }

    public void switchFavoriteStatus(View view) {
        String selection = ItemDetails.COL_1 + " = ?";
        String[] selectionArgs = { currentId };

        ContentValues values = new ContentValues();
        if (currentFavorited.equals("TRUE")) {
            values.put(ItemDetails.COL_13, "FALSE");
            currentFavorited = "FALSE";
            favoriteButton.setImageResource(R.drawable.bluehearticonhollow);
        }
        else {
            values.put(ItemDetails.COL_13, "TRUE");
            currentFavorited = "TRUE";
            favoriteButton.setImageResource(R.drawable.bluehearticon);
        }

        itemDatabase.update( ItemDetails.TABLE_NAME, values, selection, selectionArgs);
    }

    public void startFavoriteActivity(View view) {
        currentFavorites.clear();
        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_13 + " = ?";
        String[] selectionArgs = { "TRUE" };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);
        while (cursor.moveToNext()) {
            currentFavorites.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
        }

        Intent intent = new Intent(this, FavoriteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

//    public void startSettingsActivity(View view) {
////        Intent intent = new Intent(this, FavoriteActivity.class);
////        startActivity(intent);
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}

