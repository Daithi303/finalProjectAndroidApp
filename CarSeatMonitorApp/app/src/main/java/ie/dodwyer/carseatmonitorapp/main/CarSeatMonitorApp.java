package ie.dodwyer.carseatmonitorapp.main;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ie.dodwyer.carseatmonitorapp.adapters.DeviceListAdapter;
import ie.dodwyer.carseatmonitorapp.ble.BluetoothLeService;
import ie.dodwyer.carseatmonitorapp.ble.GattAttributes;
import ie.dodwyer.carseatmonitorapp.model.SecondaryContact;
import ie.dodwyer.carseatmonitorapp.model.SensorState;
import ie.dodwyer.carseatmonitorapp.model.SensorStateDto;

public class CarSeatMonitorApp extends Application {
    public Handler mHandler;
    public Activity activity;
    public BluetoothLeScanner bluetoothLeScanner;
    public BluetoothAdapter mBluetoothAdapter;
    public SensorState sensorState;
    //public static final int REQUEST_ENABLE_BT = 1;
    public DeviceListAdapter mLeDeviceListAdapter;
    private BluetoothLeService mBluetoothLeService;
    public boolean mScanning;
    public SecondaryContact secondaryContact;
   // private static final int COARSE_LOCATION_PERMISSIONS_REQUEST = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    @Override
    public void onCreate()
    {
        super.onCreate();
        mHandler = new Handler();
        sensorState = new SensorState();
        sensorState.setDeviceId("0001");
        sensorState.setVehicleSpeedValue("0");
        sensorState.setGeoLat("0.000000000");
        sensorState.setGeoLong("0.000000000");
        sensorState.setConnectionState("Disconnected");
        secondaryContact = new SecondaryContact("David O' Dwyer","0863788413",null);

    }



    public void stopScan(){
        if(mBluetoothAdapter != null) {
            if(bluetoothLeScanner != null) {
                bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }
        }
    }

    public void scanLeDevice(final boolean enable, final Activity activity) {
        this.activity = activity;
        //mLeDeviceListAdapter = new DeviceListAdapter(activity);
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter beaconFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(GattAttributes.CHILD_SEAT_SERVICE)).build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(beaconFilter);
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        if (enable) {
            System.out.println("scanLeDevice: enable is true");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    activity.invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            UUID[] uuid = new UUID[1];
            bluetoothLeScanner.startScan(filters,settings,mLeScanCallback);
            //mBluetoothAdapter.startLeScan( mLeScanCallback);

        } else {

            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        this.activity.invalidateOptionsMenu();
    }

    public ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            mLeDeviceListAdapter.addDevice(result.getDevice());
            mLeDeviceListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                mLeDeviceListAdapter.addDevice(sr.getDevice());
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    /*
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(device != null) {
                                System.out.println("mLeScanCallback- Device: "+device.getName());
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };
            */
}
