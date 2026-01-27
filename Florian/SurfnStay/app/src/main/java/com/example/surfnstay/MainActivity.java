package com.example.surfnstay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.surfnstay.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OfferAdapter.OnOfferClickListener {

    private ActivityMainBinding binding;
    private OfferAdapter adapter;

    // Netzwerk im Hintergrund ausführen
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Server URL für den Android Emulator (10.0.2.2 = localhost des PCs)
    private static final String SERVER_URL = "http://10.0.2.2:8080/offers";

    // Simuliert: Wir sind User mit der ID 1
    private static final String MY_USER_ID = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ViewBinding initialisieren
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. RecyclerView (Liste) einrichten
        adapter = new OfferAdapter(this);
        binding.recyclerOffers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerOffers.setAdapter(adapter);

        // 3. Modus-Umschalter (Surfer vs. Anbieter)
        binding.toggleGroupMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return; // Verhindert Doppelklicks

            if (checkedId == binding.btnModeHost.getId()) {
                setupHostMode();
            } else {
                setupSurferMode();
            }
        });

        // 4. Button: Suchen (Trigger)
        binding.btnSearchTrigger.setOnClickListener(v -> {
            String address = binding.etSearchAddress.getText().toString();
            float radius = binding.sliderRadius.getValue();
            // Hier würde man normalerweise Geocoding machen (Adresse -> Koordinaten)
            // Für den Prototyp laden wir erstmal alle und filtern optional
            loadOffersFromServer(null);
            Toast.makeText(this, "Suche gestartet...", Toast.LENGTH_SHORT).show();
        });

        // 5. Button: GPS nutzen
        binding.btnGpsSearch.setOnClickListener(v -> checkGpsPermissionAndSearch());

        binding.fabAddOffer.setOnClickListener(v -> {
            // Neues Angebot -> Leerer Intent
            Intent intent = new Intent(MainActivity.this, EditOfferActivity.class);
            startActivity(intent);
        });

        // Start: Standardmäßig Surfer-Modus laden
        binding.toggleGroupMode.check(binding.btnModeSurfer.getId());
    }

    // --- UI LOGIK ---

    private void setupSurferMode() {
        // Suchleiste zeigen, FAB verstecken
        binding.searchContainer.setVisibility(View.VISIBLE);
        binding.fabAddOffer.setVisibility(View.GONE);

        // Alle Angebote laden
        loadOffersFromServer(null);
    }

    private void setupHostMode() {
        // Suchleiste verstecken, FAB zeigen
        binding.searchContainer.setVisibility(View.GONE);
        binding.fabAddOffer.setVisibility(View.VISIBLE);

        // Nur MEINE Angebote laden (Filterung via Server Parameter)
        loadOffersFromServer("owner_id=" + MY_USER_ID);
    }

    // --- NETZWERK LOGIK (HTTP REQUEST) ---

    private void loadOffersFromServer(String queryParams) {
        binding.progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                // URL bauen (z.B. http://10.0.2.2:8080/offers?owner_id=1)
                String urlString = SERVER_URL;
                if (queryParams != null && !queryParams.isEmpty()) {
                    urlString += "?" + queryParams;
                }

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000); // 5 Sekunden Timeout

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // Antwort lesen
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // JSON parsen mit GSON
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<Offer>>(){}.getType();
                    List<Offer> offers = gson.fromJson(result.toString(), listType);

                    // Zurück zum UI-Thread wechseln um Liste zu füllen
                    handler.post(() -> {
                        adapter.setOffers(offers);
                        binding.progressBar.setVisibility(View.GONE);
                        if (offers.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Keine Angebote gefunden.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    handler.post(() -> {
                        Toast.makeText(MainActivity.this, "Server Fehler: " + responseCode, Toast.LENGTH_LONG).show();
                        binding.progressBar.setVisibility(View.GONE);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, "Verbindungsfehler! Läuft der Server?", Toast.LENGTH_LONG).show();
                    Log.e("Network", "Error", e);
                    binding.progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    // --- KLICK EVENTS AUF LISTE ---

    @Override
    public void onOfferClick(Offer offer) {
        // Prüfen, ob wir im Host-Modus sind
        if (binding.toggleGroupMode.getCheckedButtonId() == binding.btnModeHost.getId()) {
            // Bearbeiten -> Objekt übergeben
            Intent intent = new Intent(this, EditOfferActivity.class);
            intent.putExtra("offer_data", offer);
            startActivity(intent);
        } else {
            // Im Surfer Modus: Nur Info anzeigen
            Toast.makeText(this, "Buchung für " + offer.price, Toast.LENGTH_SHORT).show();
        }
    }

    // --- GPS / BERECHTIGUNGEN ---

    private void checkGpsPermissionAndSearch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Nach Erlaubnis fragen
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            // Wir haben die Erlaubnis -> Standort holen
            // (Hier vereinfacht als Toast, echte Implementierung bräuchte LocationManager)
            Toast.makeText(this, "GPS Position wird ermittelt...", Toast.LENGTH_SHORT).show();

            // Dummy Aufruf: Suche in der Nähe von Koordinaten (Hvide Sande)
            loadOffersFromServer("lat=56.0&lon=8.1&radius=" + binding.sliderRadius.getValue());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkGpsPermissionAndSearch();
        } else {
            Toast.makeText(this, "Ohne GPS keine Umkreissuche.", Toast.LENGTH_SHORT).show();
        }
    }
}