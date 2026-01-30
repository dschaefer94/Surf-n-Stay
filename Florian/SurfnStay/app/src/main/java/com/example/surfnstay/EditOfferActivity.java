package com.example.surfnstay;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.surfnstay.databinding.ActivityEditOfferBinding;
import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditOfferActivity extends AppCompatActivity {

    private ActivityEditOfferBinding binding;
    private Offer currentOffer; // Das Angebot, das wir bearbeiten (null wenn neu)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Server URL
    private static final String SERVER_URL = "http://10.0.2.2:8080/offers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditOfferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Prüfen: Haben wir ein Angebot übergeben bekommen?
        if (getIntent().hasExtra("offer_data")) {
            currentOffer = (Offer) getIntent().getSerializableExtra("offer_data");
            fillFields();
            binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            // Neues Angebot erstellen
            currentOffer = new Offer();
            currentOffer.ownerId = 1; // Wir sind immer User 1
            binding.btnDelete.setVisibility(View.GONE);
        }

        // 2. Buttons verknüpfen
        binding.btnSave.setOnClickListener(v -> saveOffer());
        binding.btnDelete.setOnClickListener(v -> deleteOffer());
    }

    private void fillFields() {
        binding.etAddress.setText(currentOffer.address);
        binding.etPrice.setText(currentOffer.price);
        binding.etBeds.setText(String.valueOf(currentOffer.beds));
        binding.cbSauna.setChecked(currentOffer.hasSauna == true);
        binding.cbFireplace.setChecked(currentOffer.hasFireplace == true);
        binding.cbPets.setChecked(currentOffer.hasPets == true);
        binding.cbInternet.setChecked(currentOffer.hasInternet == true);
        binding.switchPublished.setChecked(currentOffer.isPublished == true);
    }

    private void saveOffer() {
        // UI -> Objekt
        String addressInput = binding.etAddress.getText().toString().trim(); // nur fürs Geocoding
        currentOffer.price = binding.etPrice.getText().toString();

        try {
            currentOffer.beds = Integer.parseInt(binding.etBeds.getText().toString());
        } catch (NumberFormatException e) {
            currentOffer.beds = 1;
        }

        currentOffer.hasSauna = binding.cbSauna.isChecked();
        currentOffer.hasFireplace = binding.cbFireplace.isChecked();
        currentOffer.hasPets = binding.cbPets.isChecked();
        currentOffer.hasInternet = binding.cbInternet.isChecked();
        currentOffer.isPublished = binding.switchPublished.isChecked();

        // Wenn du NUR Koordinaten senden willst: hier geocoden
        if (!addressInput.isEmpty()) {
            geocodeAddressAndSend(addressInput);
        } else {
            // Wenn keine Eingabe: entweder Fehler oder Default
            Toast.makeText(this, "Bitte Ort/Adresse eingeben", Toast.LENGTH_SHORT).show();
        }
    }

    private void geocodeAddressAndSend(String addressInput) {
        executor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(addressInput, 1);

                if (results == null || results.isEmpty()) {
                    handler.post(() ->
                            Toast.makeText(this, "Adresse nicht gefunden", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                Address a = results.get(0);
                currentOffer.lat = a.getLatitude();
                currentOffer.lon = a.getLongitude();

                // Jetzt erst senden
                sendRequest(currentOffer.id == 0 ? "POST" : "PUT");

            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() ->
                        Toast.makeText(this, "Geocoding fehlgeschlagen", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    private void deleteOffer() {
        if (currentOffer.id != 0) {
            sendRequest("DELETE");
        }
    }

    private void sendRequest(String method) {
        executor.execute(() -> {
            try {
                String urlString = SERVER_URL;
                // Bei DELETE und PUT hängen wir oft die ID an die URL oder senden sie im Body
                // Unser Server DELETE Handler erwartet "id=" im Query String:
                if (method.equals("DELETE")) {
                    urlString += "?id=" + currentOffer.id;
                }

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                // Body senden (nur bei POST und PUT)
                if (!method.equals("DELETE")) {
                    conn.setDoOutput(true);
                    String jsonInput = new Gson().toJson(currentOffer);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int code = conn.getResponseCode();
                handler.post(() -> {
                    if (code >= 200 && code < 300) {
                        Toast.makeText(this, "Erfolg!", Toast.LENGTH_SHORT).show();
                        finish(); // Activity schließen und zurück zur Liste
                    } else {
                        Toast.makeText(this, "Fehler: " + code, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Verbindungsfehler", Toast.LENGTH_SHORT).show());
            }
        });
    }
}