package ie.dodwyer.carseatmonitorapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import ie.dodwyer.carseatmonitorapp.R;
import ie.dodwyer.carseatmonitorapp.ble.BluetoothLeService;
import ie.dodwyer.carseatmonitorapp.ble.GattAttributes;
import ie.dodwyer.carseatmonitorapp.model.Alert;
import ie.dodwyer.carseatmonitorapp.model.Alerts;
import ie.dodwyer.carseatmonitorapp.model.SecondaryContact;
/**
 * This class was based on the 'DeviceControlActivity' class from the BluetoothLeGatt tutorial on the Android Developers website:
 * https://developer.android.com/samples/BluetoothLeGatt/src/com.example.android.bluetoothlegatt/DeviceControlActivity.html
 */
public class MainActivity extends Base {
    LocationManager locationManager;
    Location initialCurrentLocation;
    private RequestQueue requestQueue;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String IN_PROXIMITY = "In Proximity";
    public static final String OUT_OF_PROXIMITY = "Out Of Proximity";
    public static final String OCCUPIED = "Occupied";
    public static final String UNOCCUPIED = "Unoccupied";
    private static String ACTION_SMS_SENT = "SMS_SENT";
    private static String ACTION_SMS_DELIVERED = "SMS_DELIVERED";
    private static String CONNECTED = "Connected";
    private static String DISCONNECTED = "Disconnected";
    private static final int FIME_LOCATION_PERMISSIONS_REQUEST = 2;
    private final static String TAG = MainActivity.class.getSimpleName();
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;
    public CountDownTimer childOutOfSeatAlertStationaryTimer;
    public CountDownTimer childOutOfSeatAlertInTransitTimer;
    public CountDownTimer childStillInSeatTimer;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private TextView mConnectionState;
    private TextView carSeatStatusValue;
    private TextView vehicleSpeedValue;
    private TextView rssiStatusValue;
    private TextView childOutOfSeatStationaryStatusValue;
    private TextView childOutOfSeatInTransitStatusValue;
    private TextView childStillInSeatStatusValue;
    private TextView secondaryContactValue;
    private Button buttonChildOutOfSeatStationaryAlert;
    private Button buttonChildOutOfSeatInTransitAlert;
    private Button buttonChildStillInSeatAlert;
    private Timer timer;
    private Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private Ringtone alertNotificationRingtone;
    ColorStateList oldTextColors;
    private TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            String alert = app.sensorState.checkSensorStateForAlert();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        updateSensorState();
                    String alert = app.sensorState.checkSensorStateForAlert();
                    if (alert != null&& mConnected==true)

