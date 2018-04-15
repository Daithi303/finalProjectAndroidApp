package ie.dodwyer.carseatmonitorapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import ie.dodwyer.carseatmonitorapp.R;
import ie.dodwyer.carseatmonitorapp.adapters.DeviceListAdapter;
import ie.dodwyer.carseatmonitorapp.fragments.DeviceItemFragment;

public class ScanActivity extends Base {
    //private Handler mHandler;
    //private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    //private TextView vehicleSpeedValue;
    //private DeviceListAdapter mLeDeviceListAdapter;
    // private boolean mScanning;

    // Stops scanning after 10 seconds.
    // private static final long SCAN_PERIOD = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // vehicleSpeedValue = (TextView) findViewById(R.id.vehicle_speed_value);
        //getSupportActionBar().setTitle(R.string.title_devices);
        //app.mHandler = new Handler();
        /*
        if(app.mLeDeviceListAdapter == null) {
            app.mLeDeviceListAdapter = new DeviceListAdapter(this);
        }else if(app.mLeDeviceListAdapter.getCount() == 0){
            app.mLeDeviceListAdapter = new DeviceListAdapter(this);
        }
        */
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        /*
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        app.mBluetoothAdapter = bluetoothManager.getAdapter();
*/
        // Checks if Bluetooth is supported on the device.
        if (app.mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getPermissionToAccessCoarseLocation();
        getPermissionToSendSMS();
        //testInitiateGPSLocationManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        deviceItemFragment = DeviceItemFragment.newInstance();

        getFragmentManager().beginTransaction().add(R.id.deviceListFragment, deviceItemFragment).commit(); // add it to the current activity
        if (!app.mBluetoothAdapter.isEnabled()) {
            if (!app.mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        deviceItemFragment.setListAdapter(app.mLeDeviceListAdapter);
        app.scanLeDevice(true,this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onPause() {
        super.onPause();
        app.scanLeDevice(false,this);
        if(app.mLeDeviceListAdapter != null) {
            app.mLeDeviceListAdapter.clear();
        }
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        if (!app.mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                app.mLeDeviceListAdapter.clear();
                //app.mLeDeviceListAdapter.notifyDataSetChanged();
                app.scanLeDevice(true,this);
                break;
            case R.id.menu_stop:
                app.scanLeDevice(false,this);
                break;
        }
        return true;
    }
/*
    private void testInitiateGPSLocationManager(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                location.getLatitude();
                DecimalFormat df = new DecimalFormat("#.#");
                float vehicleSpeed = ((location.getSpeed()*3600)/1000);
                vehicleSpeedValue.setText(df.format(vehicleSpeed));

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

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            } else {

                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FIME_LOCATION_PERMISSIONS_REQUEST);


            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    */
}