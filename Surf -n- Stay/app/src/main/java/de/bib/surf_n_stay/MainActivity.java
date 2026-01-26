package de.bib.surf_n_stay;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultText = findViewById(R.id.textView); // Stelle sicher, dass die ID passt

        // Netzwerk-Aufruf in einem eigenen Thread
        new Thread(() -> {
            try {
                // Die magische Emulator-IP
                URL url = new URL("http://10.0.2.2:8080/user");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Stream auslesen
                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                // UI-Update muss wieder zurück auf den Haupt-Thread!
                runOnUiThread(() -> resultText.setText(result));

            } catch (Exception e) {
                runOnUiThread(() -> resultText.setText("Fehler: " + e.getMessage()));
            }
        }).start();
    }
}