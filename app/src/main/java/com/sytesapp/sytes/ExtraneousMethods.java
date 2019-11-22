package com.sytesapp.sytes;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.collect.BiMap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

class ExtraneousMethods {

    private static SQLiteDatabase itemDatabase;
    private static SQLiteDatabase cityDatabase;
    private static ObjectAnimator detailUpAnimation;
    private static ObjectAnimator detailDownAnimation;
    private static ObjectAnimator titleUpAnimation;
    private static ObjectAnimator titleDownAnimation;
    private static int adWidth;
    private static int numOfFavorites;
    private static int currentAd = -1;
    private static int spaceBetweenAds = 5;
    static AdRequest adRequest;
    static AdView detailAdView;
    static ArrayList<AdView> listAds = new ArrayList<>();
    private static ArrayList<FrameLayout> listFrames = new ArrayList<>();
    private static boolean itemDatabaseReady = false;
    private static boolean cityDatabaseReady = false;
    private static boolean adsReady = false;

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Begin Map/Database related Methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static Cursor GetCursorFromRegion(Context context, VisibleRegion vr) {
        if (!itemDatabaseReady) {
            InitializeItemDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_4 + " < ?  AND " + ItemDetails.COL_4 + " > ? AND " + ItemDetails.COL_5 + " < ? AND " + ItemDetails.COL_5 + " > ?";
        String[] selectionArgs = {  String.valueOf(vr.latLngBounds.northeast.latitude), String.valueOf(vr.latLngBounds.southwest.latitude), String.valueOf(vr.latLngBounds.northeast.longitude), String.valueOf(vr.latLngBounds.southwest.longitude) };

        return itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    static Cursor GetCursorFromId(Context context, String id) {
        if (!itemDatabaseReady) {
            InitializeItemDatabase(context);
        }

        String[] projection = { ItemDetails.COL_2, ItemDetails.COL_3, ItemDetails.COL_6, ItemDetails.COL_7, ItemDetails.COL_8, ItemDetails.COL_9, ItemDetails.COL_10, ItemDetails.COL_11, ItemDetails.COL_12, ItemDetails.COL_13 };
        String selection = ItemDetails.COL_1 + " = ?";
        String[] selectionArgs = { id };

        return itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    static LatLng GetLatLngFromId(Context context, String id) {
        if (!itemDatabaseReady) {
            InitializeItemDatabase(context);
        }

        if (id.equals("0")) {
            return new LatLng(Double.valueOf(SearchActivity.selectedPosition.split(",")[0]), Double.valueOf(SearchActivity.selectedPosition.split(",")[1]));
        }
        else {
            String[] projection = { ItemDetails.COL_4, ItemDetails.COL_5 };
            String selection = ItemDetails.COL_1 + " = ?";
            String[] selectionArgs = { id };
            Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            cursor.moveToNext();

            return new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5)));
        }
    }

    static void GetFavorited(Context context) {
        if (!itemDatabaseReady) {
            InitializeItemDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_13 + " = ?";
        String[] selectionArgs = { "TRUE" };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);

        MainActivity.currentFavorites.clear();
        while (cursor.moveToNext()) {
            MainActivity.currentFavorites.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
            if (((MainActivity.currentFavorites.size() + 1) % spaceBetweenAds) == 0) {
                MainActivity.currentFavorites.add("Ad\n______\nnull");
            }
        }

        numOfFavorites = cursor.getCount();
        AddListAds(context);

        cursor.close();
    }

    private static void GetFavoritedCount(Context context) {
        if (!itemDatabaseReady) {
            InitializeItemDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_13 + " = ?";
        String[] selectionArgs = { "TRUE" };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);
        numOfFavorites = cursor.getCount();
        cursor.close();
    }

