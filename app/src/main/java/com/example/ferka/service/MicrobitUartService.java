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

public class MicrobitUartService extends IntentService {

    public static final String ACTION_MICROBIT = "com.example.mysecondapp.service.action.ACTION_MICROBIT";
    public static final String ACTION_MICROBIT_STOP = "com.example.mysecondapp.service.action.ACTION_MICROBIT_STOP";
    public static final String ACTION_MICROBIT_UART = "com.example.mysecondapp.service.action.ACTION_MICROBIT_UART";

    public static final String EXTRA_BLUETOOTH_DEVICE = "com.example.mysecondapp.service.extra.EXTRA_BLUETOOTH_DEVICE";
    public static final String EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_UART = "com.example.mysecondapp.service.extra.EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_UART";
    public static final String EXTRA_DATA_1 = "com.example.mysecondapp.service.extra.EXTRA_DATA_1";

    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_UART_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_UART_RX_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_UART_TX_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    public static final String BLUETOOTH_GATT_SERVICE_MICROBIT_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_AVAILABLE_UART = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_UART";

    private BluetoothGatt bluetoothGatt;


    private long start_time = 0;



    public MicrobitUartService()
    {
        super("MicrobitUartService");
    }



    public static void startActionMicrobit(Context context, BluetoothDevice bluetoothDevice, String action)
    {
        System.err.println("********** startActionMicrobit()");
        Intent intent = new Intent(context, MicrobitUartService.class);
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
        Toast.makeText(this, "MicrobitUartService service is starting", Toast.LENGTH_SHORT).show();

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
                    action.equals(ACTION_MICROBIT_UART))
            {
                System.err.println("********** onHandleIntent");
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
        System.err.println("********** handleActionMicrobit()");
        bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, new MicrobitGattCallback(action));
    }



    protected class MicrobitGattCallback extends BluetoothGattCallback {
        private static final int STATE_DISCONNECTED = 0;
        private static final int STATE_CONNECTING = 1;
        private static final int STATE_CONNECTED = 2;

        private String action;
        private int connectionState;


        public MicrobitGattCallback() {
            super();
        }


        public MicrobitGattCallback(String action) {
            this();

            this.action = action;
        }


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                System.err.println("********** Connected to GATT server");
                System.err.println("********** Attempting to start service discovery:" + gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);
                System.err.println("********** Disconnected from GATT server.");
            }
        }


        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.err.println("********** onServicesDiscovered()");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                final Intent intent = new Intent(ACTION_GATT_SERVICES_DISCOVERED);

                for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
                    if (bluetoothGattService.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_UART_UUID)) {
                        intent.putExtra(EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_UART, bluetoothGattService);

                        BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_UART_RX_UUID));
                        bluetoothGatt.setCharacteristicNotification(characteristic, true);

                        System.err.println("********** HERE");

                        for (BluetoothGattDescriptor d : characteristic.getDescriptors()) {
                            System.err.println("********** Descriptor: " + d.getUuid());
                        }

                        // ENABLE_INDICATION_VALUE
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_GATT_SERVICE_MICROBIT_CLIENT_CHARACTERISTIC_CONFIG));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                    } else {
                        System.err.println("********** Discovered other services: " + bluetoothGattService.getUuid());
                    }
                }

                sendBroadcast(intent);
            } else {
                System.err.println("********** onServicesDiscovered received: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            System.err.println("********** onCharacteristicChanged()");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }


        private void broadcastUpdate(final String action) {
            final Intent intent = new Intent(action);
            sendBroadcast(intent);
        }


        private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic) {
            final Intent intent = new Intent();

            if (characteristic.getUuid().toString().equals(BLUETOOTH_GATT_SERVICE_MICROBIT_UART_RX_UUID)) {
                System.err.println("********** broadcastUpdate: BLUETOOTH_GATT_SERVICE_MICROBIT_UART_RX_UUID");

                intent.setAction(ACTION_DATA_AVAILABLE_UART);


                final byte[] data = characteristic.getValue();
                String value = new String(data);

                System.out.println("************* VALUE IS " + value);

                if (value.substring(0, 2).equals("CC")) {
                    Integer tableNo = Integer.parseInt(value.substring(8));
                    System.err.println("********** RX: " + "Call cashier from table number: " + tableNo);
                } else if (value.substring(0, 2).equals("FH")) {
                    //finish eating update the table
                } else if (value.substring(0, 2).equals("TP")) // in case there is other UART protocol.
                {
                    System.err.println("********** RX: " + "Temperature is: " + value);
                }
                // table<no> eg CC-table1



                if (data != null && data.length > 0) {
                    //MicrobitUtil.byteArrayToInteger(data)
                    intent.putExtra(EXTRA_DATA_1, value);
                } else {
                    intent.putExtra(EXTRA_DATA_1, 0);
                }
            }

            sendBroadcast(intent);
        }


    }


}
