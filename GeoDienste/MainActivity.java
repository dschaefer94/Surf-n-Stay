package de.bib.surf_n_stay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_REQUEST_CODE = 1728813;
    private TextView tvData;
    private LocationManager locationManager;
    private static double SuchRadius = 3000.0;


    private static final Angebot ANGEBOT1 = new Angebot(1, "Haus Am See 1", 0, 52.020885, 8.532471, "0€", 1, false, false, false, false, "", "");
    private static final Angebot ANGEBOT2 = new Angebot(2, "Oper 2", 0, 52.030885, 8.542471, "0€", 1, false, false, false, false, "", "");
    private static final Angebot ANGEBOT3 = new Angebot(3, "Unter der Brücke 3", 0, 64.040885, 16.552471, "0€", 1, false, false, false, false, "", "");
    private static final Angebot ANGEBOT4 = new Angebot(4, "HBF 4", 0, 52.050885, 8.562471, "0€", 1, false, false, false, false, "", "");
    private static final Angebot ANGEBOT5 = new Angebot(5, "Schwiegermutters Zuflucht 5", 0, 52.030885, 8.542470, "0€", 1, false, false, false, false, "", "");

    private static final Angebot[] ANGEBOTE_TEST = {ANGEBOT1, ANGEBOT2, ANGEBOT3, ANGEBOT4, ANGEBOT5};

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            findPlacesInRadius(loc);
            sortPlacesByDistance();
            renderPlaces();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    private void startGPSStream() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, LOCATION_REQUEST_CODE);
        }
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 1, locationListener);
    }

    private void findPlacesInRadius(Location currentLocation) {
        for (int i = 0; i < ANGEBOTE_TEST.length; i++) {
            float[] results = new float[ANGEBOTE_TEST.length];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    ANGEBOTE_TEST[i].lat, ANGEBOTE_TEST[i].lon, results);
            float distanceInMeters = results[0];
            if (distanceInMeters <= SuchRadius) {
                ANGEBOTE_TEST[i].distanz = distanceInMeters;
            }
        }
    }

    private void sortPlacesByDistance() {
        for (int i = 0; i < ANGEBOTE_TEST.length - 1; i++) {
            for (int j = i + 1; j < ANGEBOTE_TEST.length; j++) {
                if (ANGEBOTE_TEST[i].distanz > ANGEBOTE_TEST[j].distanz) {
                    Angebot temp = ANGEBOTE_TEST[i];
                    ANGEBOTE_TEST[i] = ANGEBOTE_TEST[j];
                    ANGEBOTE_TEST[j] = temp;
                }
            }
        }
    }
    private void renderPlaces() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ANGEBOTE_TEST.length; i++) {
            if (ANGEBOTE_TEST[i].distanz > 0) {
                sb.append("Adresse: ").append(ANGEBOTE_TEST[i].adresse).append("\n");
                sb.append("Distanz: ").append(String.format("%.2f", ANGEBOTE_TEST[i].distanz)).append(" Meter\n");
                sb.append("Preis: ").append(ANGEBOTE_TEST[i].preis).append("\n");
                sb.append("Anzahl Betten: ").append(ANGEBOTE_TEST[i].anzahlBetten).append("\n\n");
            }
        }
        tvData.setText(sb.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvData = findViewById(R.id.tvData);
        startGPSStream();
    }
}