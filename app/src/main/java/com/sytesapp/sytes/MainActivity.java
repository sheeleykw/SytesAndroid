package com.sytesapp.sytes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final String VERSION = "paid";
    public static BiMap<String, Marker> markerHashMap = HashBiMap.create();

    private SearchView searchView;
    private ImageButton favoritesButton;
    private ImageButton homeButton;
    private ImageButton settingsButton;
    private FrameLayout adSpace;
    private TextView locationStart;

    private String currentView = "Home";
    private String currentFavorited;
    private boolean updateFavorites = true;
    private String favoriteSearchQuery;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> displayedFavorites;
    private SharedPreferences settings;

    public static String currentId;
    public static String searchQuery;
    public static String photosLink = null;
    public static String docsLink = null;
    public static boolean goingToPoint = false;
    public static boolean findingCity = false;
    public static Location userLocation = null;
    public static String startupLocation;
    public static ArrayList<String> currentFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new ExtraneousMethods.InitializeDatabases().execute(this);
        new ExtraneousMethods.InitializeAds().execute(this);
        settings = getApplicationContext().getSharedPreferences("SETTINGS", 0);
        startupLocation = settings.getString("StartUpLocation", "1840012541");

        favoritesButton = findViewById(R.id.favoritesButton);
        homeButton = findViewById(R.id.homeButton);
        settingsButton = findViewById(R.id.settingsButton);
        locationStart = findViewById(R.id.locationStart);
        TableLayout detailView = findViewById(R.id.detailView);
        RelativeLayout titleView = findViewById(R.id.titleView);
        RelativeLayout favoritesView = findViewById(R.id.favoritesView);
        LinearLayout settingsView = findViewById(R.id.settingsView);
        adSpace = findViewById(R.id.adSpace);

        Switch switchView = findViewById(R.id.userStart);
        locationStart.setText(settings.getString("LocationSetting", "Start at Location: Valley City, Illinois"));
        if (startupLocation.equals("0")) {
            switchView.setChecked(true);
            locationStart.setTextColor(Color.parseColor("#A5C7C7C7"));
            locationStart.setClickable(false);
        }
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.onCheckedChanged(isChecked);
            }
        });

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

        setTheme(R.style.AppTheme);
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

        if (findingCity && searchQuery.split(",").length == 2) {
            locationStart.setText(MessageFormat.format("Start at Location: {0}, {1}", searchQuery.split(",")[0].trim(), searchQuery.split(",")[1]).trim());
            onCheckedChanged(false);
            findingCity = false;
        }
        else {
            searchView.setQuery(searchQuery, false);
        }

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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.706613, -90.652503), 12));

        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            throw new Error("Unable to process map style");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            EnableUserLocation();
        }

        if (!startupLocation.equals("0") && !startupLocation.equals("1840012541")) {
            String[] returnArray = ExtraneousMethods.GetCityFromId(startupLocation);
            locationStart.setText(MessageFormat.format("Start at Location: {0}, {1}", returnArray[0].trim(), returnArray[1].trim()));

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(returnArray[2]), Double.valueOf(returnArray[3])), 12));
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
            else {
                Switch switchView = findViewById(R.id.userStart);
                switchView.setTextColor(Color.parseColor("#A5C7C7C7"));
                switchView.setClickable(false);
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
                            if (startupLocation.equals("0")) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 15));
                            }

                        }
                    }
                });
    }

    public void onCheckedChanged(boolean isChecked) {
        SharedPreferences.Editor editor = settings.edit();
        if (isChecked) {
            editor.putString("StartUpLocation", "0");
            locationStart.setTextColor(Color.parseColor("#A5C7C7C7"));
            locationStart.setClickable(false);
            editor.putString("LocationSetting", locationStart.getText().toString());
        }
        else {
            String cityState = locationStart.getText().toString().split(":")[1];
            String city = cityState.split(",")[0].trim();
            String state = cityState.split(",")[1].trim();
            String id = ExtraneousMethods.GetIdFromCity(city, state);
            editor.putString("StartUpLocation", id);
            locationStart.setTextColor(Color.parseColor("#000000"));
            locationStart.setClickable(true);
        }
        editor.apply();
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
        TextView noItems = findViewById(R.id.noItems);
        if (displayedFavorites.size() < 1 && currentFavorites.size() > 0) {
            noItems.setVisibility(View.VISIBLE);
        }
        else {
            noItems.setVisibility(View.GONE);
        }
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

    public void changeCityLocation(View view) {
        findingCity = true;
        searchQuery = locationStart.getText().toString().split(":")[1].split(",")[0].trim();
        startSearchActivity(view);
    }

    public void displayPrivacy(View view) {
        Intent intent = new Intent(this, textview.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("text", "Policy");
        startActivity(intent);
    }
    public void displayCopyright(View view) {
        Intent intent = new Intent(this, textview.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("text", "Copyright");
        startActivity(intent);
    }
    public void displayQA(View view) {
        Intent intent = new Intent(this, textview.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("text", "Questions");
        startActivity(intent);
    }

    @SuppressLint("LongLogTag")
    public void sendEmail(View view) {
        String[] TO = {"leve1incorp@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporting Issue: #" + currentId);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "An issue has been found with Unique ID point: " + currentId + "." + "\n\nExtra details from user: ");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void changeView(View view) {
        if (ExtraneousMethods.animationsReady) {
            favoritesButton.setColorFilter(Color.parseColor("#797979"));
            homeButton.setColorFilter(Color.parseColor("#797979"));
            settingsButton.setColorFilter(Color.parseColor("#797979"));
            favoritesButton.setClickable(true);
            homeButton.setClickable(true);
            settingsButton.setClickable(true);

            if (view.equals(homeButton)) {
                searchView.setQueryHint("Search Database");
                searchView.setQuery("", false);
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
                    searchView.setQueryHint("Search Favorites");
                    searchView.setQuery("", false);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }
                        @Override
                        public boolean onQueryTextChange(String query) {
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

                    TextView noFavorites = findViewById(R.id.noFavorites);
                    if (currentFavorites.size() < 1) {
                        noFavorites.setVisibility(View.VISIBLE);
                    }
                    else {
                        noFavorites.setVisibility(View.GONE);
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

