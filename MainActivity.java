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
    private static double DistanzRadius3km=3000.0;
    private static final double LATITUDE_TEST= 52.020885;
    private static final double LONGITUDE_TEST = 8.532471;
    LocationListener locationListener = new LocationListener(){
        public void onLocationChanged(Location loc){
            calculateDistanceToTestLocation(loc);
        }
        public void onStatusChanged(String provider, int status, Bundle extras){}
        public void onProviderEnabled(String provider){}
        public void onProviderDisabled(String provider){}
    };
    private void startGPSStream(){
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions,  LOCATION_REQUEST_CODE);
        }
        locationManager=(LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
    }
    private void calculateDistanceToTestLocation(Location currentLocation){
        float[] results = new float[1];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                LATITUDE_TEST, LONGITUDE_TEST, results);
        float distanceInMeters = results[0];
        if (distanceInMeters<=DistanzRadius3km){
            tvData.append("\n Sie befinden sich im 3km Radius vom Test-Ziel!");
        } else {
            tvData.append("\n Sie befinden sich nicht im 3km Radius vom Test-Ziel!");
        }
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
        tvData=findViewById(R.id.tvData);
        startGPSStream();
    }
}