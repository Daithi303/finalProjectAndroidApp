package ie.dodwyer.carseatmonitorapp.main;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.ParcelUuid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import ie.dodwyer.carseatmonitorapp.adapters.DeviceListAdapter;
import ie.dodwyer.carseatmonitorapp.ble.GattAttributes;
import ie.dodwyer.carseatmonitorapp.model.SecondaryContact;
import ie.dodwyer.carseatmonitorapp.model.SensorState;

public class CarSeatMonitorApp extends Application {
    public Handler mHandler;
    public Activity activity;
    public BluetoothLeScanner bluetoothLeScanner;
    public BluetoothAdapter mBluetoothAdapter;
    public SensorState sensorState;
    public DeviceListAdapter mLeDeviceListAdapter;
    public boolean mScanning;
    public SecondaryContact secondaryContact;
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
                    activity.invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            UUID[] uuid = new UUID[1];
            bluetoothLeScanner.startScan(filters,settings,mLeScanCallback);


        } else {

            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);

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


}
