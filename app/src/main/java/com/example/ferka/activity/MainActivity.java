package com.example.ferka.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;


import com.example.ferka.R;
import com.example.ferka.fragment.HomeFragment;
import com.example.ferka.fragment.OrderFragment;
import com.example.ferka.fragment.TemperatureSensorFragment;

import com.example.ferka.fragment.WeighSensorFragment;
import com.example.ferka.util.BottomNavigationViewHelper;




public class MainActivity extends AppCompatActivity implements TemperatureSensorFragment.OnFragmentInteractionListener , WeighSensorFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener,
OrderFragment.OnFragmentInteractionListener{

    private TextView mTextMessage;

    private FragmentManager fragmentManager;
    private TemperatureSensorFragment tempFragment;
    private HomeFragment homeFragment;
    private WeighSensorFragment weightFragment;
    private OrderFragment orderFragment;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    swithcHome();
                    return true;
                case R.id.navigation_dashboard:
                    switchTemperature();
                    return true;
                case R.id.navigation_notifications:
                    switchWeight();
                    return true;

            }
            return false;
        }
    };

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.removeShiftMode(navigation);

        fragmentManager = getSupportFragmentManager();
        tempFragment = new TemperatureSensorFragment();
        weightFragment = new WeighSensorFragment();
        homeFragment = new HomeFragment();

        swithcHome();

    }



    private void switchTemperature()
    {
        fragmentManager.beginTransaction().replace(R.id.frameLayout, tempFragment).commit();
    }




    private void switchWeight()
    {
        fragmentManager.beginTransaction().replace(R.id.frameLayout, weightFragment ).commit();

    }


    private void swithcHome(){

        fragmentManager.beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
    }

    private void switchOrder(){
        fragmentManager.beginTransaction().replace(R.id.frameLayout, orderFragment).commit();
    }


    private void exit()
    {
        finish();
    }



    @Override
    public void onFragmentInteraction(Uri uri)
    {
    }



   /* @Override
    public void onFragmentInteraction(String serverHostname, String serverPort, String username)
    {
        System.err.println("********8onFragmentInteraction(String serverHostname, String serverPort, String username)******");

       // System.err.println("********** returned serverHostname: " + this.serverHostname);
    }*/





}
