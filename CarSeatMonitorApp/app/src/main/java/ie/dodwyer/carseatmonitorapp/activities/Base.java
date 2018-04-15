package ie.dodwyer.carseatmonitorapp.activities;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.text.DecimalFormat;

import ie.dodwyer.carseatmonitorapp.adapters.DeviceListAdapter;
import ie.dodwyer.carseatmonitorapp.fragments.DeviceItemFragment;
import ie.dodwyer.carseatmonitorapp.main.CarSeatMonitorApp;


/**
 * Created by User on 2/24/2017.
 */

public class Base extends AppCompatActivity {
    public CarSeatMonitorApp app;
    public DeviceItemFragment deviceItemFragment;
    private static final int COARSE_LOCATION_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (CarSeatMonitorApp) getApplication();
        app.mLeDeviceListAdapter = new DeviceListAdapter(this);
        app.mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        app.mBluetoothAdapter = bluetoothManager.getAdapter();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (requestCode == COARSE_LOCATION_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access coarse location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Access coarse location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == SEND_SMS_PERMISSIONS_REQUEST){
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public void getPermissionToAccessCoarseLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }else {

                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        COARSE_LOCATION_PERMISSIONS_REQUEST);


            }
        }
    }

    public void getPermissionToSendSMS(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},
                        SEND_SMS_PERMISSIONS_REQUEST);
            }
        }

    }
}
