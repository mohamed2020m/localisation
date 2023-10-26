package me.ensa.map;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 123;

    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    RequestQueue requestQueue;
    String insertUrl = "http://192.168.0.131/localisation/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Button btn = findViewById(R.id.open_map);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        Log.d("out2", "outside Location manger");

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
        } else {
            // Location permissions are already granted, you can proceed with location-related operations.
        }

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }

        Log.d("out", "outside Location manger");

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 150,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d("ye", "inside Location manger");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        altitude = location.getAltitude();
                        accuracy = location.getAccuracy();
                        @SuppressLint("StringFormatMatches") String msg = String.format(
                                getResources().getString(R.string.new_location), latitude, longitude, altitude, accuracy);
                        addPosition(latitude, longitude);
                        Intent locationIntent = new Intent("NEW_LOCATION_RECEIVED");
                        locationIntent.putExtra("latitude", latitude);
                        locationIntent.putExtra("longitude", longitude);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(locationIntent);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        String newStatus = "";
                        switch (status) {
                            case LocationProvider.OUT_OF_SERVICE:
                                newStatus = "OUT_OF_SERVICE";
                                break;
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                newStatus = "TEMPORARILY_UNAVAILABLE";
                                break;
                            case LocationProvider.AVAILABLE:
                                newStatus = "AVAILABLE";
                                break;
                        }
                        String msg = String.format(getResources().getString(R.string.provider_new_status),
                                provider, newStatus);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        String msg = String.format(getResources().getString(R.string.provider_enabled),
                                provider);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        String msg = String.format(getResources().getString(R.string.provider_disabled),
                                provider);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed with location-related operations.
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, handle this case gracefully, maybe explain to the user why you need location permissions.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    void addPosition(final double lat, final double lon) {
        Log.d("p", "position");
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
            new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("vol_err", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                TelephonyManager telephonyManager =
                        (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                HashMap<String, String> params = new HashMap<String, String>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                params.put("latitude", lat + "");
                params.put("longitude", lon + "");
                params.put("date", sdf.format(new Date()) + "");
                params.put("imei", Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID));
                Log.d("Send", "getParams: ");
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings","com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }
}

//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//public class MainActivity extends AppCompatActivity {
//    private Button button;
//    private double latitude;
//    private double longitude;
//    private double altitude;
//    private float accuracy;
//    RequestQueue requestQueue;
//    String insertUrl = "http://192.168.0.131:8080/localisation/createposition";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        button = findViewById(R.id.open_map);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//
//        requestQueue = Volley.newRequestQueue(getApplicationContext());
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 150, new
//                LocationListener() {
//                    @Override
//                    public void onLocationChanged(Location location) {
//                        Log.d("loc", "location");
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                        altitude = location.getAltitude();
//                        accuracy = location.getAccuracy();
//                        @SuppressLint("StringFormatMatches")
//                        String msg = String.format(getResources().getString(R.string.new_location), latitude, longitude, altitude, accuracy);
//                        addPosition(latitude, longitude);
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onStatusChanged(String provider, int status, Bundle extras) {
//                        String newStatus = "";
//                        switch (status) {
//                            case LocationProvider.OUT_OF_SERVICE:
//                                newStatus = "OUT_OF_SERVICE";
//                                break;
//                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                                newStatus = "TEMPORARILY_UNAVAILABLE";
//                                break;
//                            case LocationProvider.AVAILABLE:
//                                newStatus = "AVAILABLE";
//                                break;
//                        }
//                        String msg = String.format(getResources().getString(R.string.provider_new_status),
//                                provider, newStatus);
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onProviderEnabled(String provider) {
//                        String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onProviderDisabled(String provider) {
//                        String msg = String.format(getResources().getString(R.string.provider_disabled),
//                                provider);
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    void addPosition(final double lat, final double lon) {
//        StringRequest request = new StringRequest(Request.Method.POST,
//                insertUrl, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                TelephonyManager telephonyManager =
//                        (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//
//                HashMap<String, String> params = new HashMap<String, String>();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                params.put("latitude", lat + "");
//                params.put("longitude", lon + "");
//                params.put("date", sdf.format(new Date()) + "");
//                params.put("imei", Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID));
//                return params;
//            }
//        };
//        requestQueue.add(request);
//    }
//}