package com.android.samplesmartpen.fragments;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.android.samplesmartpen.R;
import com.android.samplesmartpen.SampleViewModel;
import com.android.samplesmartpen.utilities.PenClientCtrl;
import com.android.samplesmartpen.utilities.PenDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import kr.neolab.sdk.util.NLog;

import static com.android.samplesmartpen.utilities.Util.isBLESupported;
import static com.android.samplesmartpen.utilities.Util.showToast;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";
    private static final int REQUEST_BT_PAIRING = 0x1;

    //VIEWS
    private ListView mlvPairedDevices;

    //BLUETOOTH

    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mLeScanner;
    private ScanSettings mScanSetting;
    private List<ScanFilter> mScanFilters;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    private SampleViewModel model;

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        model = ViewModelProviders.of(getActivity()).get(SampleViewModel.class);

        mlvPairedDevices = view.findViewById(R.id.lvPairedDevice);

        mPairedDevicesArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.device_name);

        // Find and set up the ListView for paired devices
        mlvPairedDevices.setAdapter(mPairedDevicesArrayAdapter);
        mlvPairedDevices.setOnItemClickListener(itemClickListener);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        getPairedDevices();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        getActivity().registerReceiver(mBluetoothReceiver, filter);

        if (Build.VERSION.SDK_INT >= 21) {
            mLeScanner = mBtAdapter.getBluetoothLeScanner();
            mScanSetting = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            mScanFilters = new ArrayList<>();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.scan_device) {//TODO: SCAN DEVICE CODE
            scanDevices();
        }
        return super.onOptionsItemSelected(item);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            model.updatedata(address);
            Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_bluetoothFragment_to_mainFragment);
        }
    };

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device;

            if (action != null) {
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String deviceName = device.getName();
                        String deviceMACAddress = device.getAddress();
                        if (Arrays.asList(PenDetails.penNames).contains(deviceName)) {
                            pairDevice(device);

                            if (mBtAdapter.isDiscovering()) {
                                mBtAdapter.cancelDiscovery();
                            }
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                        final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                            showToast(getContext(), "Paired");
                        } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                            showToast(getContext(), "Unpaired");
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        showToast(getContext(), "Discovery Started");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        showToast(getContext(), "Discovery Finished");
                        break;

                    case BluetoothDevice.ACTION_PAIRING_REQUEST:
                        break;
                }

            }
        }
    };


    private void getPairedDevices() {
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }

        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    private void scanDevices() {
        if (isBLESupported(getContext())) {
            //TODO: SUPPORTED
//            mLeScanner.startScan(mScanFilters, mScanSetting, mScanCallback);
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }
            mBtAdapter.startDiscovery();
        } else {
            //TODO: NOT SUPPORTED
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }
            mBtAdapter.startDiscovery();
        }
    }

    private void pairDevice(BluetoothDevice device) {
        device.createBond();

        mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        mPairedDevicesArrayAdapter.notifyDataSetChanged();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "onScanFailed: failed");
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            if (device != null) {
                String msg = device.getName() + "\n" + "[RSSI : " + result.getRssi() + "dBm]" + device.getAddress();
                NLog.d("ACTION_FOUND onLeScan : " + device.getName() + " address : " + device.getAddress() + ", COD:" + device.getBluetoothClass());
                PenClientCtrl.getInstance(getContext()).setLeMode(true);
                if (PenClientCtrl.getInstance(getContext()).isAvailableDevice(result.getScanRecord().getBytes())) {
                    if (Arrays.asList(PenDetails.penNames).contains(device.getName())) {
//                        pairDevice(device);
                        BluetoothGatt server = device.connectGatt(getContext(), true, serverCallback);
                        server.connect();
                        mLeScanner.stopScan(mScanCallback);

                    }
                }
            }
        }
    };

    BluetoothGattCallback serverCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (BluetoothProfile.STATE_CONNECTED == newState){
                showToast(getContext(), "GWAPO");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }


    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BT_PAIRING) {
            if (resultCode == Activity.RESULT_CANCELED) {  // 0 != -1
                Log.d("CARLO", "Let#s pair!!!!"); // NOT CALLED
            }
        }

    }
}