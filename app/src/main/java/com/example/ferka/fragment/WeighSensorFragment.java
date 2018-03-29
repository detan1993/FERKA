package com.example.ferka.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeighSensorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeighSensorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeighSensorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;
    Handler mHandler;

    private DoAsyncTaskButtonAsyncTask doAsyncTaskButtonAsyncTask;

    private OnFragmentInteractionListener mListener;

    public WeighSensorFragment() {
        // Required empty public constructor
        mHandler = new Handler();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WeighSensorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeighSensorFragment newInstance(String param1, String param2) {
        WeighSensorFragment fragment = new WeighSensorFragment();
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

        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable,5000);
        m_Runnable.run();
    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()
        {
            retrieveContainer();
            System.out.println("Refresh Date :" + new Date());
            WeighSensorFragment.this.mHandler.postDelayed(m_Runnable,10000);
        }

    };
    protected class DoAsyncTaskButtonAsyncTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                System.out.println("Container webservice doInBackground()");

                SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                String restaurantId = sharedPref.getString("staff_restaurantId",null);; // get restaurant Id from local file
                System.out.println("Restaurant Id: " + restaurantId);

                URL url = new URL(getString(R.string.VM_address) + "FoodEmblemV1-war/Resources/Sensor/getContainersByRestaurantId/" + restaurantId);
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

            return "Async Task Completed";
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {

        }

        @Override
        protected void onPostExecute(String jsonString)
        {
            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("containers");
                List<HashMap<String,String>> containerList = new ArrayList<HashMap<String,String>>();

                System.out.println("Number of containers: " + jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    System.out.println("Inside Container number : " + i+1);
                    HashMap<String,String>  dataToStore = new HashMap<String,String>();
                    dataToStore.put("containerId" , "Id: " + jsonArray.getJSONObject(i).getString("id"));

                    JSONObject inventoryJSONObject = jsonArray.getJSONObject(i).getJSONObject("inventory");
                    dataToStore.put("inventoryId", "Id: " + inventoryJSONObject.getString("id"));
                    dataToStore.put("inventoryName", "Name: " + inventoryJSONObject.getString("name"));

                    double inventoryWeight = Double.parseDouble(inventoryJSONObject.getString("weight"));
                    double containerWeight = Double.parseDouble(jsonArray.getJSONObject(i).getString(   "weight"));
                    dataToStore.put("inventoryWeight", "Weight: " + String.valueOf(inventoryWeight-containerWeight));

                    containerList.add(dataToStore);
                }
                String[] from = {"containerId" , "inventoryId" , "inventoryName" , "inventoryWeight"};
                int[] to = {R.id.tv_container, R.id.tv_inventoryId , R.id.tv_inventoryName,  R.id.tv_inventoryWeight};
                SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), containerList, R.layout.containerlistview_layout, from, to);
                ListView listView = ( ListView ) view.findViewById(R.id.lv_inventory);
                listView.setAdapter(adapter);
            }

            catch(Exception ex){
                ex.printStackTrace();
            }
        }

    }

    public void retrieveContainer(){
        doAsyncTaskButtonAsyncTask = new DoAsyncTaskButtonAsyncTask();
        doAsyncTaskButtonAsyncTask.execute("Current progress");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_weigh_sensor, container, false);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
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

    @Override
    public void onStop() {
        super.onStop();

        //check the state of the task
        if(doAsyncTaskButtonAsyncTask != null && doAsyncTaskButtonAsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            doAsyncTaskButtonAsyncTask.cancel(true);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