    static void GetSearched(Context context, String searchQuery) {
        if (!itemDatabaseReady) {
            InitializeItemDatabase(context);
        }
        if (!cityDatabaseReady) {
            InitializeCityDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, ItemDetails.COL_9, ItemDetails.COL_11 };
        String selection = ItemDetails.COL_3 + " LIKE ?";
        String[] selectionArgs = { "%" + searchQuery + "%" };

        Cursor cursor = itemDatabase.query(ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);

        SearchActivity.searchList.clear();
        while (cursor.moveToNext()) {
            SearchActivity.searchList.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_9)) + ", " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_11)) + "\n" + cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)) + "," + cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5)));
        }
        cursor.close();
        
        String[] cityProjection = { ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, "StateName", "NumOfPoints" };

        cursor = cityDatabase.query( "cities", cityProjection, selection, selectionArgs, null, null, ItemDetails.COL_3);

        while (cursor.moveToNext()) {
            SearchActivity.searchList.add("0" + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + ", " + cursor.getString(cursor.getColumnIndexOrThrow("StateName")) + "\n" + "Number of sites found in city: " + cursor.getString(cursor.getColumnIndexOrThrow("NumOfPoints")) + "\n" + cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)) + "," + cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5)));
        }
        cursor.close();

        if (MainActivity.userLocation != null ) {
            SortSearchList();
        }

    }

    static MarkerOptions GetMarkerOptions(Context context, Cursor cursor) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)));
        markerOptions.snippet(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
        markerOptions.position(new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5))));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFromDrawable(Objects.requireNonNull(context.getDrawable(R.drawable.swithoutshadow)))));

        return markerOptions;
    }

    static void MoveMap(GoogleMap map, double latitude, double longitude, float zoomLevel, boolean animating, boolean offCenter) {
        double offCenterDistance = 0.0;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(map.getCameraPosition().target, zoomLevel));

        if (offCenter) {
            VisibleRegion vr = map.getProjection().getVisibleRegion();
            offCenterDistance = (vr.latLngBounds.northeast.latitude - vr.latLngBounds.southwest.latitude) / 5.0;
        }

        if (animating) {
            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude - offCenterDistance, longitude)), 500, null);
        }
        else {
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude - offCenterDistance, longitude)));
        }
    }

    static String UpdateText(Cursor cursor, TextView titleText, TextView categoryText, TextView dateText, TextView refText, TextView streetText, TextView locationText, TextView countyText, TextView buildersText, ImageButton favoriteButton) {
        String favorited = "FALSE";
        if (cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_13)).equals("TRUE")) {
            favoriteButton.setImageResource(R.drawable.fullheart);
            favorited = "TRUE";
        }
        else {
            favoriteButton.setImageResource(R.drawable.greyheart);
        }

        titleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)));

        categoryText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
        dateText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_6)));
        refText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2)));
        streetText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_8)));
        locationText.setText(MessageFormat.format("{0}, {1}", cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_9)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_11))));
        countyText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_10)));
        buildersText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_12)));

        MainActivity.photosLink = "https://npgallery.nps.gov/pdfhost/docs/NRHP/Photos/" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2)) + ".pdf";
        MainActivity.docsLink = "https://npgallery.nps.gov/pdfhost/docs/NRHP/Text/" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2)) + ".pdf";
        cursor.close();

        return favorited;
    }

    static String UpdateFavoriteStatus(String id, String favorited) {
        String selection = ItemDetails.COL_1 + " = ?";
        String[] selectionArgs = { id };

        ContentValues values = new ContentValues();
        if (favorited.equals("TRUE")) {
            values.put(ItemDetails.COL_13, "FALSE");
            favorited = "FALSE";
        }
        else {
            values.put(ItemDetails.COL_13, "TRUE");
            favorited = "TRUE";
        }

        itemDatabase.update( ItemDetails.TABLE_NAME, values, selection, selectionArgs);

        return favorited;
    }

    static void RemoveMarkers(BiMap<String, Marker> markerHashMap, VisibleRegion vr) {
        ArrayList<String> removeStrings = new ArrayList<>();

        for (HashMap.Entry<String, Marker> entry : markerHashMap.entrySet()) {
            if (entry.getValue().getPosition().latitude > vr.latLngBounds.northeast.latitude || entry.getValue().getPosition().latitude < vr.latLngBounds.southwest.latitude || entry.getValue().getPosition().longitude > vr.latLngBounds.northeast.longitude || entry.getValue().getPosition().longitude < vr.latLngBounds.southwest.longitude) {
                removeStrings.add(entry.getKey());
            }
        }

        for (String removal : removeStrings) {
            markerHashMap.get(removal).remove();
            markerHashMap.remove(removal);
        }
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //End Map/Database related Methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Begin Initialization related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static void InitializeAds(Context context){
        if(!adsReady) {
            MobileAds.initialize(context, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            adRequest = new AdRequest.Builder().addTestDevice("481D9EB0E450EFE1F74321C81D584BCE").build();
            float widthPixels = context.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
            float density = context.getApplicationContext().getResources().getDisplayMetrics().density;
            adWidth = (int) (widthPixels / density);

            detailAdView = new AdView(context);
            detailAdView.setAdUnitId("ca-app-pub-3281339494640251/9986601233");

            detailAdView.setAdSize(AdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, adWidth));
            detailAdView.loadAd(adRequest);

            GetFavoritedCount(context);
            AddListAds(context);

            adsReady = true;
        }
    }

    static void InitializeAnimations(Context context, TableLayout detailView, RelativeLayout titleView) {
        detailUpAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 0);
        detailUpAnimation.setDuration(600);
        detailDownAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        detailUpAnimation.setDuration(400);

        titleDownAnimation = ObjectAnimator.ofFloat(titleView, "translationY", 0);
        titleDownAnimation.setDuration(400);
        titleUpAnimation = ObjectAnimator.ofFloat(titleView, "translationY", -500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        titleUpAnimation.setDuration(400);
    }

    private static void InitializeItemDatabase(Context context) {

        ItemDatabase dbHelper = new ItemDatabase(context);

        try {
            dbHelper.updateDataBase();
            itemDatabase = dbHelper.getWritableDatabase();
        } catch (Exception exception) {
            throw new Error("UnableToUpdateDatabase");
        }

        itemDatabaseReady = true;
    }
    
    private static void InitializeCityDatabase(Context context) {

        CityDatabase dbHelper = new CityDatabase(context);

        try {
            dbHelper.updateDataBase();
            cityDatabase = dbHelper.getWritableDatabase();
        } catch (Exception exception) {
            throw new Error("UnableToUpdateDatabase");
        }
        
        cityDatabaseReady = true;
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //End Initialization related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Begin Other non-related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private static void SortSearchList() {
        for (int listLength = SearchActivity.searchList.size(); listLength > 1; listLength--) {
            for (int index = 0; index < listLength - 1; index++) {
                double currentLatitude = Double.valueOf(SearchActivity.searchList.get(index).split("\n")[3].split(",")[0]);
                double currentLongitude = Double.valueOf(SearchActivity.searchList.get(index).split("\n")[3].split(",")[1]);
                double currentDiff = Math.abs(currentLatitude - MainActivity.userLocation.getLatitude()) + Math.abs(currentLongitude - MainActivity.userLocation.getLongitude());

                double nextLatitude = Double.valueOf(SearchActivity.searchList.get(index + 1).split("\n")[3].split(",")[0]);
                double nextLongitude = Double.valueOf(SearchActivity.searchList.get(index + 1).split("\n")[3].split(",")[1]);
                double nextDiff = Math.abs(nextLatitude - MainActivity.userLocation.getLatitude()) + Math.abs(nextLongitude - MainActivity.userLocation.getLongitude());

                if(currentDiff > nextDiff) {
                    String currentString = SearchActivity.searchList.get(index);
                    String nextString = SearchActivity.searchList.get(index + 1);
                    SearchActivity.searchList.set(index, nextString);
                    SearchActivity.searchList.set(index + 1, currentString);
                }
            }
        }
    }

    private static void AddListAds(Context context) {
        while(listAds.size() < (numOfFavorites / spaceBetweenAds)) {
            AdView listAdView = new AdView(context);
            listAdView.setAdUnitId("ca-app-pub-3281339494640251/4734274558");

            listAdView.setAdSize(AdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, adWidth - 16));
            listAdView.loadAd(adRequest);
            listAds.add(listAdView);
        }
    }

    static void AddListAdToFrame(FrameLayout frame) {
        if (adsReady) {
            currentAd++;
            if (currentAd < listAds.size()) {
                frame.addView(listAds.get(currentAd));
                listFrames.add(frame);
            }
        }
    }

    static void ResetListAds() {
        currentAd = -1;
        for(FrameLayout layout: listFrames) {
            layout.removeAllViews();
        }
        listFrames.clear();
    }

    private static Bitmap BitmapFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    static void DisplayViews() {
        detailUpAnimation.start();
        titleDownAnimation.start();
    }

    static void HideViews() {
        detailDownAnimation.start();
        titleUpAnimation.start();
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //End Other non-related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
}
