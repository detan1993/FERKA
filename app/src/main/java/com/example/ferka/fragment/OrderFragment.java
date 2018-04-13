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
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.ferka.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View view;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Handler handler;
    private DoRetrieveOrderAsyncTask doRetrieveOrderAsyncTask;

    public OrderFragment() {
        // Required empty public constructor

        handler = new Handler();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderFragment newInstance(String param1, String param2) {
        OrderFragment fragment = new OrderFragment();
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

        this.handler = new Handler();
       // this.handler.postDelayed(m_Runnable,10000);
        m_Runnable.run();
    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()
        {
            retrieveContainer();
            System.out.println("Refresh Date :" + new Date());
            OrderFragment.this.handler.postDelayed(m_Runnable,10000);
        }

    };


    protected class DoRetrieveOrderAsyncTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            if(!isAdded()) {
                return "";
            }
            try
            {
                System.out.println("***************************Order webservice doInBackground()");
                SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                String restaurantId = sharedPref.getString("staff_restaurantId",null);; // get restaurant Id from local file
                System.out.println("***************************Restaurant Id: " + restaurantId);

                URL url = new URL(getString(R.string.VM_address) + "FoodEmblemV1-war/Resources/Restaurant/retrieveCustomerOrders/" + restaurantId);
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
        protected void onPostExecute(String jsonString)
        {
            if(!isAdded()) {
                return;
            }

            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("customerOrders");
                System.out.println("***************************Number of Orders: " + jsonArray.length());

                List<Order> orderList = new ArrayList<Order>();
                for(int i=0; i <jsonArray.length();i++){
                    String orderId = jsonArray.getJSONObject(i).getString("orderId");
                    String dishName = jsonArray.getJSONObject(i).getString("dishName");
                    String quantity = jsonArray.getJSONObject(i).getString("qty");
                    long orderIdLong = Long.parseLong(orderId);

                    Dish dish = new Dish(dishName,Integer.parseInt(quantity));
                    boolean isInOrder = false;
                    for(Order o : orderList){
                        if(o.orderId == orderIdLong){
                            isInOrder = true;
                            o.dish.add(dish);
                        }
                    }
                    if(!isInOrder){
                        Order newOrder = new Order(orderIdLong,dish);
                        orderList.add(newOrder);
                    }
                }

                final TableLayout tableLayout =(TableLayout) getActivity().findViewById(R.id.tb_layout);
                TableRow.LayoutParams  params1=new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
                TableRow.LayoutParams params2=new TableRow.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                tableLayout.removeAllViewsInLayout();

                for(final Order o : orderList){
                    final Order finalOrder = o;
                    TableRow tr_top = new TableRow(getContext());
                    TextView tv_orderId = new TextView(getContext());

                    tv_orderId.setText("Order Id: #" + o.orderId);
                    tv_orderId.setLayoutParams(params1);

                    tr_top.addView(tv_orderId, params1);
                    tableLayout.addView(tr_top,params2);

                    for(Dish d : o.dish){
                        TableRow tr_middle = new TableRow(getContext());

                        TextView tv_dishId = new TextView(getContext());
                        tv_dishId.setLayoutParams(params1);
                        TextView tv_quantity =new TextView(getContext());
                        tv_quantity.setLayoutParams(params1);

                        tv_dishId.setText("Name: " + d.dishName);
                        tv_quantity.setText("Quantity: " + d.quantity);

                        tr_middle.addView(tv_dishId, params1);
                        tr_middle.addView(tv_quantity, params1);
                        tableLayout.addView(tr_middle,params2);
                    }

                    TableRow tr_bottom = new TableRow(getContext());

                    Button myButton = new Button(getActivity());
                    myButton.setText("Complete Dish");
                    myButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UpdateCookingWebService(finalOrder.orderId);
                            TableRow row = (TableRow) v.getParent();
                            DestroyRows(tableLayout,o, row);
                        }
                    });

                    tr_bottom.addView(myButton, params1);
                    tableLayout.addView(tr_bottom,params2);
                }
            }

            catch(Exception ex){
                ex.printStackTrace();
            }
        }

        private class Order{
            long orderId;
            List<Dish> dish;

            public Order(){

            }

            public Order(long orderId, Dish dish){
                this.orderId = orderId;
                this.dish = new ArrayList<Dish>();
                this.dish.add(dish);
            }
            public Order(long orderId, List<Dish> dish){
                this.orderId = orderId;
                this.dish = dish;
            }

            public boolean indexOfOrder(long orderId){
                return this.orderId == orderId;
            }
        }
        private class Dish{
            String dishName;
            int quantity;

            public Dish(){

            }
            public Dish(String dishName, int quantity){
                this.dishName = dishName;
                this.quantity = quantity;
            }
        }
        private void DestroyRows(TableLayout tableLayout, Order o, TableRow row){

            int index = tableLayout.indexOfChild(row);
            ViewGroup container = ((ViewGroup)row.getParent());
            container.removeView(row);
            container.invalidate();

            System.out.println("************INDEX OF BUTTON IS : " + index + "******************");
            for(int i=1;i<o.dish.size()+2;i++){
                System.out.println("***************** GOING TO REMOVE INDEX: " + (index-i));
                TableRow r = (TableRow)tableLayout.getChildAt(index-i);
                ViewGroup c = ((ViewGroup)r.getParent());
                c.removeView(r);
                c.invalidate();
            }
        }
    }



    public void UpdateCookingWebService(long id){
        final long orderId = id;
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... voids)
            {
                try
                {
                    System.out.println("*************************UPDATE COOKING WS CALLED: " + orderId);
                    String wsPath = getString(R.string.VM_address) + "FoodEmblemV1-war/Resources/Customer/updateCustomerOrder";
                    URL url = new URL(wsPath);

                    System.out.print("*************************** doInBackground() - Web Service Path: " + wsPath);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setRequestProperty("Accept", "application/json");

                    JSONObject orderReq = new JSONObject();
                    orderReq.put("orderId",orderId);

                    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr.writeBytes(orderReq.toString());
                    wr.flush();
                    wr.close();

                    InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line = null;

                    while ((line = bufferedReader.readLine()) != null)
                    {
                        stringBuilder.append(line);
                    }

                    return stringBuilder.toString();

                }catch(Exception ex){

                    ex.printStackTrace();
                }

                return "";
            }

            @Override
            protected void onPostExecute(String jsonString)
            {
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    String result = jsonObject.getString("isUpdated");
                    System.out.println("******************** IS UPDATED: " + result);
                }catch(Exception ex){

                }
            }
        }.execute();
    }
    public void retrieveContainer(){
        doRetrieveOrderAsyncTask = new DoRetrieveOrderAsyncTask();
        doRetrieveOrderAsyncTask.execute("Current progress");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order, container, false);

        return view;
    }

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
        if(doRetrieveOrderAsyncTask != null && doRetrieveOrderAsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            doRetrieveOrderAsyncTask.cancel(true);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
