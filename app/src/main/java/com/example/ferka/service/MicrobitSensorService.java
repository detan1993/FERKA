package com.example.ferka.service;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by David on 25/3/2018.
 */

public class MicrobitSensorService extends IntentService {

    public static final String ACTION_MICROBIT = "com.example.mysecondapp.service.action.ACTION_MICROBIT";
    public static final String ACTION_MICROBIT_STOP = "com.example.mysecondapp.service.action.ACTION_MICROBIT_STOP";
    public static final String ACTION_MICROBIT_ACCELEROMETER = "com.example.mysecondapp.service.action.ACTION_MICROBIT_ACCELEROMETER";
    public static final String ACTION_MICROBIT_TEMPERATURE = "com.example.mysecondapp.service.action.ACTION_MICROBIT_TEMPERATURE";
    public static final String ACTION_MICROBIT_MAGNETOMETER = "com.example.mysecondapp.service.action.ACTION_MICROBIT_MAGNETOMETER";

    public static final String EXTRA_BLUETOOTH_DEVICE = "com.example.mysecondapp.service.extra.EXTRA_BLUETOOTH_DEVICE";
    public static final String EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER = "com.example.mysecondapp.service.extra.EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER";
    public static final String EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE = "com.example.mysecondapp.service.extra.EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE";
    public static final String EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER = "com.example.mysecondapp.service.extra.EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER";
    public static final String EXTRA_DATA_1 = "com.example.mysecondapp.service.extra.EXTRA_DATA_1";
    public static final String EXTRA_DATA_2 = "com.example.mysecondapp.service.extra.EXTRA_DATA_2";
    public static final String EXTRA_DATA_3 = "com.example.mysecondapp.service.extra.EXTRA_DATA_3";


    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER_UUID = "e95d0753-251d-470a-a062-fa1922dfa9a8";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER_DATA_UUID = "e95dca4b-251d-470a-a062-fa1922dfa9a8";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER_PERIOD_UUID = "e95dfb24-251d-470a-a062-fa1922dfa9a8";

    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE_UUID = "e95d6100-251d-470a-a062-fa1922dfa9a8";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE_DATA_UUID = "e95d9250-251d-470a-a062-fa1922dfa9a8";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE_PERIOD_UUID = "e95d1b25-251d-470a-a062-fa1922dfa9a8";

    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER_UUID = "e95df2d8-251d-470a-a062-fa1922dfa9a8";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER_DATA_UUID = "e95dfb11-251d-470a-a062-fa1922dfa9a8";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER_PERIOD_UUID = "e95d386c-251d-470a-a062-fa1922dfa9a8";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER_BEARING_UUID = "e95d9715-251d-470a-a062-fa1922dfa9a8";

    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_AVAILABLE_ACCELEROMETER = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_ACCELEROMETER";
    public final static String ACTION_DATA_AVAILABLE_TEMPERATURE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_TEMPERATURE";
    public final static String ACTION_DATA_AVAILABLE_MAGNETOMETER = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_MAGNETOMETER";

    private BluetoothGatt bluetoothGatt;

    private long start_time = 0;



    public MicrobitSensorService()
    {
        super("MicrobitSensorService");
    }



    public static void startActionMicrobit(Context context, BluetoothDevice bluetoothDevice, String action)
    {
        Intent intent = new Intent(context, MicrobitSensorService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE, bluetoothDevice);
        context.startService(intent);
    }



    public static void stopActionMicrobit(Context context)
    {
        Intent intent = new Intent(context, MicrobitSensorService.class);
        intent.setAction(ACTION_MICROBIT_STOP);
        context.sendBroadcast(intent);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Toast.makeText(this, "MicrobitSensorService service is starting", Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }



    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();

            if (action.equals(ACTION_MICROBIT) ||
                    action.equals(ACTION_MICROBIT_ACCELEROMETER) ||
                    action.equals(ACTION_MICROBIT_TEMPERATURE) ||
                    action.equals(ACTION_MICROBIT_MAGNETOMETER))
            {
                final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(EXTRA_BLUETOOTH_DEVICE);
                handleActionMicrobit(bluetoothDevice, action);
            }
            else if (action.equals(ACTION_MICROBIT_STOP))
            {
                Toast.makeText(this, "MicrobitSensorService service is stopping", Toast.LENGTH_SHORT).show();

                if(bluetoothGatt != null)
                {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
            }
        }
    }