                    {
                        if (Alerts.alertsRaised.get(alert) == false) {
                            alertPrimaryContact(MainActivity.this, alert);
                        }
                    }

                }
            });
        }
    };

    public void startAlertTimer() {
        if (timer != null) {
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 3000, 3000);
    }

    public void stopAlertTimer() {
        timer.cancel();
        timer = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent intent = getIntent();
        alertNotificationRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        carSeatStatusValue = (TextView) findViewById(R.id.car_seat_status_value);
        vehicleSpeedValue = (TextView) findViewById(R.id.vehicle_speed_value);
        rssiStatusValue = (TextView) findViewById(R.id.rssi_status_value);
        buttonChildOutOfSeatStationaryAlert = (Button) findViewById(R.id.button_child_out_of_seat_stationary_alert);
        buttonChildOutOfSeatInTransitAlert = (Button) findViewById(R.id.button_child_out_of_seat_in_transit_alert);
        buttonChildStillInSeatAlert = (Button) findViewById(R.id.button_child_still_in_seat_alert);
        buttonChildOutOfSeatStationaryAlert.setOnClickListener(buttonClickListener);
        buttonChildOutOfSeatInTransitAlert.setOnClickListener(buttonClickListener);
        buttonChildStillInSeatAlert.setOnClickListener(buttonClickListener);
        buttonChildOutOfSeatStationaryAlert.setEnabled(false);
        buttonChildOutOfSeatStationaryAlert.setBackgroundResource(R.drawable.rounded_button_semi_transparent);
        buttonChildOutOfSeatInTransitAlert.setEnabled(false);
        buttonChildOutOfSeatInTransitAlert.setBackgroundResource(R.drawable.rounded_button_semi_transparent);
        buttonChildStillInSeatAlert.setEnabled(false);
        buttonChildStillInSeatAlert.setBackgroundResource(R.drawable.rounded_button_semi_transparent);
        childOutOfSeatStationaryStatusValue = (TextView) findViewById(R.id.child_out_of_seat_stationary_status_value);
        childOutOfSeatInTransitStatusValue = (TextView) findViewById(R.id.child_out_of_seat_in_transit_status_value);
        secondaryContactValue = (TextView) findViewById(R.id.secondary_contact_value);
        secondaryContactValue.setText(app.secondaryContact.getName());
        oldTextColors = secondaryContactValue.getTextColors();
        childStillInSeatStatusValue = (TextView) findViewById(R.id.child_still_in_seat_status_value);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        initiateGPSLocationManager();
        initialCurrentLocation = getLastKnownLocation();
        if(initialCurrentLocation!=null){
            app.sensorState.setGeoLat(String.valueOf(initialCurrentLocation.getLatitude()));
            app.sensorState.setGeoLong(String.valueOf(initialCurrentLocation.getLongitude()));
        }
        startAlertTimer();
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == FIME_LOCATION_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Network Access permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Network Access permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    /*
     * The code in the method below was based on code from the following web page:
     * https://stackoverflow.com/questions/40079174/how-to-send-a-post-request-with-json-body-using-volley/40079812
     * */
    public void insertAlert(Alert alertObj) {
        try {

            String URL = "https://car-seat-monitor-api-daithi303.c9users.io:8080/api/alert/";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("deviceId", alertObj.getDeviceId());
            jsonBody.put("alertType", alertObj.getAlertType());
            jsonBody.put("contactType", alertObj.getContactType());
            jsonBody.put("secondaryContactName", alertObj.getSecondaryContactName());
            jsonBody.put("secondaryContactNumber", alertObj.getSecondaryContactNumber());
            jsonBody.put("timeOfAlert", alertObj.getTimeOfAlert());
            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {

                        responseString = String.valueOf(response.statusCode);

                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*
     * The code in the method below was based on code from the following web page:
     * https://stackoverflow.com/questions/40079174/how-to-send-a-post-request-with-json-body-using-volley/40079812
     * */
    public void updateSensorState() {
        try {

            String URL = "https://car-seat-monitor-api-daithi303.c9users.io:8080/api/sensorState/" + app.sensorState.getDeviceId();
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("carSeatStatusValue", app.sensorState.getCarSeatStatusValue());
            jsonBody.put("vehicleSpeedValue", app.sensorState.getVehicleSpeedValue());
            jsonBody.put("rssiStatusValue", app.sensorState.getRssiStatusValue());
            jsonBody.put("geoLat", app.sensorState.getGeoLat());
            jsonBody.put("geoLong", app.sensorState.getGeoLong());
            jsonBody.put("connectionState", app.sensorState.getConnectionState());
            jsonBody.put("connectionStateTimeStamp", getTimeInMilliseconds());

            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {

                        responseString = String.valueOf(response.statusCode);

                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                app.sensorState.setConnectionState(CONNECTED);
                mConnected=true;
                initialCurrentLocation = getLastKnownLocation();
                if(initialCurrentLocation!=null){
                    app.sensorState.setGeoLat(String.valueOf(initialCurrentLocation.getLatitude()));
                    app.sensorState.setGeoLong(String.valueOf(initialCurrentLocation.getLongitude()));
                }
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                app.sensorState.setConnectionState(DISCONNECTED);
                mConnected=false;
                app.sensorState.setVehicleSpeedValue("0");
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mBluetoothLeService.disconnect();
        app.sensorState.setConnectionState(DISCONNECTED);
        app.sensorState.setVehicleSpeedValue("0");
        super.onBackPressed();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                initialCurrentLocation = getLastKnownLocation();
                if(initialCurrentLocation!=null){
                    app.sensorState.setGeoLat(String.valueOf(initialCurrentLocation.getLatitude()));
                    app.sensorState.setGeoLong(String.valueOf(initialCurrentLocation.getLongitude()));
                }
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                app.sensorState.setVehicleSpeedValue("0");
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i(TAG, "onRecieve GATT_SERVICES_DISCOVERED");
                displayGattServices(mBluetoothLeService.getSupportedGattServices(), "ff51b30e-d7e2-4d93-8842-a7c4a57dfb99");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayCarSeat(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_READ_REMOTE_RSSI.equals(action)) {
                Log.i(TAG, "onRecieve ReadRssi: " + intent.getStringExtra(BluetoothLeService.RSSI));
                displayRssi(intent.getStringExtra(BluetoothLeService.RSSI));
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void run() {
                if(getString(resourceId).equals(DISCONNECTED)){
                    mConnectionState.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimaryRed));

                }
                else if(getString(resourceId).equals(CONNECTED)){
                    mConnectionState.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorAlternative));
                }
                mConnectionState.setText(resourceId);
                app.sensorState.setConnectionState(getString(resourceId));

            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void displayRssi(String data) {
        int rssiRange = Integer.parseInt(data);
        String alert = new String();
        if (rssiRange > -85) {
            if (!(rssiStatusValue.getText().equals(IN_PROXIMITY))) {
                rssiStatusValue.setText(IN_PROXIMITY);
                rssiStatusValue.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorAlternative));
                alert = app.sensorState.setRssiStatusValue(IN_PROXIMITY);
            }
        } else {
            if (!(rssiStatusValue.getText().equals(OUT_OF_PROXIMITY))) {
                rssiStatusValue.setText(OUT_OF_PROXIMITY);
                rssiStatusValue.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimaryRed));
                alert = app.sensorState.setRssiStatusValue(OUT_OF_PROXIMITY);
            }
        }
    }


    private void displayCarSeat(String data) {
        Double value;
        String alert = new String();
        if (data != null) {
            value = Double.valueOf(data);
            if (value > 0.3) {

                if (!(carSeatStatusValue.getText().equals(OCCUPIED))) {
                    carSeatStatusValue.setText(OCCUPIED);
                    carSeatStatusValue.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorAlternative));

                    alert = app.sensorState.setCarSeatStatusValue(OCCUPIED);
                }
            } else {
                if (!(carSeatStatusValue.getText().equals(UNOCCUPIED))) {
                    carSeatStatusValue.setText(UNOCCUPIED);
                    carSeatStatusValue.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimaryRed));

                    alert = app.sensorState.setCarSeatStatusValue(UNOCCUPIED);
                }
            }
        }
    }

    /*
    * The code in the method below was based on code from the following web page:
    * https://stackoverflow.com/questions/25483352/how-to-get-last-known-location-for-location-manager-in-android
    * */
    public Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocationGPS != null) {

            return lastKnownLocationGPS;
        } else {
            Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            return loc;
        }
    } else {
        return null;
    }
}


