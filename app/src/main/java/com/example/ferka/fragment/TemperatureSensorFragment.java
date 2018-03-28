package com.example.ferka.fragment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ferka.service.MicrobitUartService;
import com.example.ferka.service.MicrobitSensorService;

import com.example.ferka.R;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class TemperatureSensorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tempOutput;
    private View view;

    Handler handler;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGatt bluetoothGatt;
    private HashMap<String, BluetoothDevice> bluetoothDevices;
    private MicrobitUartBroadcastReceiver microbitUartBroadcastReceiver;

    private Boolean connected;
    private UpdateFridgeTemperatureAsyncTask updateFridge;
    public static int notificationId = 1;

    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 800;


    private OnFragmentInteractionListener mListener;

    public TemperatureSensorFragment() {
        // Required empty public constructor

            handler = new Handler();
            bluetoothDevices = new HashMap<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TemperatureSensorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TemperatureSensorFragment newInstance(String param1, String param2) {
        TemperatureSensorFragment fragment = new TemperatureSensorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_temperature_sensor, container, false);
        tempOutput = (TextView) view.findViewById(R.id.textView_Result);

        Button buttonDoUart = (Button) view.findViewById(R.id.button_pairTemperature);
        buttonDoUart.setOnClickListener(new pairTemperatureButtonListener());

        return view;

    }



    private boolean checkBluetoothPermission(){


        System.err.println("************* check permission and request permission");
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
        {
            System.err.println("***********Bluetooth");
            return false;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
        {
            System.err.println("***********BluetoothAmin");
            return false;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            System.err.println("***********Bluetooth Coarse Location");
             return false;

        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            System.err.println("***********Fine Location");
            return false;

        }

        return true;

    }

    protected class pairTemperatureButtonListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            if(!checkBluetoothPermission()){

                System.err.println("**************** Please Request Bluetooth Permision");
                Toast.makeText(getActivity().getApplicationContext(), "Please Request Bluetooth Permision", Toast.LENGTH_LONG).show();

            }
            else
            {
                bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
                leScanCallback = new TemperatureSensorFragment.BleScanner();
                handler.postDelayed(new TemperatureSensorFragment.StopBleScannerThread(), 30 * 1000);
                bluetoothAdapter.startLeScan(leScanCallback);

                updateTextViewtempOutput("Scanning for ...sensor");
            }

        }
    }


    public void onButtonPressed(Uri uri)
    {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    protected class BleScanner implements BluetoothAdapter.LeScanCallback
    {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes)
        {
            getActivity().runOnUiThread(new TemperatureSensorFragment.BleScannerThread(bluetoothDevice));
        }
    }



    protected class BleScannerThread implements Runnable
    {
        private BluetoothDevice bluetoothDevice;



        public BleScannerThread()
        {
            super();
        }



        public BleScannerThread(BluetoothDevice bluetoothDevice)
        {
            this();

            this.bluetoothDevice = bluetoothDevice;
        }



        @Override
        public void run()
        {
            if(bluetoothDevice.getName() != null && bluetoothDevice.getName().equals(getString(R.string.key_microbit1Name)))
            {
                System.err.println("BleScannerThread.run(): " + bluetoothDevice.getName());

                bluetoothAdapter.stopLeScan(leScanCallback);
                bluetoothDevices.put(getString(R.string.key_microbit1Name), bluetoothDevice);

                updateTextViewtempOutput("Found Sensor BBC micro:bit 1!");

                microbitUartBroadcastReceiver = new TemperatureSensorFragment.MicrobitUartBroadcastReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(MicrobitUartService.ACTION_GATT_CONNECTED);
                intentFilter.addAction(MicrobitUartService.ACTION_GATT_DISCONNECTED);
                intentFilter.addAction(MicrobitUartService.ACTION_GATT_SERVICES_DISCOVERED);
                intentFilter.addAction(MicrobitUartService.ACTION_DATA_AVAILABLE);
                intentFilter.addAction(MicrobitUartService.ACTION_DATA_AVAILABLE_UART);
                getActivity().registerReceiver(microbitUartBroadcastReceiver, intentFilter);

                String action = MicrobitUartService.ACTION_MICROBIT_UART;

                MicrobitUartService.startActionMicrobit(getActivity(), bluetoothDevice, action);
            }
        }
    }



    protected class StopBleScannerThread implements Runnable
    {
        @Override
        public void run()
        {
            Boolean foundMicrobit = false;

            if(bluetoothDevices.get(getString(R.string.key_microbit1Name)) != null)
            {
                foundMicrobit = true;
            }

            bluetoothAdapter.stopLeScan(leScanCallback);

            if(!foundMicrobit)
            {
                updateTextViewtempOutput("Unable to find sensor. Try Again!");
            }
        }
    }



    protected class MicrobitUartBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (action.equals(MicrobitSensorService.ACTION_GATT_CONNECTED))
            {
                connected = true;
            }
            else if (action.equals(MicrobitSensorService.ACTION_GATT_DISCONNECTED))
            {
                connected = false;
            }
            else if (action.equals(MicrobitSensorService.ACTION_GATT_SERVICES_DISCOVERED))
            {
                updateTextViewtempOutput("*** Discovered the following services");
                BluetoothGattService gattServiceMicrobitUart = intent.getParcelableExtra(MicrobitUartService.EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_UART);

                updateTextViewtempOutput("MicroBit UART Service: " + gattServiceMicrobitUart.getUuid().toString());
            }
            else if (action.equals(MicrobitSensorService.ACTION_DATA_AVAILABLE))
            {
                updateTextViewtempOutput("Unexpected data!");
            }
            else if (action.equals(MicrobitUartService.ACTION_DATA_AVAILABLE_UART))
            {


                System.err.println("**************(action.equals(MicrobitUartService.ACTION_DATA_AVAILABLE_UART))");
                String tempValue = intent.getStringExtra(MicrobitSensorService.EXTRA_DATA_1);
                String restaurantId = "1";
                String fridgeId = "1";
                //updateTextViewUartOutput("Command: " + intent.getIntExtra(MicrobitSensorService.EXTRA_DATA_1, -1));
                System.err.println("**************** Calling Updating fridge Async task");
                updateFridge = new UpdateFridgeTemperatureAsyncTask(restaurantId, fridgeId, tempValue);
                updateFridge.execute("Update Fridge in progress");

                if(tempValue.equals("CC-Table1"))
                {

                    String tittle="Helllo";
                    String subject="Waiter Enquiry Calling";
                    String body="Table No 1";

                    NotificationManager notif=(NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notify=new Notification.Builder    (getActivity().getApplicationContext()).setContentTitle(tittle).setContentText(body).
                            setContentTitle(subject).setSmallIcon(R.drawable.ic_launcher_background).setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 }).
                            setDefaults(Notification.DEFAULT_SOUND).build();

                    notify.flags |= Notification.FLAG_AUTO_CANCEL;
                    notif.notify(notificationId, notify);

                }
                updateTextViewtempOutput( tempValue.substring(2));






            }
        }


    }



    private void updateTextViewtempOutput(String val)
    {
     //   tempOutput.append("\r\n" + val);
        tempOutput.setText(val + " Celcius");
    }


    private void doUpdateFridge(String value)
    {
        System.err.println("***************Update Fridge Value " + value);

    }



    protected class UpdateFridgeTemperatureAsyncTask extends AsyncTask<String, Integer, String> {
        private String temperatureToUpdateValue;
        private String restaurantId;
        private String fridgeId;

        public UpdateFridgeTemperatureAsyncTask() {


        }

        public UpdateFridgeTemperatureAsyncTask(String restaurantId, String fridgeId ,String temperatureToUpdateValue) {

            System.err.println("******************* initialize contructor UpdateFridgeTemperatureAsyn");
            this.restaurantId = restaurantId;
            this.fridgeId = fridgeId;
            this.temperatureToUpdateValue = temperatureToUpdateValue.substring(2);
            System.err.println("**************** TEMP IS  " + this.temperatureToUpdateValue);

        }

        public String getRestaurantId(){
            return restaurantId;
        }

        public String getTemperatureValue() {
            return temperatureToUpdateValue;
        }

        public String getFridgeId(){

            return fridgeId;
        }

        @Override
        public void onPreExecute() {
        }

        @Override
        public String doInBackground(String... params) {

            System.out.println("*************** param[0] " + params[0]);
            try {

                try
                {
                    System.err.println("********** Calling RESTful web service");

                    URL url = new URL("http://192.168.43.203:3446/FoodEmblemV1-war/Resources/Sensor/updateRestaurantFridgeTemp");
                    //        URL url = new URL("http://" + + ":" + editTextServerPort.getText().toString() + "/JavaRestfulWebServices/Resources/Register");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestMethod("PUT");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    httpURLConnection.setRequestProperty("Accept","application/json");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.connect();

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("restaurantId", getRestaurantId());
                    jsonParam.put("fridgeId" , getFridgeId());
                    jsonParam.put("tempValue" , getTemperatureValue());
                    System.err.println("DATA: " + jsonParam.toString());
                    httpURLConnection.getOutputStream().write(jsonParam.toString().getBytes());
                    httpURLConnection.getOutputStream().flush();
                    httpURLConnection.getOutputStream().close();

                    System.err.println("********** STATUS: " + String.valueOf(httpURLConnection.getResponseCode()));

                    httpURLConnection.disconnect();

                    return httpURLConnection.getResponseMessage();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }

                return "";



            } catch (Exception ex) {

                System.out.println("erro calling API");


                ex.printStackTrace();
            }


            return "Async Task Completed";
        }

        @Override
        protected void onPostExecute(String jsonString) {



            System.err.println("Update succesfully");
        }
    }


}
