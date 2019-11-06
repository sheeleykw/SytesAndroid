package com.sytesapp.sytes;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private ItemDatabase dbHelper;
    private SQLiteDatabase itemDatabase;
    private ArrayList<String> markerIds = new ArrayList<String>();
    private HashMap<String, Marker> markerHashMap = new HashMap<>();

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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.538384, -81.385555), 18));
    }

    @Override
    public void onCameraIdle() {
        VisibleRegion vr = map.getProjection().getVisibleRegion();

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_4 + " < ?  AND " + ItemDetails.COL_4 + " > ? AND " + ItemDetails.COL_5 + " < ? AND " + ItemDetails.COL_5 + " > ?"  ;
        String[] selectionArgs = {  String.valueOf(vr.latLngBounds.northeast.latitude),
                                    String.valueOf(vr.latLngBounds.southwest.latitude),
                                    String.valueOf(vr.latLngBounds.northeast.longitude),
                                    String.valueOf(vr.latLngBounds.southwest.longitude) };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            if(markerHashMap.get(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_1))) == null) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .title(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_3)))
                        .snippet(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_7)))
                        .position(new LatLng(cursor.getDouble(cursor.getColumnIndex(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndex(ItemDetails.COL_5))))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.s)));
                markerHashMap.put(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_1)), marker);
            }
            else {
                Marker marker = markerHashMap.get(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_1)));
                marker.remove();
                markerHashMap.remove(cursor.getString(cursor.getColumnIndex(ItemDetails.COL_1)));
            }
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
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