/*
 * The code in the method below was based on code from the following web page:
 * http://www.androidhub4you.com/2013/06/how-to-get-device-current-speed-in_112.html
 * */
    private void initiateGPSLocationManager(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                String geoLat = String.valueOf(location.getLatitude());
                String geoLong = String.valueOf(location.getLongitude());
                DecimalFormat df = new DecimalFormat("#.#");
                float vehicleSpeed = ((location.getSpeed()*3600)/1000);
                vehicleSpeedValue.setText(df.format(vehicleSpeed));
                String alert = app.sensorState.setVehicleSpeedValue(df.format(vehicleSpeed));
                app.sensorState.setGeoLat(geoLat);
                app.sensorState.setGeoLong(geoLong);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FIME_LOCATION_PERMISSIONS_REQUEST);
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void alertPrimaryContact(Context context,String alert){
        Alert alertObj = new Alert();
        alertObj.setDeviceId(app.sensorState.getDeviceId());
        alertObj.setContactType(getString(R.string.label_primary));
        alertObj.setSecondaryContactName("");
        alertObj.setSecondaryContactNumber("");
        alertObj.setTimeOfAlert(getTimeInMilliseconds());
        if(alert.equals(Alerts.CHILD_STILL_IN_SEAT_ALERT)){
        alertObj.setAlertType(getString(R.string.label_child_still_in_seat_alert));
        raiseChildStillInSeatAlert();
        insertAlert(alertObj);
        }

        if(alert.equals(Alerts.CHILD_OUT_OF_SEAT_IN_TRANSIT_ALERT)){
        alertObj.setAlertType(getString(R.string.label_child_out_of_seat_in_transit_alert));
        raiseChildOutOfSeatInTransitAlert();
        insertAlert(alertObj);
        }
        if(alert.equals(Alerts.CHILD_OUT_OF_SEAT_STATIONARY_ALERT)){
            alertObj.setAlertType(getString(R.string.label_child_out_of_seat_stationary_alert));
            raiseChildOutOfSeatStationaryAlert();
            insertAlert(alertObj);
        }
}

    private void raiseChildStillInSeatAlert(){
        Alerts.alertsRaised.put(Alerts.CHILD_STILL_IN_SEAT_ALERT,true);
        childStillInSeatStatusValue.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryRed));
        childStillInSeatStatusValue.setText(R.string.alert_status_value_alert_raised);
        buttonChildStillInSeatAlert.setEnabled(true);
        buttonChildStillInSeatAlert.setBackgroundResource(R.drawable.rounded_button);
        childStillInSeatTimer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                buttonChildStillInSeatAlert.setText(String.valueOf(Math.round(millisUntilFinished/1000)));
            }

            @Override
            public void onFinish() {
                acknowledgeChildStillInSeatAlert();
                Alert alertObj = new Alert();
                alertObj.setDeviceId(app.sensorState.getDeviceId());
                alertObj.setContactType(getString(R.string.label_secondary));
                alertObj.setSecondaryContactName(app.secondaryContact.getName());
                alertObj.setSecondaryContactNumber("000 0000000(for testing)");
                alertObj.setTimeOfAlert(getTimeInMilliseconds());
                alertObj.setAlertType(getString(R.string.label_child_still_in_seat_alert));
                sendSMS(app.secondaryContact, getString(R.string.label_child_still_in_seat_alert));
                insertAlert(alertObj);
            }
        }.start();
        if(alertNotificationRingtone.isPlaying() == false) {
            alertNotificationRingtone.play();
        }
    }

    private void acknowledgeChildStillInSeatAlert(){
        Alerts.alertsRaised.put(Alerts.CHILD_STILL_IN_SEAT_ALERT,false);
        childStillInSeatStatusValue.setTextColor(ContextCompat.getColor(this, R.color.colorAlternative));
        childStillInSeatStatusValue.setText(R.string.alert_status_value_no_alert_raised);
        buttonChildStillInSeatAlert.setEnabled(false);
        buttonChildStillInSeatAlert.setBackgroundResource(R.drawable.rounded_button_semi_transparent);
        childStillInSeatTimer.cancel();
        buttonChildStillInSeatAlert.setText(R.string.alert_button_no_alert);
        if(alertNotificationRingtone.isPlaying() == true) {
            alertNotificationRingtone.stop();
        }
    }

    private void raiseChildOutOfSeatInTransitAlert(){
        Alerts.alertsRaised.put(Alerts.CHILD_OUT_OF_SEAT_IN_TRANSIT_ALERT,true);
        childOutOfSeatInTransitStatusValue.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryRed));
        childOutOfSeatInTransitStatusValue.setText(R.string.alert_status_value_alert_raised);
        buttonChildOutOfSeatInTransitAlert.setEnabled(true);
        buttonChildOutOfSeatInTransitAlert.setBackgroundResource(R.drawable.rounded_button);
        childOutOfSeatAlertInTransitTimer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                buttonChildOutOfSeatInTransitAlert.setText(String.valueOf(String.valueOf(Math.round(millisUntilFinished/1000))));
            }

            @Override
            public void onFinish() {
                acknowledgeChildOutOfSeatInTransitAlert();
                Alert alertObj = new Alert();
                alertObj.setDeviceId(app.sensorState.getDeviceId());
                alertObj.setContactType(getString(R.string.label_secondary));
                alertObj.setSecondaryContactName(app.secondaryContact.getName());
                alertObj.setSecondaryContactNumber("000 0000000(for testing)");
                alertObj.setTimeOfAlert(getTimeInMilliseconds());
                alertObj.setAlertType(getString(R.string.label_child_out_of_seat_in_transit_alert));
                sendSMS(app.secondaryContact, getString(R.string.label_child_out_of_seat_in_transit_alert));
                insertAlert(alertObj);
            }
        }.start();
        if(alertNotificationRingtone.isPlaying() == false) {
            alertNotificationRingtone.play();
        }
    }

    private void acknowledgeChildOutOfSeatInTransitAlert(){
        Alerts.alertsRaised.put(Alerts.CHILD_OUT_OF_SEAT_IN_TRANSIT_ALERT,false);
        childOutOfSeatInTransitStatusValue.setTextColor(ContextCompat.getColor(this, R.color.colorAlternative));
        childOutOfSeatInTransitStatusValue.setText(R.string.alert_status_value_no_alert_raised);
        buttonChildOutOfSeatInTransitAlert.setEnabled(false);
        buttonChildOutOfSeatInTransitAlert.setBackgroundResource(R.drawable.rounded_button_semi_transparent);
        childOutOfSeatAlertInTransitTimer.cancel();
        buttonChildOutOfSeatInTransitAlert.setText(R.string.alert_button_no_alert);
        if(alertNotificationRingtone.isPlaying() == true) {
            alertNotificationRingtone.stop();
        }
    }


    private void raiseChildOutOfSeatStationaryAlert(){
        Alerts.alertsRaised.put(Alerts.CHILD_OUT_OF_SEAT_STATIONARY_ALERT,true);
        childOutOfSeatStationaryStatusValue.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryRed));
        childOutOfSeatStationaryStatusValue.setText(R.string.alert_status_value_alert_raised);
        buttonChildOutOfSeatStationaryAlert.setEnabled(true);
        buttonChildOutOfSeatStationaryAlert.setBackgroundResource(R.drawable.rounded_button);
        childOutOfSeatAlertStationaryTimer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                buttonChildOutOfSeatStationaryAlert.setText(String.valueOf(String.valueOf(Math.round(millisUntilFinished/1000))));
            }

            @Override
            public void onFinish() {
                acknowledgeChildOutOfSeatStationaryAlert();
                Alert alertObj = new Alert();
                alertObj.setDeviceId(app.sensorState.getDeviceId());
                alertObj.setContactType(getString(R.string.label_secondary));
                alertObj.setSecondaryContactName(app.secondaryContact.getName());
                alertObj.setSecondaryContactNumber("000 0000000(for testing)");
                alertObj.setTimeOfAlert(getTimeInMilliseconds());
                alertObj.setAlertType(getString(R.string.label_child_out_of_seat_stationary_alert));
                sendSMS(app.secondaryContact, getString(R.string.label_child_out_of_seat_stationary_alert));
                insertAlert(alertObj);
            }
        }.start();
        if(alertNotificationRingtone.isPlaying() == false) {
            alertNotificationRingtone.play();
        }
    }

    private void acknowledgeChildOutOfSeatStationaryAlert(){
        Alerts.alertsRaised.put(Alerts.CHILD_OUT_OF_SEAT_STATIONARY_ALERT,false);
        childOutOfSeatStationaryStatusValue.setTextColor(ContextCompat.getColor(this, R.color.colorAlternative));
        childOutOfSeatStationaryStatusValue.setText(R.string.alert_status_value_no_alert_raised);
        buttonChildOutOfSeatStationaryAlert.setEnabled(false);
        buttonChildOutOfSeatStationaryAlert.setBackgroundResource(R.drawable.rounded_button_semi_transparent);
        childOutOfSeatAlertStationaryTimer.cancel();
        buttonChildOutOfSeatStationaryAlert.setText(R.string.alert_button_no_alert);
        if(alertNotificationRingtone.isPlaying() == true) {
            alertNotificationRingtone.stop();
        }
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v){
            switch (v.getId()) {
                case R.id.button_child_still_in_seat_alert:
                    acknowledgeChildStillInSeatAlert();
                    break;
                case R.id.button_child_out_of_seat_stationary_alert:
                    acknowledgeChildOutOfSeatStationaryAlert();
                    break;
                case R.id.button_child_out_of_seat_in_transit_alert:
                    acknowledgeChildOutOfSeatInTransitAlert();
                default:

                    break;
            }
        }
    };


    private void clearUI() {
        carSeatStatusValue.setText(R.string.no_data);
        carSeatStatusValue.setTextColor(oldTextColors);
        rssiStatusValue.setText(R.string.no_data);
        rssiStatusValue.setTextColor(oldTextColors);
        vehicleSpeedValue.setText(R.string.no_data);
        carSeatStatusValue.setTextColor(oldTextColors);
    }

    private void displayGattServices(List<BluetoothGattService> gattServices, String charUuidToEnableNotifocation) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                if(charUuidToEnableNotifocation != null){
                    if(uuid.equals(charUuidToEnableNotifocation)){
                        final int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = gattCharacteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    gattCharacteristic, true);
                        }
                    }
                }
                currentCharaData.put(
                        LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_READ_REMOTE_RSSI);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    /*
     * Some of the code in the method below was based on code from the following web page:
     * https://www.androidauthority.com/how-to-create-an-sms-app-721438/
     * */
    public void sendSMS(SecondaryContact secondaryContact, String alertType){
        Intent sendIntent = new Intent(ACTION_SMS_SENT);
        sendIntent.putExtra("contact", secondaryContact.getName());
        sendIntent.putExtra("alertType", alertType);
        Intent deliveryIntent = new Intent(ACTION_SMS_DELIVERED);
        deliveryIntent.putExtra("contact",secondaryContact.getName());
        PendingIntent pendingSent = PendingIntent.getBroadcast(MainActivity.this, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingDelivered = PendingIntent.getBroadcast(MainActivity.this, 0, deliveryIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(secondaryContact.getPhoneNumber(), null, createSmsAlertMessage(alertType), pendingSent, pendingDelivered);
    }

    public String createSmsAlertMessage(String alertType){
        String message = "A '"+alertType+"' alert was raised at "+getDate()+" on the car seat monitor device: '"+mDeviceName+"'.";
        return message;
    }

    public String getDate(){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = df.format(c.getTime());
        df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate+", "+formattedTime;
    }
    public String getTimeInMilliseconds(){
        Calendar c = Calendar.getInstance();
        return String.valueOf(c.getTimeInMillis());
    }
}
