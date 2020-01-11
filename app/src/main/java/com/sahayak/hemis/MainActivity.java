package com.sahayak.hemis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageButton mGetLocation;
    private TextView mCoordinates;
    private TextView mSuccess;
    private ImageView mSuccessIcon;
    private LocationManager locationManager;
    private LocationListener listener;
    private String deviceId;
    private double latitude = 33.418801;
    private double longitude = -111.933017;

    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGetLocation = findViewById(R.id.requestLocation);
        mCoordinates = findViewById(R.id.locationCoordinates);
        mSuccess = findViewById(R.id.successText);
        mSuccessIcon = findViewById(R.id.successIcon);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}
                        , 12);
                Log.e(TAG, "onCreate: ");
                return;
            }
        }
        deviceId = telephonyManager.getDeviceId();

        Log.e(TAG, "onCreate: "+deviceId);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCoordinates.setText("");
                mCoordinates.append("\n " + location.getLongitude() + "," + location.getLatitude() + ", IMEI : " + deviceId);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        mGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "onClick: Permission not granted");
                        return;
                    }
                }
                locationManager.requestLocationUpdates("gps", 1000, 0, listener);
                new PostData().execute(deviceId,String.valueOf(longitude),String.valueOf(latitude));
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Help on it's way",
                        Toast.LENGTH_LONG);
                toast.show();
                mSuccess.setVisibility(View.VISIBLE);
                mSuccessIcon.setVisibility(View.VISIBLE);

            }
        });
    }
    private class PostData extends AsyncTask< String, Void, Void > {
        protected Void doInBackground(String...params) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://192.168.43.163:33/users");
            Log.e(TAG, "doInBackground: "+httppost.getURI());
            try {
                // Add your data
                String deviceId = params[0];
                String longitude = params[1];
                String latitude = params[2];

                List<BasicNameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("user_id", deviceId));
                nameValuePairs.add(new BasicNameValuePair("longitude", longitude));
                nameValuePairs.add(new BasicNameValuePair("latitude", latitude));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "doInBackground: "+ e.fillInStackTrace() );
            }
            return null;
        }
    }
}
