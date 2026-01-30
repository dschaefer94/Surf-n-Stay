package com.example.surfnstay;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.surfnstay.databinding.ActivityEditOfferBinding;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditOfferActivity extends AppCompatActivity {

    private ActivityEditOfferBinding binding;
    private Offer currentOffer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String SERVER_URL = "http://10.0.2.2:8080/offers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditOfferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("offer_data")) {
            currentOffer = (Offer) getIntent().getSerializableExtra("offer_data");
            fillFields();
            binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            currentOffer = new Offer();
            currentOffer.ownerId = 1; // fix: User 1
            binding.btnDelete.setVisibility(View.GONE);
        }

        // Speichern/Absenden: immer in DB schreiben (POST bei neu, PUT bei edit)
        binding.btnSave.setOnClickListener(v -> saveOfferAndSend());

        // Löschen
        binding.btnDelete.setOnClickListener(v -> deleteOffer());
    }

    private void fillFields() {
        // Adresse ist nur UI-Hilfsfeld, NICHT Backend-Feld
        binding.etAddress.setText(currentOffer.address != null ? currentOffer.address : "");

        binding.etPrice.setText(currentOffer.price != null ? currentOffer.price : "");
        binding.etBeds.setText(String.valueOf(currentOffer.beds));

        binding.cbSauna.setChecked(currentOffer.hasSauna);
        binding.cbFireplace.setChecked(currentOffer.hasFireplace);
        binding.cbPets.setChecked(currentOffer.hasPets);
        binding.cbInternet.setChecked(currentOffer.hasInternet);
        binding.switchPublished.setChecked(currentOffer.isPublished);
    }

    private void saveOfferAndSend() {
        // 1) UI -> Objekt
        String addressText = binding.etAddress.getText().toString().trim();
        if (addressText.isEmpty()) {
            Toast.makeText(this, "Bitte Adresse eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        currentOffer.address = addressText; // nur fürs UI/Geocoding

        currentOffer.price = binding.etPrice.getText().toString().trim();
        try {
            currentOffer.beds = Integer.parseInt(binding.etBeds.getText().toString().trim());
        } catch (NumberFormatException e) {
            currentOffer.beds = 1;
        }

        currentOffer.hasSauna = binding.cbSauna.isChecked();
        currentOffer.hasFireplace = binding.cbFireplace.isChecked();
        currentOffer.hasPets = binding.cbPets.isChecked();
        currentOffer.hasInternet = binding.cbInternet.isChecked();
        currentOffer.isPublished = binding.switchPublished.isChecked(); // darf false sein

        // 2) Adresse -> lat/lon -> dann senden
        String method = (currentOffer.id == 0) ? "POST" : "PUT";
        geocodeThenSend(addressText, method);
    }

    private void geocodeThenSend(String addressText, String method) {
        executor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(addressText, 1);

                if (results == null || results.isEmpty()) {
                    handler.post(() ->
                            Toast.makeText(this, "Adresse nicht gefunden", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                Address a = results.get(0);
                currentOffer.lat = a.getLatitude();
                currentOffer.lon = a.getLongitude();

                // jetzt wirklich senden
                sendRequest(method);

            } catch (IOException e) {
                handler.post(() ->
                        Toast.makeText(this, "Geocoding fehlgeschlagen (Netz?)", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() ->
                        Toast.makeText(this, "Geocoding Fehler", Toast.LENGTH_SHORT).show()
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
            HttpURLConnection conn = null;
            try {
                String urlString = SERVER_URL;

                if (method.equals("DELETE")) {
                    urlString += "?id=" + currentOffer.id;
                }

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept", "application/json");

                if (!method.equals("DELETE")) {
                    conn.setDoOutput(true);
                    String jsonInput = new Gson().toJson(currentOffer);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
                    }
                }

                int code = conn.getResponseCode();
                handler.post(() -> {
                    if (code >= 200 && code < 300) {
                        Toast.makeText(this, "Gespeichert ✅", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Fehler: " + code, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() ->
                        Toast.makeText(this, "Verbindungsfehler", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }
}
