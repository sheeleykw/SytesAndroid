package com.sytesapp.sytes;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.collect.BiMap;

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
    private static boolean animationsReady = false;

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

    static Cursor GetCursorFromFavorited(Context context) {
        if (!databasesReady) {
            InitializeDatabase(context);
        }

        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_13 + " = ?";
        String[] selectionArgs = { "TRUE" };

        return itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);
    }

    static MarkerOptions GetMarkerOptions(Context context, Cursor cursor) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)));
        markerOptions.snippet(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
        markerOptions.position(new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4)), cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5))));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFromDrawable(context.getDrawable(R.drawable.swithoutshadow))));

        return markerOptions;
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

    private static void InitializeAnimations(Context context, TableLayout detailView, TextView titleView) {
        detailUpAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 0);
        detailUpAnimation.setDuration(600);
        detailDownAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        detailUpAnimation.setDuration(400);

        titleDownAnimation = ObjectAnimator.ofFloat(titleView, "translationY", 0);
        titleDownAnimation.setDuration(400);
        titleUpAnimation = ObjectAnimator.ofFloat(titleView, "translationY", -500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        titleUpAnimation.setDuration(400);

        animationsReady = true;
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

    private static Bitmap BitmapFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    static void DisplayViews(Context context, TableLayout detailView, TextView titleView) {
        if (!animationsReady) {
            InitializeAnimations(context, detailView, titleView);
        }

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
