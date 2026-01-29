// java
package de.bib.surf_n_stay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
    private EditText etAddress;
    private Button btnSearchAddress, btnUseCurrentLocation;
    private Location activeLocation;


    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            activeLocation=loc;
            findPlacesInRadius(activeLocation, angebote);
            sortPlacesByDistance(angebote);
            renderPlaces(angebote);
            // log distances when location updates arrive
            for (int i = 0; i < angebote.size(); i++) {
                Log.d("INFO", "Angebot " + i + ": " + angebote.get(i).distanz);
            }
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
            return;
        }
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locationManager == null) return;
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            Log.e("PERM", "requestLocationUpdates failed: " + e.getMessage());
        }
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
        Collections.sort(angebote, (a, b) -> Double.compare(a.distanz, b.distanz));
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
                sb.append("Hat Internet:            ").append(angebot.hasInternet ? "Ja" : "Nein").append("\n");
                sb.append("Haustiere erlaubt:  ").append(angebot.hasPets ? "Ja" : "Nein").append("\n");
                sb.append("Hat Saune:              ").append(angebot.hasSauna ? "Ja" : "Nein").append("\n");
                sb.append("Hat Kamin:              ").append(angebot.hasFireplace ? "Ja" : "Nein").append("\n");
                sb.append("Ist veröffentlicht:    ").append(angebot.isPublished ? "Ja" : "Nein").append("\n");
                sb.append("-----------------------------------------------\n\n");
            }
        }
        tvData.setText(sb.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activeLocation=null;
                startGPSStream();
                // update UI with last known location if available
                if (locationManager == null) {
                    locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
                }
                try {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && locationManager != null) {
                        Location activeLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (activeLocation != null && angebote != null && !angebote.isEmpty()) {
                            findPlacesInRadius(activeLocation, angebote);
                            sortPlacesByDistance(angebote);
                            renderPlaces(angebote);
                        }
                    }
                } catch (SecurityException e) {
                    Log.e("PERM", "getActiveLocation failed: " + e.getMessage());
                }
            } else {
                Log.w("PERM", "Location permission denied");
            }
        }
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
                int radiusKm = progress + 1;
                SuchRadius = radiusKm * 1000.0;
                tvRadius.setText("Radius: " + radiusKm + " km");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!angebote.isEmpty() && locationManager != null) {
                    try {
                        if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (activeLocation != null&&!angebote.isEmpty()) {
                                findPlacesInRadius(activeLocation, angebote);
                                sortPlacesByDistance(angebote);
                                renderPlaces(angebote);
                            }
                        } else {
                            Log.w("PERM", "onStopTrackingTouch: no location permission");
                        }
                    } catch (SecurityException e) {
                        Log.e("PERM", "getLastKnownLocation failed: " + e.getMessage());
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
        etAddress = findViewById(R.id.etAddress);
        btnSearchAddress = findViewById(R.id.btnSearchAddress);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);


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
                    Log.d("INFO", "JSON:  " + jsonString);
                    tvData.setText("Angebote geladen: " + (angebote != null ? angebote.size() : 0));

                    if (locationManager == null) {
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    }
                    try {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && locationManager != null && angebote != null && !angebote.isEmpty()) {
                            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (lastLocation != null) {
                                findPlacesInRadius(lastLocation, angebote);
                                sortPlacesByDistance(angebote);
                                renderPlaces(angebote);

                                // Log Distanzen nach Berechnung
                                for (int i = 0; i < angebote.size(); i++) {
                                    Log.d("INFO", "Angebot " + i + ": " + angebote.get(i).distanz);
                                }
                            }
                        }
                    } catch (SecurityException e) {
                        Log.e("PERM", "getLastKnownLocation failed: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        tvData.setText("Fehler: " + e.getMessage())
                );
            }
        }).start();

        btnSearchAddress.setOnClickListener(v -> {
            stopGPSStream();
            String addressText = etAddress.getText().toString().trim();
            if (addressText.isEmpty()) return;

            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> results = geocoder.getFromLocationName(addressText, 1);

                    if (results != null && !results.isEmpty()) {
                        Address address = results.get(0);
                        double lat = address.getLatitude();
                        double lon = address.getLongitude();

                        runOnUiThread(() -> {
                            Location fakeLocation = new Location("GEOCODER");
                            fakeLocation.setLatitude(lat);
                            fakeLocation.setLongitude(lon);

                            activeLocation=fakeLocation;
                            findPlacesInRadius(activeLocation, angebote);
                            sortPlacesByDistance(angebote);
                            renderPlaces(angebote);
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() ->
                            tvData.setText("Adresse nicht gefunden")
                    );
                }
            }).start();
        });
        btnUseCurrentLocation.setOnClickListener(v -> {
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }

            try {
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc != null) {
                    findPlacesInRadius(loc, angebote);
                    sortPlacesByDistance(angebote);
                    renderPlaces(angebote);
                }
            } catch (SecurityException e) {
                Log.e("PERM", e.getMessage());
            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();
        stopGPSStream();
    }

    private void stopGPSStream() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
