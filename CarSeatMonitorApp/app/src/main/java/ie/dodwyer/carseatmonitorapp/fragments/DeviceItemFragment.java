package ie.dodwyer.carseatmonitorapp.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;

import java.util.UUID;

import ie.dodwyer.carseatmonitorapp.activities.Base;
import ie.dodwyer.carseatmonitorapp.activities.MainActivity;
import ie.dodwyer.carseatmonitorapp.adapters.DeviceListAdapter;

public class DeviceItemFragment extends ListFragment {
  //  ListView listView;
    Base activity;
  //  private boolean mScanning;
   // private Handler mHandler;
   // private BluetoothAdapter mBluetoothAdapter;
   // private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
   // private static final long SCAN_PERIOD = 10000;
   // public static DeviceListAdapter mLeDeviceListAdapter;
    public DeviceItemFragment(){
    }

    public static DeviceItemFragment newInstance() {
        DeviceItemFragment fragment = new DeviceItemFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (Base) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter (activity.app.mLeDeviceListAdapter);
        /*
        List<Game> gameList =  activity.app.dbManager.getAllGames();
        listAdapter = new GameListAdapter(activity, new DeleteListener(),new NewChallengeListener(), gameList);
        gameFilter = new GameFilter(gameList,-999,listAdapter,getActivity());
        if (getActivity() instanceof MyGamesActivity) {
            int currentPlayerId = activity.app.currentPlayer.getPlayerId();
            gameFilter.setPlayerFilter(currentPlayerId);
            gameFilter.filter(null);
            listAdapter.notifyDataSetChanged();
        }

        */
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = activity.app.mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this.getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (activity.app.mScanning) {
            activity.app.stopScan();
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
            activity.app.mScanning = false;
        }
        startActivity(intent);
    }

}
