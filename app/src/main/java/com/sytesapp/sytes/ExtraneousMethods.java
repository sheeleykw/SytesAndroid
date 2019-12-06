package com.sytesapp.sytes;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private static ObjectAnimator favoritesLeftAnimation;
    private static ObjectAnimator favoritesRightAnimation;
    private static ObjectAnimator settingsLeftAnimation;
    private static ObjectAnimator settingsRightAnimation;
    private static SpannableStringBuilder category;
    private static SpannableStringBuilder date;
    private static SpannableStringBuilder ref;
    private static SpannableStringBuilder street;
    private static SpannableStringBuilder location;
    private static SpannableStringBuilder county;
    private static SpannableStringBuilder builders;
    static AdView detailAdView;
    static boolean adsReady = false;
    static boolean animationsReady = false;
    private static int adWidth;
    private static AdRequest adRequest;
    private static ArrayList<AdView> listAds = new ArrayList<>();
    private static boolean wordsReady = false;


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Begin Map/Database related Methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static Cursor GetCursorFromRegion(VisibleRegion vr) {
        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_4 + " < ?  AND " + ItemDetails.COL_4 + " > ? AND " + ItemDetails.COL_5 + " < ? AND " + ItemDetails.COL_5 + " > ?";
        String[] selectionArgs = {  String.valueOf(vr.latLngBounds.northeast.latitude), String.valueOf(vr.latLngBounds.southwest.latitude), String.valueOf(vr.latLngBounds.northeast.longitude), String.valueOf(vr.latLngBounds.southwest.longitude) };

        return itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    static Cursor GetCursorFromId(String id) {
        String[] projection = { ItemDetails.COL_2, ItemDetails.COL_3, ItemDetails.COL_6, ItemDetails.COL_7, ItemDetails.COL_8, ItemDetails.COL_9, ItemDetails.COL_10, ItemDetails.COL_11, ItemDetails.COL_12, ItemDetails.COL_13 };
        String selection = ItemDetails.COL_1 + " = ?";
        String[] selectionArgs = { id };

        return itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    static LatLng GetLatLngFromId(String id) {
        if (id.equals("0")) {
            return new LatLng(Double.valueOf(SearchActivity.selectedPosition.split(",")[0]), Double.valueOf(SearchActivity.selectedPosition.split(",")[1]));
        }
        else {
            String[] projection = { ItemDetails.COL_4, ItemDetails.COL_5 };
            String selection = ItemDetails.COL_1 + " = ?";
            String[] selectionArgs = { id };
            Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            cursor.moveToNext();

            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4));
            double longi = cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5));

            cursor.close();
            return new LatLng(lat, longi);
        }
    }

    static String GetIdFromCity(String city, String state) {
        String[] projection = { "ID" };
        String selection = ItemDetails.COL_3 + " LIKE ? AND " + "StateName" + " LIKE ?";
        String[] selectionArgs = { city, state };
        Cursor cursor = cityDatabase.query( "cities", projection, selection, selectionArgs, null, null, null);
        cursor.moveToNext();

        String id = cursor.getString(cursor.getColumnIndexOrThrow("ID"));

        cursor.close();
        return id;
    }

    static String[] GetCityFromId(String id) {
        String[] projection = { ItemDetails.COL_3, "StateName", ItemDetails.COL_4, ItemDetails.COL_5 };
        String selection = "ID" + " LIKE ?";
        String[] selectionArgs = { id };
        Cursor cursor = cityDatabase.query( "cities", projection, selection, selectionArgs, null, null, null);
        cursor.moveToNext();

        String city = cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3));
        String state = cursor.getString(cursor.getColumnIndexOrThrow("StateName"));
        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_4));
        double longi = cursor.getDouble(cursor.getColumnIndexOrThrow(ItemDetails.COL_5));

        String[] returnArray = {city, state, String.valueOf(lat), String.valueOf(longi)};

        cursor.close();
        return returnArray;
    }

    static void GetFavorited() {
        String[] projection = { ItemDetails.COL_1, ItemDetails.COL_3, ItemDetails.COL_7 };
        String selection = ItemDetails.COL_13 + " = ?";
        String[] selectionArgs = { "TRUE" };

        Cursor cursor = itemDatabase.query( ItemDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, ItemDetails.COL_3);

        MainActivity.currentFavorites.clear();
        while (cursor.moveToNext()) {
            MainActivity.currentFavorites.add(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_1)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
            int spaceBetweenAds = 5;
            if (((MainActivity.currentFavorites.size() + 1) % spaceBetweenAds) == 0) {
                MainActivity.currentFavorites.add("Ad\n______\nnull");
            }
        }

        cursor.close();
    }

    static void GetSearched(String searchQuery) {
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

    static void GetCitySearched(String searchQuery) {
        String[] cityProjection = { ItemDetails.COL_3, ItemDetails.COL_4, ItemDetails.COL_5, "StateName", "NumOfPoints" };
        String selection = ItemDetails.COL_3 + " LIKE ?";
        String[] selectionArgs = { "%" + searchQuery + "%" };

        Cursor cursor = cityDatabase.query( "cities", cityProjection, selection, selectionArgs, null, null, ItemDetails.COL_3);

        SearchActivity.searchList.clear();
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

        if (wordsReady) {
            category.replace(10, category.length(), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_7)));
            date.replace(24, date.length(), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_6)));
            ref.replace(18, ref.length(), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_2)));
            street.replace(25, street.length(), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_8)));
            location.replace(10, location.length(), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_9)) + ", " + cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_11)));
            county.replace(8, county.length(), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_10)));
            builders.replace(21, builders.length(), cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_12)));
        }

        titleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemDetails.COL_3)));
        categoryText.setText(category);
        dateText.setText(date);
        refText.setText(ref);
        streetText.setText(street);
        locationText.setText(location);
        countyText.setText(county);
        buildersText.setText(builders);

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
            Objects.requireNonNull(markerHashMap.get(removal)).remove();
            markerHashMap.remove(removal);
        }
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //End Map/Database related Methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Begin Initialization related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static void InitializeAnimations(Context context, TableLayout detailView, RelativeLayout titleView, RelativeLayout favoritesView, LinearLayout settingsView) {
        detailUpAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 0);
        detailUpAnimation.setDuration(500);
        detailDownAnimation = ObjectAnimator.ofFloat(detailView, "translationY", 500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        detailDownAnimation.setDuration(350);

        titleDownAnimation = ObjectAnimator.ofFloat(titleView, "translationY", 0);
        titleDownAnimation.setDuration(500);
        titleUpAnimation = ObjectAnimator.ofFloat(titleView, "translationY", -500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        titleUpAnimation.setDuration(350);

        favoritesRightAnimation = ObjectAnimator.ofFloat(favoritesView, "translationX", 0);
        favoritesRightAnimation.setDuration(500);
        favoritesLeftAnimation = ObjectAnimator.ofFloat(favoritesView, "translationX", -500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        favoritesLeftAnimation.setDuration(500);

        settingsLeftAnimation = ObjectAnimator.ofFloat(settingsView, "translationX", 0);
        settingsLeftAnimation.setDuration(500);
        settingsRightAnimation = ObjectAnimator.ofFloat(settingsView, "translationX", 500 * context.getApplicationContext().getResources().getDisplayMetrics().density);
        settingsRightAnimation.setDuration(500);

        animationsReady = true;
    }

    public static class InitializeDatabases extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... contexts) {
            ItemDatabase dbHelper = new ItemDatabase(contexts[0]);

            try {
                dbHelper.updateDataBase();
                itemDatabase = dbHelper.getWritableDatabase();
            } catch (Exception exception) {
                throw new Error("UnableToUpdateDatabase");
            }

            CityDatabase dbHelper2 = new CityDatabase(contexts[0]);

            try {
                dbHelper2.updateDataBase();
                cityDatabase = dbHelper2.getWritableDatabase();
            } catch (Exception exception) {
                throw new Error("UnableToUpdateDatabase");
            }

            category = new SpannableStringBuilder("CATEGORY:\n");
            category.setSpan(new UnderlineSpan(),0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            category.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            category.setSpan(new RelativeSizeSpan(1.2f),0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            date = new SpannableStringBuilder("DATE ADDED TO REGISTER:\n");
            date.setSpan(new UnderlineSpan(),0, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            date.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            date.setSpan(new RelativeSizeSpan(1.2f),0, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ref = new SpannableStringBuilder("REFERENCE NUMBER:\n");
            ref.setSpan(new UnderlineSpan(),0, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ref.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ref.setSpan(new RelativeSizeSpan(1.2f),0, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            street = new SpannableStringBuilder("REPORTED STREET ADDRESS:\n");
            street.setSpan(new UnderlineSpan(),0, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            street.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            street.setSpan(new RelativeSizeSpan(1.2f),0, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            location = new SpannableStringBuilder("LOCATION:\n");
            location.setSpan(new UnderlineSpan(),0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            location.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            location.setSpan(new RelativeSizeSpan(1.2f),0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            county = new SpannableStringBuilder("COUNTY:\n");
            county.setSpan(new UnderlineSpan(),0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            county.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            county.setSpan(new RelativeSizeSpan(1.2f),0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            builders = new SpannableStringBuilder("ARCHITECTS/BUILDERS:\n");
            builders.setSpan(new UnderlineSpan(),0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builders.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builders.setSpan(new RelativeSizeSpan(1.2f),0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            wordsReady = true;

            return null;
        }
    }

    public static class InitializeAds extends AsyncTask<Context, Void, Context> {
        @Override
        protected Context doInBackground(Context... contexts) {
            MobileAds.initialize(contexts[0], new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            float widthPixels = contexts[0].getApplicationContext().getResources().getDisplayMetrics().widthPixels;
            float density = contexts[0].getApplicationContext().getResources().getDisplayMetrics().density;
            adWidth = (int) (widthPixels / density);

            adRequest = new AdRequest.Builder().addTestDevice("481D9EB0E450EFE1F74321C81D584BCE").build();

            return contexts[0];
        }

        @Override
        protected void onPostExecute(Context context) {
            detailAdView = new AdView(context);
            detailAdView.setAdUnitId("ca-app-pub-3281339494640251/9986601233");

            detailAdView.setAdSize(AdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, adWidth));
            detailAdView.loadAd(adRequest);

            adsReady = true;

            for (int i = 0; i < 9; i ++) {
                AdView listAdView = new AdView(context);
                listAdView.setAdUnitId("ca-app-pub-3281339494640251/4734274558");

                listAdView.setAdSize(AdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, adWidth - 16));
                listAdView.loadAd(adRequest);
                listAds.add(listAdView);
            }
        }
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

    static void AddListAdToFrame(FrameLayout frame, int listPosition) {
        int currentAd = ((listPosition - 4) / 5) % 9;
        if (frame.getChildCount() == 0) {
            if (listAds.get(currentAd).getParent() != null) {
                if (listAds.get(8 - currentAd).getParent() != null) {
                    ((FrameLayout)listAds.get(8 - currentAd).getParent()).removeAllViews();
                }
                frame.addView(listAds.get(8 - currentAd));
            }
            else {
                frame.addView(listAds.get(currentAd));
            }
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

    static void ChangeView(String previousView, String currentView) {
        switch (previousView) {
            case "Settings":
                settingsRightAnimation.start();
                break;
            case "Favorites":
                favoritesLeftAnimation.start();
                break;
        }
        switch (currentView) {
            case "Favorites":
                favoritesRightAnimation.start();
                break;
            case "Settings":
                settingsLeftAnimation.start();
                break;
        }
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //End Other non-related methods
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
}
