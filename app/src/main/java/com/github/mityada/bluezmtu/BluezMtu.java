package com.github.mityada.bluezmtu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

public class BluezMtu extends Activity {

    private static final String TAG = "BluezMtu";

    private static final String ADDRESS = "DE:AD:BE:EF:CA:FE"; // Change to real MAC

    private static final UUID TEST_SERVICE = UUID.fromString("12345678-1234-5678-1234-56789abcdef0");
    private static final UUID TEST_CHARACTERISTIC = UUID.fromString("12345678-1234-5678-1234-56789abcdef1");

    private static final int MTU_A = 200;
    private static final int MTU_B = 100;

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected");

                Log.d(TAG, "Requesting MTU " + MTU_A);
                gatt.requestMtu(MTU_A);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.d(TAG, "onServicesDiscovered " + status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Discovery failed");
                return;
            }

            BluetoothGattService service = gatt.getService(TEST_SERVICE);
            BluetoothGattCharacteristic test = service.getCharacteristic(TEST_CHARACTERISTIC);
            gatt.readCharacteristic(test);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.d(TAG, "onCharacteristicRead " + status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Read failed");
                return;
            }

            Log.d(TAG, "Characteristic value: " + characteristic.getStringValue(0));
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            Log.d(TAG, "onMtuChanged " + mtu + " " + status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Failed to change MTU");
                return;
            }

            if (mtu == MTU_A) {
                Log.d(TAG, "Requesting MTU " + MTU_B);
                gatt.requestMtu(MTU_B);
            } else {
                Log.d(TAG, "Discovering services");
                gatt.discoverServices();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ADDRESS);

        Log.d(TAG, "Connecting");
        device.connectGatt(
            this,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        );
    }

}
