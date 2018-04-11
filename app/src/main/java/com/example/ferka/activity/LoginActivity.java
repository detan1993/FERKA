package com.example.ferka.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.ferka.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void LoginWebservice(View view){
        new AsyncTask<Void,Void,String>(){
            @Override
            protected void onPreExecute()
            {

            }

            @Override
            protected String doInBackground(Void... voids)
            {
                try
                {
                    EditText email_tb = findViewById(R.id.email_tb);
                    EditText password_tb = findViewById(R.id.password_tb);

                    String email = email_tb.getText().toString();
                    String pwd = password_tb.getText().toString();

                    //Web service need 2 parameters
                    if(email.length() <1 || pwd.length()<1){
                        email = "fakeemail";
                        pwd = "fakepwd";
                    }

                    String wsPath = getString(R.string.VM_address) + "FoodEmblemV1-war/Resources/RestaurantEmployee/login/" + email + "/" + pwd;
                    URL url = new URL(wsPath);

                    System.err.print("*************************** doInBackground() - Web Service Path: " + wsPath);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestProperty("Accept","*/*");
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

                    String email = jsonObject.getString("email");
                    String fullName = jsonObject.getString("fullName");
                    String restaurantId = jsonObject.getString("restaurantId");
                    String gender = jsonObject.getString("gender");
                    String restaurantRole = jsonObject.getString("restaurantRole");

                    System.err.print("*************************** onPostExecute() - jsonObject : " + jsonObject);
                    System.err.print("*************************** onPostExecute() - jsonObject :email: " + email);
                    System.err.print("*************************** onPostExecute() - jsonObject :fullName " + fullName);
                    System.err.print("*************************** onPostExecute() - jsonObject :restaurantId " + restaurantId);
                    System.err.print("*************************** onPostExecute() - jsonObject :gender " + gender);
                    System.err.print("*************************** onPostExecute() - jsonObject :restaurantRole " + restaurantRole);

                    if(!gender.equalsIgnoreCase("n")) {
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString("staff_email", email);
                        editor.putString("staff_name", fullName);
                        editor.putString("staff_restaurantId", restaurantId);
                        editor.putString("staff_gender", gender);
                        editor.putString("staff_restaurantRole", restaurantRole);
                        editor.commit();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }else{
                        System.err.print("Wrong userName/Password");
                        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                        alert.setTitle("Error");
                        alert.setMessage("Wrong userName/Password");
                        alert.setPositiveButton("OK",null);
                        alert.show();
                    }

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

        }.execute();
    }
}
