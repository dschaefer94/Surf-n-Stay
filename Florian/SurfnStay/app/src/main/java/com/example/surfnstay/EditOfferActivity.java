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
        binding.cbSauna.setChecked(currentOffer.hasSauna == 1);
        binding.cbFireplace.setChecked(currentOffer.hasFireplace == 1);
        binding.cbPets.setChecked(currentOffer.hasPets == 1);
        binding.cbInternet.setChecked(currentOffer.hasInternet == 1);
        binding.switchPublished.setChecked(currentOffer.isPublished == 1);
    }

    private void saveOffer() {
        // Daten aus UI ins Objekt schreiben
        currentOffer.address = binding.etAddress.getText().toString();
        currentOffer.price = binding.etPrice.getText().toString();
        try {
            currentOffer.beds = Integer.parseInt(binding.etBeds.getText().toString());
        } catch (NumberFormatException e) { currentOffer.beds = 1; }

        currentOffer.hasSauna = binding.cbSauna.isChecked() ? 1 : 0;
        currentOffer.hasFireplace = binding.cbFireplace.isChecked() ? 1 : 0;
        currentOffer.hasPets = binding.cbPets.isChecked() ? 1 : 0;
        currentOffer.hasInternet = binding.cbInternet.isChecked() ? 1 : 0;
        currentOffer.isPublished = binding.switchPublished.isChecked() ? 1 : 0;

        // Dummy GPS Daten (falls keine da sind)
        if (currentOffer.lat == 0) currentOffer.lat = 56.0;
        if (currentOffer.lon == 0) currentOffer.lon = 8.1;

        // Senden
        sendRequest(currentOffer.id == 0 ? "POST" : "PUT");
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