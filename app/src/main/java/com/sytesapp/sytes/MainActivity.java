package com.sytesapp.sytes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private BiMap<String, Marker> markerHashMap = HashBiMap.create();

    private SearchView searchView;

    private String currentFavorited;
    public static String searchQuery;
    public static String currentId;
    public static String photosLink = null;
    public static String docsLink = null;
    public static boolean goingToPoint = false;
    public static Location userLocation = null;
    public static ArrayList<String> currentFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExtraneousMethods.InitializeAds(this);

//        //TODO remove test id from code
        FrameLayout adSpace = findViewById(R.id.adSpace);
        adSpace.addView(ExtraneousMethods.detailAdView);

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
                startSearchActivity(null);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView = findViewById((R.id.mapView));
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.setMaxZoomPreference(18);
        map.setMinZoomPreference(10);
        map.setOnCameraIdleListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowCloseListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.706613, -90.652503), 15));

        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            throw new Error("Unable to process map style");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            EnableUserLocation();
        }
    }

    @Override
    public void onCameraIdle() {
        VisibleRegion vr = map.getProjection().getVisibleRegion();

        Cursor cursor = ExtraneousMethods.GetCursorFromRegion(this, vr);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1));

            if(markerHashMap.get(id) == null) {
                Marker marker = map.addMarker(ExtraneousMethods.GetMarkerOptions(this, cursor));
                markerHashMap.put(id, marker);
            }

            if (goingToPoint && id.equals(currentId)) {
                onMarkerClick(markerHashMap.get(currentId));
                goingToPoint = false;
            }
        }
        cursor.close();

        ExtraneousMethods.RemoveMarkers(markerHashMap, vr);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();

        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        ((ScrollView)findViewById(R.id.scrollView)).fullScroll(View.FOCUS_UP);

        currentId = markerHashMap.inverse().get(marker);
        Cursor cursor = ExtraneousMethods.GetCursorFromId(this, currentId);
        cursor.moveToNext();

        currentFavorited = ExtraneousMethods.UpdateText(cursor, (TextView)findViewById(R.id.titleText), (TextView)findViewById(R.id.categoryText), (TextView)findViewById(R.id.dateText), (TextView)findViewById(R.id.refText), (TextView)findViewById(R.id.streetText), (TextView)findViewById(R.id.locationText), (TextView)findViewById(R.id.countyText), (TextView)findViewById(R.id.buildersText), (ImageButton)findViewById(R.id.favoriteButton));
        ExtraneousMethods.DisplayViews();

        ExtraneousMethods.MoveMap(map, marker.getPosition().latitude, marker.getPosition().longitude, map.getCameraPosition().zoom, true, true);
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        if(!goingToPoint) {
            ExtraneousMethods.HideViews();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                EnableUserLocation();
            }
        }
    }

    private void EnableUserLocation() {
        map.setMyLocationEnabled(true);
        FusedLocationProviderClient locationHandler = LocationServices.getFusedLocationProviderClient(this);
        locationHandler.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLocation = location;
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 15));
                        }
                    }
                });
    }

    public void switchFavoriteStatus(View view) {
        ImageButton favoriteButton = findViewById(R.id.favoriteButton);
        currentFavorited = ExtraneousMethods.UpdateFavoriteStatus(currentId, currentFavorited);

        if (currentFavorited.equals("TRUE")) {
            favoriteButton.setImageResource(R.drawable.fullheart);
        }
        else {
            favoriteButton.setImageResource(R.drawable.greyheart);
        }
    }

    public void startFavoritesActivity(View view) {
        ExtraneousMethods.GetFavorited(this);

        Intent intent = new Intent(this, FavoriteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void startSearchActivity(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    //TODO add settings button functionality
    public void startSettingsActivity(View view) {
//        Intent intent = new Intent(this, FavoriteActivity.class);
//        startActivity(intent);
    }

    public void startPdfRendererActivityPhotos(View view) {
        Intent intent = new Intent(this, PdfRenderer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("link", photosLink);
        startActivity(intent);
    }

    public void startPdfRendererActivityDocuments(View view) {
        Intent intent = new Intent(this, PdfRenderer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("link", docsLink);
        startActivity(intent);
    }

    private void moveToPoint() {
        LatLng pointPosition = ExtraneousMethods.GetLatLngFromId(this, currentId);
        if (currentId.equals("0")) {
            ExtraneousMethods.MoveMap(map, pointPosition.latitude, pointPosition.longitude, 12, false, false);
            goingToPoint = false;
        }
        else {
            ExtraneousMethods.MoveMap(map, pointPosition.latitude, pointPosition.longitude, 18, false, true);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();

        ExtraneousMethods.InitializeAnimations(this, (TableLayout)findViewById(R.id.detailView), (RelativeLayout)findViewById(R.id.titleView));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

        searchQuery = SearchActivity.searchQuery;
        searchView.setQuery(searchQuery, false);

        if (goingToPoint) {
            moveToPoint();
        }

        //detailAd.loadAd(adRequest);
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
        searchView.clearFocus();
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

