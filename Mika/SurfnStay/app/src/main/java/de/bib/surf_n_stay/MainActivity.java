package de.bib.surf_n_stay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_REQUEST_CODE = 1728813;
    private TextView tvData;
    private LocationManager locationManager;
    private static double SuchRadius = 12000.0;
    private ListView lvPlaces;
    private List<Angebot> angebote = new ArrayList<>();
    private SeekBar seekRadius;
    private TextView tvRadius;

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            if (angebote == null || angebote.isEmpty()) return;
            findPlacesInRadius(loc, angebote);
            sortPlacesByDistance(angebote);
            renderPlaces(angebote);
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

    private void findPlacesInRadius(Location currentLocation, List<Angebot> angebote) {
        for (Angebot angebot : angebote) {
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    angebot.lat, angebot.lon, results);
            float distanceInMeters = results[0];
            angebot.distanz = distanceInMeters;
        }
    }

    private void sortPlacesByDistance(List<Angebot> angebote) {
        for (int i = 0; i < angebote.size(); i++) {
            Angebot angebotI = angebote.get(i);
            for (int j = i + 1; j < angebote.size(); j++) {
                Angebot angebotJ = angebote.get(j);
                if (angebotI.distanz > angebotJ.distanz) {
                    angebote.set(i, angebotJ);
                    angebote.set(j, angebotI);
                }
            }
        }
    }

    private void renderPlaces(List<Angebot> angebote) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < angebote.size(); i++) {
            Angebot angebot = angebote.get(i);
            if (angebot.distanz > 0 && angebot.distanz <= SuchRadius) {
                sb.append("Adresse:                ").append(angebot.address).append("\n");
                sb.append("Distanz:                 ").append(String.format("%.2f", angebot.distanz)).append(" Meter\n");
                sb.append("Preis:                      ").append(angebot.price).append("\n");
                sb.append("Anzahl Betten:       ").append(angebot.beds).append("\n");
                sb.append("Hat Internet:           ").append(angebot.hasInternet ? "Ja" : "Nein").append("\n");
                sb.append("Haustiere erlaubt:  ").append(angebot.hasPets ? "Ja" : "Nein").append("\n");
                sb.append("Hat Saune:              ").append(angebot.hasSauna ? "Ja" : "Nein").append("\n");
                sb.append("Hat Kamin:              ").append(angebot.hasFireplace ? "Ja" : "Nein").append("\n");
                sb.append("Ist veröffentlicht:   ").append(angebot.isPublished ? "Ja" : "Nein").append("\n");
                sb.append("-----------------------------------------------\n\n");
            }
        }
        tvData.setText(sb.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        seekRadius = findViewById(R.id.seekRadius);
        tvRadius = findViewById(R.id.tvRadius);

        int startKm = 3;
        seekRadius.setProgress(startKm - 1);
        SuchRadius = startKm * 1000;
        tvRadius.setText("Radius: " + startKm + " km");

        seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radiusKm = progress + 1;          // 1–10 km
                SuchRadius = radiusKm * 1000.0;        // Meter
                tvRadius.setText("Radius: " + radiusKm + " km");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!angebote.isEmpty() && locationManager != null) {
                    Location lastLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation != null) {
                        findPlacesInRadius(lastLocation, angebote);
                        sortPlacesByDistance(angebote);
                        renderPlaces(angebote);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvData = findViewById(R.id.tvData);

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/offer");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String jsonString = s.hasNext() ? s.next() : "";
                s.close();
                conn.disconnect();

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Angebot>>() {}.getType();
                angebote = gson.fromJson(jsonString, listType);
                runOnUiThread(() -> {
                    Log.d("INFO", jsonString);
                    tvData.setText("Angebote geladen: " + angebote.size());
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        tvData.setText("Fehler: " + e.getMessage())
                );
            }
        }).start();

        startGPSStream();
    }
}
