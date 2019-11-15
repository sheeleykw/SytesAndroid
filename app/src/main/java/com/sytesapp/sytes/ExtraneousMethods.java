package com.sytesapp.sytes;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

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

class ExtraneousMethods {

    private static SQLiteDatabase itemDatabase;
    private static SQLiteDatabase cityDatabase;
    private static ObjectAnimator detailUpAnimation;
    private static ObjectAnimator detailDownAnimation;
    private static ObjectAnimator titleUpAnimation;
    private static ObjectAnimator titleDownAnimation;
    private static boolean databasesReady = false;
    private static boolean adsReady = false;

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Begin Map/Database related Methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static Cursor GetCursorFromRegion(Context context, VisibleRegion vr) {
        if (!databasesReady) {
            InitializeDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_4 + " < ?  AND " + ItemDetails.COL_4 + " > ? AND " + ItemDetails.COL_5 + " < ? AND " + ItemDetails.COL_5 + " > ?";
        String[] selectionArgs = {  String.valueOf(vr.latLngBounds.northeast.latitude), String.valueOf(vr.latLngBounds.southwest.latitude), String.valueOf(vr.latLngBounds.northeast.longitude), String.valueOf(vr.latLngBounds.southwest.longitude) };

        return itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    static Cursor GetCursorFromId(Context context, String id) {
        if (!databasesReady) {
            InitializeDatabase(context);
        }

        String[] projection = { ItemDetails.COL_2, ItemDetails.COL_3, ItemDetails.COL_6, ItemDetails.COL_7, ItemDetails.COL_8, ItemDetails.COL_9, ItemDetails.COL_10, ItemDetails.COL_11, ItemDetails.COL_12, ItemDetails.COL_13 };
        String selection = ItemDetails.COL_1 + " = ?";
        String[] selectionArgs = { id };

        return itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    static LatLng GetLatLngFromId(Context context, String id) {
        if (!databasesReady) {
            InitializeDatabase(context);
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
        if (!databasesReady) {
            InitializeDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_13 + " = ?";
        String[] selectionArgs = { "TRUE" };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        MainActivity.currentFavorites.clear();
        while (cursor.moveToNext()) {
            MainActivity.currentFavorites.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
        }
        cursor.close();
    }

    static void GetSearched(Context context, String searchQuery) {
        if (!databasesReady) {
            InitializeDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5 , ItemDetails.COL_9, ItemDetails.COL_11 };
        String selection = ItemDetails.COL_3 + " LIKE ?";
        String[] selectionArgs = { "%" + searchQuery + "%" };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);
        SearchActivity.searchList.clear();
        while (cursor.moveToNext()) {

            SearchActivity.searchList.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_9)) + ", " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_11)) + "\n" + cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)) + "," + cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5)));
        }

        SortSearchList();
        cursor.close();
    }

    static MarkerOptions GetMarkerOptions(Context context, Cursor cursor) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)));
        markerOptions.snippet(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
        markerOptions.position(new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5))));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFromDrawable(context.getDrawable(R.drawable.swithoutshadow))));

        return markerOptions;
    }

    static void MoveMap(GoogleMap map, double latitude, double longitude, float zoomLevel, boolean animating) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(map.getCameraPosition().target, zoomLevel));

        VisibleRegion vr = map.getProjection().getVisibleRegion();
        double oneFifthMapSpan = (vr.latLngBounds.northeast.latitude - vr.latLngBounds.southwest.latitude) / 5.0;

        if (animating) {
            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude - oneFifthMapSpan, longitude)), 500, null);
        }
        else {
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude - oneFifthMapSpan, longitude)));
        }
    }

    static String UpdateText(Cursor cursor, TextView detailText, TextView titleText, ImageButton favoriteButton) {
        String favorited = "FALSE";
        if (cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_13)).equals("TRUE")) {
            favoriteButton.setImageResource(R.drawable.fullheart);
            favorited = "TRUE";
        }
        else {
            favoriteButton.setImageResource(R.drawable.greyheart);
        }
        detailText.setText(MessageFormat.format("Category: {0}\nReference Number: {1}\nDate added to register: {2}\nReported Street Address: {3}\nLocation: {4}, {5}\nCounty: {6}\nArchitects/Builders: {7}", cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_6)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_8)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_9)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_11)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_10)), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_12))));
        titleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)));
        MainActivity.photosLink = "https://npgallery.nps.gov/pdfhost/docs/NRHP/Photos/" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2))+ ".pdf";
        MainActivity.docsLink = "https://npgallery.nps.gov/pdfhost/docs/NRHP/Text/" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2))+ ".pdf";
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

    private static void InitializeDatabase(Context context) {

        ItemDatabase dbHelper = new ItemDatabase(context);

        try {
            dbHelper.updateDataBase();
            itemDatabase = dbHelper.getWritableDatabase();
        } catch (Exception exception) {
            throw new Error("UnableToUpdateDatabase");
        }

        databasesReady = true;
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //End Initialization related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Begin Other non-related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static void HideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view == null) view = new View(context);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static void SortSearchList() {
        for (String item: SearchActivity.searchList) {
            //item.split()
        }
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
