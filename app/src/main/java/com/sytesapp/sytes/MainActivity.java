package com.sytesapp.sytes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static BiMap<String, Marker> markerHashMap = HashBiMap.create();

    private SearchView searchView;
    private ImageButton favoritesButton;
    private ImageButton homeButton;
    private ImageButton settingsButton;
    private FrameLayout adSpace;

    private String currentView = "Home";
    private String currentFavorited;
    private boolean updateFavorites = true;
    private String favoriteSearchQuery;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> displayedFavorites;

    public static String currentId;
    public static String searchQuery;
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

        new ExtraneousMethods.InitializeDatabases().execute(this);
        new ExtraneousMethods.InitializeAds().execute(this);

        favoritesButton = findViewById(R.id.favoritesButton);
        homeButton = findViewById(R.id.homeButton);
        settingsButton = findViewById(R.id.settingsButton);
        TableLayout detailView = findViewById(R.id.detailView);
        RelativeLayout titleView = findViewById(R.id.titleView);
        RelativeLayout favoritesView = findViewById(R.id.favoritesView);
        RelativeLayout settingsView = findViewById(R.id.settingsView);
        adSpace = findViewById(R.id.adSpace);

        ExtraneousMethods.InitializeAnimations(this, detailView, titleView, favoritesView, settingsView);

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
                searchQuery = query;
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

        displayedFavorites = new ArrayList<>(MainActivity.currentFavorites);
        RecyclerView listView = findViewById(R.id.listView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mAdapter = new MyAdapter(displayedFavorites);
        ((MyAdapter) mAdapter).setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!((TextView) view.findViewById(R.id.idText)).getText().toString().equals("Ad")) {
                    MainActivity.goingToPoint = true;
                    MainActivity.currentId = ((TextView) view.findViewById(R.id.idText)).getText().toString();
                    changeView(homeButton);
                    moveToPoint();
                }
            }
        });

        listView.setHasFixedSize(true);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
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

        Cursor cursor = ExtraneousMethods.GetCursorFromRegion(vr);
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
        Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (ExtraneousMethods.adsReady) {
            adSpace.addView(ExtraneousMethods.detailAdView);
            ExtraneousMethods.adsReady = false;
        }

        ((ScrollView)findViewById(R.id.scrollView)).fullScroll(View.FOCUS_UP);

        currentId = markerHashMap.inverse().get(marker);
        Cursor cursor = ExtraneousMethods.GetCursorFromId(currentId);
        cursor.moveToNext();

        currentFavorited = ExtraneousMethods.UpdateText(cursor, (TextView)findViewById(R.id.titleText), (TextView)findViewById(R.id.categoryText), (TextView)findViewById(R.id.dateText), (TextView)findViewById(R.id.refText), (TextView)findViewById(R.id.streetText), (TextView)findViewById(R.id.locationText), (TextView)findViewById(R.id.countyText), (TextView)findViewById(R.id.buildersText), (ImageButton)findViewById(R.id.favoriteButton));
        ExtraneousMethods.DisplayViews();

        ExtraneousMethods.MoveMap(map, marker.getPosition().latitude, marker.getPosition().longitude, map.getCameraPosition().zoom, true, true);
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        if (!goingToPoint) {
            ExtraneousMethods.HideViews();
        }
        if (currentView.equals("Home")) {
            currentId = null;
        }
    }

    private void moveToPoint() {
        LatLng pointPosition = ExtraneousMethods.GetLatLngFromId(currentId);
        if (currentId.equals("0")) {
            ExtraneousMethods.MoveMap(map, pointPosition.latitude, pointPosition.longitude, 12, false, false);
            goingToPoint = false;
        }
        else {
            ExtraneousMethods.MoveMap(map, pointPosition.latitude, pointPosition.longitude, 18, false, true);
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
        updateFavorites = true;

        if (currentFavorited.equals("TRUE")) {
            favoriteButton.setImageResource(R.drawable.fullheart);
        }
        else {
            favoriteButton.setImageResource(R.drawable.greyheart);
        }
    }

    private void searchInitialize() {
        displayedFavorites.clear();
        for (int i = 0; i < currentFavorites.size(); i++) {
            if (favoriteSearchQuery != null) {
                if (currentFavorites.get(i).split("\n")[1].toLowerCase().contains(favoriteSearchQuery.toLowerCase())) {
                    displayedFavorites.add(currentFavorites.get(i));
                }
            }
            else {
                displayedFavorites.add(currentFavorites.get(i));
            }
        }
    }

    public void changeView(View view) {
        favoritesButton.setColorFilter(Color.parseColor("#797979"));
        homeButton.setColorFilter(Color.parseColor("#797979"));
        settingsButton.setColorFilter(Color.parseColor("#797979"));
        favoritesButton.setClickable(true);
        homeButton.setClickable(true);
        settingsButton.setClickable(true);


        if (view.equals(homeButton)) {
            System.out.println("home");
            System.out.println(searchQuery);
            searchView.setQueryHint("Search Database");
            searchView.setQuery(searchQuery, false);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    startSearchActivity(null);
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String query) {
                    searchQuery = query;
                    return false;
                }
            });
            homeButton.setColorFilter(Color.parseColor("#5F90FE"));
            homeButton.setClickable(false);

            ExtraneousMethods.ChangeView(currentView, "Home");
            currentView = "Home";

            Marker marker = markerHashMap.get(currentId);
            if (marker != null) {
                onMarkerClick(marker);
            }
        }
        else {
            if (view.equals(favoritesButton)) {
                System.out.println("favorites");
                System.out.println(favoriteSearchQuery);
                searchView.setQueryHint("Search Favorites");
                searchView.setQuery(favoriteSearchQuery, false);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String query) {
                        System.out.println(query);
                        favoriteSearchQuery = query;
                        searchInitialize();
                        mAdapter.notifyDataSetChanged();
                        return false;
                    }
                });

                favoritesButton.setColorFilter(Color.parseColor("#5F90FE"));
                favoritesButton.setClickable(false);

                if (updateFavorites) {
                    ExtraneousMethods.GetFavorited();
                    searchInitialize();
                    mAdapter.notifyDataSetChanged();
                    updateFavorites = false;
                }

                ExtraneousMethods.ChangeView(currentView, "Favorites");
                currentView = "Favorites";
            }
            else if (view.equals(settingsButton)) {
                settingsButton.setColorFilter(Color.parseColor("#5F90FE"));
                settingsButton.setClickable(false);

                ExtraneousMethods.ChangeView(currentView, "Settings");
                currentView = "Settings";
            }
            Marker marker = markerHashMap.get(currentId);
            if (marker != null) {
                marker.hideInfoWindow();
            }
        }
    }

    public void startSearchActivity(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
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
}

