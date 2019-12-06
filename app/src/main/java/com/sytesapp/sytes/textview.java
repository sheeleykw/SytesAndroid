package com.sytesapp.sytes;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class textview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);
        TextView text = findViewById(R.id.textField);
        TextView header = findViewById(R.id.headerField);
        if (Objects.equals(this.getIntent().getStringExtra("text"), "Policy")) {
            header.setText("Privacy Policy");
            text.setText(Html.fromHtml("<p><b>USER LOCATION</b></p>" +
                    "<p>User Location is only initialized and requested from the user at the start of the app and the app will not use the location while it is running in the background. The user location is never monitored by Sytes Inc, nor is it stored in any of the local databases. The locational access can be turned on and off at any given time while using the Sytes App. Sytes Inc has no access to users past, current, or future locations, and therefore can not and will not share said information with anyone.</p>" +
                    "<p><b>PERSONAL DATA</b></p>" +
                    "<p>We do not obtain, or ask for any personal data such as email, home address, names, or phone numbers. Sytes App is purely a locational and educational service, and uses only the user’s location to improve the app experience. The location is used to start the map in the general vicinity of the user and to sort points in proximity to user while performing a search.</p>"));
        }
        else if (Objects.equals(this.getIntent().getStringExtra("text"), "Copyright")) {
            header.setText("Copyright");
            text.setText(Html.fromHtml("<p><b>PHOTOS</b></p>" +
                    "<p>Sytes Inc does not own any photos displayed from within the app. The Sytes App is purely a locational application with a mobile PDF (Portable Document Format) display. Sytes Inc created a mobile viewer that displays the National Park Service’s website for viewing any of the photos and documents shown within the app. All images are respectfully owned by whomever took said photo, and or the respected state to which is home to these historic properties. Sytes Inc does not make money while any images are being displayed to a user. The viewing and displaying of said photos within the mobile PDF viewer are free from revenue to Sytes Inc.</p>" +
                    "<p><b>DOCUMENTS</b></p>" +
                    "<p>Sytes Inc does not own any documents displayed from within the app. The Sytes App is purely a locational application with a mobile PDF (Portable Document Format) display. Sytes Inc created a mobile viewer that displays the National Park Service’s website for viewing any of the photos and documents shown within the app. All written descriptions and documents are respectfully owned by whomever wrote said document, and or the respected state to which is home to these historic properties. Sytes Inc does not make money while any documents are being displayed to a user. The viewing and displaying of said documents within the mobile PDF viewer are free from revenue to Sytes Inc.</p>"));
        }
        else if (Objects.equals(this.getIntent().getStringExtra("text"), "Questions")) {
            header.setText("Frequently Asked Questions");
            text.setText(Html.fromHtml("<p><b>Q.</b> Why are the photos and documents not loading?</p><p><b>A.</b> Depending on the current connection to either your wifi or your digital network, the speed at which the photos and documents load can be slow. Another answer might be that the access to the photos and documents is not currently available. The photos and documents are not controlled by Sytes Inc and therefore we do not have control over how or when the servers holding the photos and documents will be functional.</p><p><b>Q.</b> Why does the app take so long to startup (Paid/Free versions)?</p><p><b>A.</b> Because the free version of the app contains ads, which are obtained from Google’s Admob API, sometimes the requesting and loading of these ads can severely slow down startup time and overall performance. If you have the paid version of the app and experiencing startup times that are unsatisfactory to you, please contact us at leve1incorp@gmail.com. Please include a description of the specific areas where you experience performance issues.</p>"));
        }
    }
}