    private void handleActionMicrobit(BluetoothDevice bluetoothDevice, String action)
    {
        bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, new MicrobitGattCallback(action));
    }



    protected class MicrobitGattCallback extends BluetoothGattCallback
    {
        private static final int STATE_DISCONNECTED = 0;
        private static final int STATE_CONNECTING = 1;
        private static final int STATE_CONNECTED = 2;

        private String action;
        private int connectionState;



        public MicrobitGattCallback()
        {
            super();
        }



        public MicrobitGattCallback(String action)
        {
            this();

            this.action = action;
        }



        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                connectionState = STATE_CONNECTED;
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                System.err.println("********** Connected to GATT server");
                System.err.println("********** Attempting to start service discovery:" + gatt.discoverServices());

            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                connectionState = STATE_DISCONNECTED;
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);
                System.err.println("********** Disconnected from GATT server.");
            }
        }



  /*      @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                final Intent intent = new Intent(ACTION_GATT_SERVICES_DISCOVERED);

                for(BluetoothGattService bluetoothGattService:gatt.getServices())
                {
                    if(bluetoothGattService.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER_UUID))
                    {
                        intent.putExtra(EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER, bluetoothGattService);

                        if(action.equals(ACTION_MICROBIT_ACCELEROMETER))
                        {
                            BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER_DATA_UUID));
                            bluetoothGatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_CLIENT_CHARACTERISTIC_CONFIG));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);
                        }
                    }
                    else if(bluetoothGattService.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER_UUID))
                    {
                        intent.putExtra(EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER, bluetoothGattService);

                        if(action.equals(ACTION_MICROBIT_MAGNETOMETER))
                        {
                            BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER_BEARING_UUID));
                            bluetoothGatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_CLIENT_CHARACTERISTIC_CONFIG));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);
                        }
                    }
                    else if(bluetoothGattService.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE_UUID))
                    {
                        intent.putExtra(EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE, bluetoothGattService);

                        if(action.equals(ACTION_MICROBIT_TEMPERATURE))
                        {
                            BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE_DATA_UUID));
                            bluetoothGatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_CLIENT_CHARACTERISTIC_CONFIG));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);
                        }
                    }
                }

                sendBroadcast(intent);
            }
            else
            {
                System.err.println("********** onServicesDiscovered received: " + status);
            }
        }*/



        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }



        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }



        private void broadcastUpdate(final String action)
        {
            final Intent intent = new Intent(action);
            sendBroadcast(intent);
        }



        private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic)
        {
            final Intent intent = new Intent();

           /* if(characteristic.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_ACCELEROMETER_DATA_UUID))
            {
                Boolean sendData = false;

                if(start_time == 0)
                {
                    start_time = System.currentTimeMillis();
                    sendData = true;
                }
                else
                {
                    if (System.currentTimeMillis() - start_time >= 100)
                    {
                        start_time = System.currentTimeMillis();
                        sendData = true;
                    }
                }

                if(sendData)
                {
                    intent.setAction(ACTION_DATA_AVAILABLE_ACCELEROMETER);

                    final byte[] data = characteristic.getValue();

                    if (data != null && data.length > 0) {
                        byte[] x_bytes = new byte[2];
                        byte[] y_bytes = new byte[2];
                        byte[] z_bytes = new byte[2];

                        System.arraycopy(data, 0, x_bytes, 0, 2);
                        System.arraycopy(data, 2, y_bytes, 0, 2);
                        System.arraycopy(data, 4, z_bytes, 0, 2);

                        short raw_x = MicrobitUtil.shortFromLittleEndianBytes(x_bytes);
                        short raw_y = MicrobitUtil.shortFromLittleEndianBytes(y_bytes);
                        short raw_z = MicrobitUtil.shortFromLittleEndianBytes(z_bytes);

                        intent.putExtra(EXTRA_DATA_1, raw_x);
                        intent.putExtra(EXTRA_DATA_2, raw_y);
                        intent.putExtra(EXTRA_DATA_3, raw_z);
                    } else {
                        intent.putExtra(EXTRA_DATA_1, (short) 0);
                        intent.putExtra(EXTRA_DATA_2, (short) 0);
                        intent.putExtra(EXTRA_DATA_3, (short) 0);
                    }
                }
            }
            else if(characteristic.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_TEMPERATURE_DATA_UUID))
            {
                intent.setAction(ACTION_DATA_AVAILABLE_TEMPERATURE);

                final byte[] data = characteristic.getValue();

                if (data != null && data.length > 0)
                {
                    intent.putExtra(EXTRA_DATA_1, (int)data[0]);
                }
                else
                {
                    intent.putExtra(EXTRA_DATA_1, 0);
                }
            }
            else if(characteristic.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_MAGNETOMETER_BEARING_UUID))
            {
                intent.setAction(ACTION_DATA_AVAILABLE_MAGNETOMETER);

                final byte[] data = characteristic.getValue();

                if (data != null && data.length > 0)
                {
                    byte[] bearing_bytes = new byte[2];
                    System.arraycopy(data, 0, bearing_bytes, 0, 2);
                    short bearing = MicrobitUtil.shortFromLittleEndianBytes(bearing_bytes);

                    intent.putExtra(EXTRA_DATA_1, bearing);
                    intent.putExtra(EXTRA_DATA_2, MicrobitUtil.compassPoint(bearing));
                }
                else
                {
                    intent.putExtra(EXTRA_DATA_1, (short)0);
                    intent.putExtra(EXTRA_DATA_2, "Unknown");
                }
            }*/

            sendBroadcast(intent);
        }
    }
}
