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
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ferka.service.MicrobitUartService;
import com.example.ferka.service.MicrobitSensorService;

import com.example.ferka.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TemperatureSensorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tempOutput1;
    private TextView tempOutput2;
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
    private List<String> microbitIds ;

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
        microbitIds = new ArrayList<>();
        tempOutput1 = (TextView) view.findViewById(R.id.textViewTempResult1);
        tempOutput2 = (TextView) view.findViewById(R.id.textViewTempResult2);
    //    microbitIds.add("FF:43:88:BB:AA:E5");
        Button buttonDoUart = (Button) view.findViewById(R.id.button_pairTemperature);
        buttonDoUart.setOnClickListener(new pairTemperatureButtonListener());


        if(!checkBluetoothPermission()){

            System.err.println("**************** Please Request Bluetooth Permision");
            Toast.makeText(getActivity().getApplicationContext(), "Please Request Bluetooth Permision", Toast.LENGTH_LONG).show();

        }
        else
        {

            GetListOfFridgeMicrobitAysncTask a = new GetListOfFridgeMicrobitAysncTask();
            a.execute("GET ALL FRIDGE MICROBIT");
            //run aysn task here
            bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            leScanCallback = new TemperatureSensorFragment.BleScanner();
            handler.postDelayed(new TemperatureSensorFragment.StopBleScannerThread(), 45 * 1000);
            bluetoothAdapter.startLeScan(leScanCallback);

            updateTextViewtempOutput1("Scanning for your fridge sensor");
        }

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

                GetListOfFridgeMicrobitAysncTask a = new GetListOfFridgeMicrobitAysncTask();
                a.execute("GET ALL FRIDGE MICROBIT");
                //run aysn task here
                bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
                leScanCallback = new TemperatureSensorFragment.BleScanner();
                handler.postDelayed(new TemperatureSensorFragment.StopBleScannerThread(), 45 * 1000);
                bluetoothAdapter.startLeScan(leScanCallback);

                updateTextViewtempOutput1("Scanning for your fridge sensor");
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

                  if(!microbitIds.isEmpty()){
                      System.err.println("****************** PRINTING MICROBIT SIZE "+ microbitIds.size());
                       for(int noOfMicrobit=0; noOfMicrobit<microbitIds.size(); noOfMicrobit++){
                           System.err.println("BleScannerThread.run(): INSIDE LOOP device name " + bluetoothDevice.getName());
                           System.err.println("BleScannerThread.run(): INSIDE LOOP device address detected : " + bluetoothDevice.getAddress());


                           String restaurantMicrobitID = microbitIds.get(noOfMicrobit);
                           System.err.println("BleScannerThread.run(): INSIDE LOOP Restaurant MICROBIT : " + restaurantMicrobitID);
                           //getString(R.string.key_microbit1Name)
                        if(bluetoothDevice.getAddress().equals(restaurantMicrobitID))
                        {
                            //bluetoothDevice.getName() != null &&

                            System.err.println("BleScannerThread.run() : " + bluetoothDevice.getName() + " " + bluetoothDevice.getUuids());

                            bluetoothAdapter.stopLeScan(leScanCallback);
                            bluetoothDevices.put(restaurantMicrobitID ,  bluetoothDevice);
                           // bluetoothDevice.put()
                        //  bluetoothDevices.put(getString(R.string.key_microbit1Name), bluetoothDevice);

                            String microbitValue = String.valueOf(noOfMicrobit+1);
                            updateTextViewtempOutput(  microbitValue ,"Found Your Fridge Sensor ID" +  microbitIds.get(noOfMicrobit));

                           // updateTextViewtempOutput1("Found Your Fridge Sensor ID" + microbitIds.get(noOfMicrobit) );

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


        }
    }



    protected class StopBleScannerThread implements Runnable
    {
        @Override
        public void run()
        {
            Boolean foundMicrobit = false;

            for(int i=0; i<microbitIds.size(); i++){
                if (bluetoothDevices.get(microbitIds.get(i)) != null){
                    foundMicrobit = true;
                    break;
                }
            }
        //    if (bluetoothDevices.get(getString()) != null)
        //    if(microbitIds.contains(bluetoothDevices.get())
        //    {
         //       foundMicrobit = true;
          //  }

            bluetoothAdapter.stopLeScan(leScanCallback);

            if(!foundMicrobit)
            {
                updateTextViewtempOutput1("Unable to find your fridge sensor. Try Again!");
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
                updateTextViewtempOutput1("*** Discovered the following services");
                BluetoothGattService gattServiceMicrobitUart = intent.getParcelableExtra(MicrobitUartService.EXTRA_BLUETOOTH_GATT_SERVICE_MICROBIT_UART);

                updateTextViewtempOutput1("MicroBit UART Service: " + gattServiceMicrobitUart.getUuid().toString());
            }
            else if (action.equals(MicrobitSensorService.ACTION_DATA_AVAILABLE))
            {
                updateTextViewtempOutput1("Unexpected data!");
            }
            else if (action.equals(MicrobitUartService.ACTION_DATA_AVAILABLE_UART))
            {


                System.err.println("**************(action.equals(MicrobitUartService.ACTION_DATA_AVAILABLE_UART))");
                String tempValue = intent.getStringExtra(MicrobitSensorService.EXTRA_DATA_1);
                String[] tempValue_FridgeID = intent.getStringExtra(MicrobitSensorService.EXTRA_DATA_1).split("_");
                String restaurantId = "1";
                String fridgeId = tempValue_FridgeID[1];
                String fridgeTemp  = tempValue_FridgeID[0];
                //updateTextViewUartOutput("Command: " + intent.getIntExtra(MicrobitSensorService.EXTRA_DATA_1, -1));
                System.err.println("**************** Calling Updating fridge Async task + FRIDGE ID " + fridgeId  + " tem Value " + fridgeTemp);
                updateFridge = new UpdateFridgeTemperatureAsyncTask(restaurantId, fridgeId, fridgeTemp);
                updateFridge.execute("Update Fridge in progress");


               updateTextViewtempOutput(fridgeId , fridgeTemp.substring(2) );

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

              /*  if(fridgeId.equals("1")){
                    updateTextViewtempOutput1(fridgeTemp.substring(2));
                }else if(fridgeId.equals("2")) {
                    updateTextViewtempOutput2(fridgeTemp.substring(2));
                }*/


            }
        }


    }


    private void updateTextViewtempOutput(String textboxId, String val)
    {
        //   tempOutput.append("\r\n" + val);
        if(textboxId.equals("1")){
            tempOutput1.setText(val + " Celcius");
        }
        else if(textboxId.equals("2")){
            tempOutput2.setText(val + " Celcius");
        }

    }



    private void updateTextViewtempOutput1( String val)
    {
     //   tempOutput.append("\r\n" + val);
        tempOutput1.setText(val + " Celcius");
        //tempOutput2.setText(val + " Celcius");
    }

    private void updateTextViewtempOutput2(String val){
        tempOutput2.setText(val + " Celcius");
    }

   private void updateTextViewtempOutput1(String fridgeId, String val){

       tempOutput1.setText(val + " Celcius");
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

                    SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    String restaurantId = sharedPref.getString("staff_restaurantId",null);; // get restaurant Id from local file
                    System.out.println("Restaurant Id: " + restaurantId);

                    URL url = new URL(getString(R.string.VM_address) + "FoodEmblemV1-war/Resources/Sensor/updateRestaurantFridgeTemp");
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



    protected class GetListOfFridgeMicrobitAysncTask extends AsyncTask<String, Integer, String> {
        private String temperatureToUpdateValue;
        private String restaurantId;
        private String fridgeId;

        public GetListOfFridgeMicrobitAysncTask() {
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
                    System.err.println("********** Calling Get All Fridge RESTful web service");
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    String restaurantId = sharedPref.getString("staff_restaurantId",null);; // get restaurant Id from local file
                    System.out.println("Restaurant Id: " + restaurantId);

                    System.err.println("********** Calling get Fridges Rest web service");
                    URL url = new URL(getString(R.string.VM_address) + "FoodEmblemV1-war/Resources/Sensor/getRestaurantAllFridgeSensors/" + restaurantId + "");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line = null;

                    while ((line = bufferedReader.readLine()) != null)
                    {
                        stringBuilder.append(line);
                    }

                    return stringBuilder.toString(); //complete json string

                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }

                return "";



            } catch (Exception ex) {

                System.err.println("**************** error calling fridge sensor API");


                ex.printStackTrace();
            }


            return "Async Task Completed";
        }

        @Override
        protected void onPostExecute(String jsonString) {

            //this is how we populate the value obtained from web service to list view.
            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("sensors");
                //jsonObject.getString()
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    microbitIds.add(jsonArray.getJSONObject(i).getString("sensorId"));
                    System.err.println("********************JSONRESULT IS " + jsonArray.getJSONObject(i).getString("sensorId"));
                }


            }

            catch(Exception ex){

            }


        }
    }



}
