package com.sytesapp.sytes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sytesapp.sytes.ItemEntry.ItemDetails;

import java.io.Console;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private ItemDatabase dbHelper;
    private SQLiteDatabase itemDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new ItemDatabase(this);

        try {
            dbHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            itemDatabase = dbHelper.getReadableDatabase();
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
        map.setBuildingsEnabled(false);
        map.setMaxZoomPreference(18);
        map.setMinZoomPreference(8);
        map.setOnCameraIdleListener(this);
        map.setOnMarkerClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.538384, -81.385555), 8));
    }

    @Override
    public void onCameraIdle() {
        System.out.println(map.getCameraPosition().bearing);
        System.out.println(map.getCameraPosition().zoom);
        System.out.println(map.getCameraPosition().target.latitude);



        String[] projection = { ItemDetails.COL_2, ItemDetails.COL_3 };
        String selection = ItemDetails.COL_1 + " <= ?";
        String[] selectionArgs = { "4" };

        System.out.println(itemDatabase.getPath());
        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        System.out.println(cursor.getCount());
        for (int i = 0; i < cursor.getColumnCount(); i ++) {
            System.out.println(cursor.getColumnName(i));
        }
        while(cursor.moveToNext()) {
            System.out.println();
            map.addMarker(new MarkerOptions().title(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_3))).snippet(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_7))).position(new LatLng(cursor.getDouble(cursor.getColumnIndex(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndex(ItemDetails.COL_5)))));
        }




    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println("Marker ID:" + marker.getId());



        System.out.println("End marker click event");
        return false;
    }

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

