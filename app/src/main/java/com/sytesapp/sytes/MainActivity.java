package com.sytesapp.sytes;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener {

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private BiMap<String, Marker> markerHashMap = HashBiMap.create();

    private AdView detailAd;
    private SearchView searchView;

    private String refNum;
    private String currentFavorited;
    public static String searchQuery;
    public static String currentId;
    public static boolean goingToPoint = false;
    public static ArrayList<String> currentFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //TODO remove test id from code
//        List<String> testDeviceIds = Arrays.asList("481D9EB0E450EFE1F74321C81D584BCE");
//        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
//        MobileAds.setRequestConfiguration(configuration);

        detailAd = findViewById(R.id.detailAd);

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);
        map.setMaxZoomPreference(18);
        map.setMinZoomPreference(8);
        map.setOnCameraIdleListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowCloseListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.538384, -81.385555), 18));

        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            throw new Error("Unable to process map style");
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

        if (markerHashMap.size() > 100) {
            ExtraneousMethods.RemoveMarkers(markerHashMap, vr);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //ExtraneousMethods.HideKeyboard(this, this.getCurrentFocus());

        currentId = markerHashMap.inverse().get(marker);
        Cursor cursor = ExtraneousMethods.GetCursorFromId_Data(this, currentId);
        cursor.moveToNext();

        refNum = cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2));
        currentFavorited = ExtraneousMethods.UpdateText(cursor, (TextView)findViewById(R.id.detailText), (TextView)findViewById(R.id.titleText), (ImageButton)findViewById(R.id.favoriteButton));
        ExtraneousMethods.DisplayViews();

        ExtraneousMethods.MoveMap(map, marker.getPosition().latitude, marker.getPosition().longitude, false, true);
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        if(!goingToPoint) {
            ExtraneousMethods.HideViews();
        }
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
        intent.putExtra("link", "https://npgallery.nps.gov/pdfhost/docs/NRHP/Photos/" + refNum + ".pdf");
        startActivity(intent);
    }

    public void startPdfRendererActivityDocuments(View view) {
        Intent intent = new Intent(this, PdfRenderer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("link", "https://npgallery.nps.gov/pdfhost/docs/NRHP/Text/" + refNum + ".pdf");
        startActivity(intent);
    }

    public void moveToPoint() {
        Cursor cursor = ExtraneousMethods.GetCursorFromId_Location(this, currentId);
        cursor.moveToNext();
        ExtraneousMethods.MoveMap(map, cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5)), true, false);
        cursor.close();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

        ExtraneousMethods.InitializeAds(this);
        ExtraneousMethods.InitializeAnimations(this, (TableLayout)findViewById(R.id.detailView), (TextView)findViewById(R.id.titleText));

        searchQuery = SearchActivity.searchQuery;
        searchView.setQuery(searchQuery, false);

        if (goingToPoint) {
            moveToPoint();
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        detailAd.loadAd(adRequest);
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

