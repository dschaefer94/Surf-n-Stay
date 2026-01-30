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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
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

    private LocationManager locationManager;
    private static double SuchRadius = 12000.0;
    private List<Angebot> alleAngebote = new ArrayList<>();
    private List<Angebot> angebote = new ArrayList<>();

    private SeekBar seekRadius;
    private TextView tvRadius;
    private EditText etAddress;
    private Button btnSearchAddress, btnUseCurrentLocation;
    private Location activeLocation;
    private RecyclerView rvOffers;
    private OfferAdapter offerAdapter;



    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            activeLocation = loc;
            updateDistances();
            filterByRadius();
            sortPlacesByDistance(angebote);
            offerAdapter.setAngebote(angebote);
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

    private void updateDistances() {
        if (activeLocation == null || alleAngebote == null) return;

        for (Angebot a : alleAngebote) {
            float[] results = new float[1];
            Location.distanceBetween(
                    activeLocation.getLatitude(),
                    activeLocation.getLongitude(),
                    a.lat,
                    a.lon,
                    results
            );

            a.distanz = results[0] > 0 ? results[0] : Double.MAX_VALUE;
        }
    }


    private void filterByRadius() {
        if (alleAngebote == null) return;

        angebote.clear();

        for (Angebot a : alleAngebote) {
            if (a.distanz > 0 && a.distanz <= SuchRadius) {
                angebote.add(a);
            }
        }
    }



    private void sortPlacesByDistance(List<Angebot> angebote) {
        Collections.sort(angebote, (a, b) -> {
            if (a.distanz < 0) return 1;
            if (b.distanz < 0) return -1;
            return Double.compare(a.distanz, b.distanz);
        });

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
                        activeLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (activeLocation != null && angebote != null && !angebote.isEmpty()) {
                            updateDistances();
                            filterByRadius();
                            sortPlacesByDistance(angebote);
                            offerAdapter.setAngebote(angebote);
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
        rvOffers = findViewById(R.id.rvOffers);
        rvOffers.setLayoutManager(new LinearLayoutManager(this));

        offerAdapter = new OfferAdapter(angebote);
        rvOffers.setAdapter(offerAdapter);

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
                filterByRadius();
                sortPlacesByDistance(angebote);
                offerAdapter.setAngebote(angebote);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        etAddress = findViewById(R.id.etAddress);
        btnSearchAddress = findViewById(R.id.btnSearchAddress);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);


        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/offers");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String jsonString = s.hasNext() ? s.next() : "";
                s.close();
                conn.disconnect();

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Angebot>>() {}.getType();
                List<Angebot> loaded = gson.fromJson(jsonString, listType);

                runOnUiThread(() -> {
                    alleAngebote.clear();
                    if (loaded != null) alleAngebote.addAll(loaded);

                    angebote.clear();
                    angebote.addAll(alleAngebote);

                    offerAdapter.setAngebote(angebote);

                    if (activeLocation != null) {
                        updateDistances();
                        filterByRadius();
                        sortPlacesByDistance(angebote);
                        offerAdapter.setAngebote(angebote);
                    }
                });


                runOnUiThread(() -> {
                    Log.d("INFO", "JSON:  " + jsonString);
                    if (locationManager == null) {
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    }
                    try {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && locationManager != null && angebote != null && !angebote.isEmpty()) {
                            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (lastLocation != null) {
                                activeLocation= lastLocation;
                                updateDistances();
                                filterByRadius();
                                sortPlacesByDistance(angebote);
                                offerAdapter.setAngebote(angebote);
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
                        Log.d("INFO", "Fehler beim Laden der Angebote: " + e.getMessage())
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

                            activeLocation = fakeLocation;
                            updateDistances();
                            filterByRadius();
                            sortPlacesByDistance(angebote);
                            offerAdapter.setAngebote(angebote);
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Log.d("INFO", "Geocoding-Fehler: " + e.getMessage())
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
                    activeLocation = loc;
                    updateDistances();
                    filterByRadius();
                    sortPlacesByDistance(angebote);
                    offerAdapter.setAngebote(angebote);
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
