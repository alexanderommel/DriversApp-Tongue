package com.example.tongue_drivers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tongue_drivers.config.TongueNetworkSettings;
import com.example.tongue_drivers.databinding.ActivityMainBinding;
import com.example.tongue_drivers.databinding.FragmentHomeBinding;
import com.example.tongue_drivers.fragments.HomeFragment;
import com.example.tongue_drivers.fragments.LoginFragment;
import com.example.tongue_drivers.fragments.ShippingFragment;
import com.example.tongue_drivers.models.Driver;
import com.example.tongue_drivers.viewmodels.DriverViewModel;
import com.example.tongue_drivers.viewmodels.ShippingConnectionViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginButtonListener,
        HomeFragment.OnLogoutButtonListener,
        ShippingFragment.OnHomeButtonListener,
        ShippingFragment.OnConnectedButtonListener{
    
    //Fields
    private ActivityMainBinding binding;
    private FragmentHomeBinding homeBinding;
    private DrawerLayout drawer;
    private NavController navController;
    private GoogleSignInClient googleSignInClient;
    private DriverViewModel driverViewModel;
    private ShippingConnectionViewModel shippingConnectionViewModel;
    private static final int RC_SIGN_IN = 9001;
    private RequestQueue queue;
    private static final String loginUrl = "http://"+
            TongueNetworkSettings.domain +":"+TongueNetworkSettings.port+"/drivers/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        homeBinding = DataBindingUtil.setContentView(this, R.layout.fragment_home);
        setContentView(binding.getRoot());
        drawer = binding.homeDrawerLayout;
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_container);
        navController = navHostFragment.getNavController();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        CookieHandler.setDefault(new CookieManager());

        queue = Volley.newRequestQueue(this);

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        driverViewModel = new ViewModelProvider(this).get(DriverViewModel.class);

        shippingConnectionViewModel = new ViewModelProvider(this).get(ShippingConnectionViewModel.class);

    }


    @Override
    protected void onStart() {
        super.onStart();
        // Testing purposes
        //fastLogin();
        //Enable it on production
        silentSignInTask();
    }

    private void fastLogin(){
        Driver driver = new Driver();
        driver.setId("id");
        driver.setRating(4.4);
        driver.setName("Alexander");
        driverViewModel.setDriver(driver);
        navController.navigate(R.id.action_mainFragment_to_shippingFragment);
    }

    @Override
    public void onButtonClicked(View view) {
        switch (view.getId()){
            case R.id.frag_login_google_button:
                googleSignIn();
                break;
        }
    }


    @Override
    public void onLogoutClicked(View view) {
        switch (view.getId()) {
            case R.id.home_panel_logout_button:
                googleSignOut();
                break;
        }
    }


    @Override
    public void OnHomeButtonClicked(View view) {
        navController.navigate(R.id.action_shippingFragment_to_homeFragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 170005:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    navController.navigate(R.id.action_mainFragment_to_shippingFragment);
                }else {
                    // Explain to the user that Location Permissions are mandatory
                }
        }
    }

    private void googleSignIn(){
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Log.w(TAG,"Handling GoogleSignInTaskResult");
            Log.w(TAG,account.getEmail());
            Log.w(TAG,account.getIdToken());
            Log.w(TAG,account.getId());

            final Driver driver = new Driver();
            driver.setIdToken(account.getIdToken());


            Uri.Builder builder = Uri.parse(loginUrl).buildUpon();
            builder.appendQueryParameter("idToken", account.getIdToken());
            String loginUrl=builder.build().toString();

            StringRequest stringRequest = new StringRequest(Request.Method.GET, loginUrl, response -> {
                Driver driver1 = new Driver();
                driver1 = populateDriverFromStringResponse(response,driver);
                if (driver1!=null){
                    driverViewModel.setDriver(driver1);
                    homeBinding.setDriver(driverViewModel);
                    Log.w(TAG,"Navigate from LoginFragment to ShippingFragment");
                    navController.navigate(R.id.action_loginFragment_to_shippingFragment);
                }else {
                    googleSignInClient.signOut();
                    driverViewModel.setDriver(null);
                    Log.w(TAG,"Parsing failed");
                }

            }, error -> {
                googleSignInClient.signOut();
                driverViewModel.setDriver(null);
                Log.w(TAG,"String request failed");
                Log.w(TAG,error.toString());
                //Log.w(TAG,"Error "+error.networkResponse.statusCode);
                Log.w(TAG, String.valueOf(error.networkResponse==null));
                Log.w(TAG,"Failed Sign In");
            })


            {
                @Override
                protected Map<String, String> getParams(){
                    Map<String,String> params = new HashMap<>();
                    params.put("idToken",account.getIdToken());
                    return params;
                }


            };

            queue.getCache().clear();
            queue.add(stringRequest);

        }catch (ApiException e){
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            driverViewModel.setDriver(null);
        }
    }


    private void googleSignOut(){
        googleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        driverViewModel=null;
                        homeBinding.setDriver(null);
                        navController.navigate(R.id.action_homeFragment_to_loginFragment);
                    }
                });
    }

    private void silentSignInTask(){
        final Driver driver = new Driver();

        googleSignInClient.silentSignIn().
                addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {

                        Uri.Builder builder = Uri.parse(loginUrl).buildUpon();
                        builder.appendQueryParameter("idToken", googleSignInAccount.getIdToken());
                        String loginUrl=builder.build().toString();

                        driver.setIdToken(googleSignInAccount.getIdToken());
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, loginUrl, response -> {

                            Driver driver1 = new Driver();
                            driver1 = populateDriverFromStringResponse(response,driver);

                            if (driver1!=null){
                                driverViewModel.setDriver(driver1);
                                homeBinding.setDriver(driverViewModel);
                                Log.w(TAG,"Navigate from MainFragment to ShippingFragment");
                                navController.navigate(R.id.action_mainFragment_to_shippingFragment);
                            }else {
                                googleSignInClient.signOut();
                                Log.w(TAG,"Parsing failed");
                            }

                        }, error -> {
                            googleSignInClient.signOut();
                            Log.w(TAG,"String request failed");
                            //Log.w(TAG,"Error "+error.networkResponse.statusCode);
                            Log.w(TAG,error.toString());
                            Log.w(TAG,"Failed Silent Sign In");
                            Log.w(TAG,"Navigate from MainFragment to LoginFragment");
                            navController.navigate(R.id.action_mainFragment_to_loginFragment);
                        })



                        {
                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                try {
                                    String jsonString = new String(response.data,
                                            HttpHeaderParser.parseCharset(response.headers));
                                    JSONObject jsonResponse = new JSONObject(jsonString);
                                    jsonResponse.put("headers", new JSONObject(response.headers));
                                    String cookie = response.headers.get("Set-Cookie");
                                    String[] fields = cookie.split(";");
                                    String[] parts = fields[0].split("=");
                                    String sessionId = parts[1];
                                    driver.setSessionId(sessionId);
                                    Log.w("SESSION",sessionId);
                                    //int i1 = header.indexOf("sessionId");
                                    //int i2 = header.indexOf("; path");

                                    //String sessionId = header.substring(i1,i2);

                                    return Response.success(jsonString,HttpHeaderParser.parseCacheHeaders(response));

                                }catch (Exception e){
                                    Log.w("ERROR","Error parsing network");
                                    return Response.error(new ParseError(e));
                                }
                            }

                            @Override
                            protected Map<String, String> getParams(){
                                Log.w(TAG,"GetParams");
                                Map<String,String> params = new HashMap<>();
                                params.put("idToken",googleSignInAccount.getIdToken());
                                return params;
                            }

                            @Override
                            public String getBodyContentType() {
                                return "application/x-www-form-urlencoded; charset=UTF-8";
                            }

                        };

                        queue.getCache().clear();
                        queue.add(stringRequest);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.w(TAG,"Failed Silent Sign In");
                Log.w(TAG,"Navigate from MainFragment to LoginFragment");
                driverViewModel.setDriver(null);
                navController.navigate(R.id.action_mainFragment_to_loginFragment);
            }
        });
    }


    private static Driver populateDriverFromStringResponse(String response,Driver driver){
        try {
            Log.w(TAG,"Parsing response");
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonResponse = jsonObject.getJSONObject("response");
            driver.setName(jsonResponse.getString("name"));
            driver.setEmail(jsonResponse.getString("email"));
            driver.setCountryCode(jsonResponse.getString("countryCode"));
            driver.setFirstName(jsonResponse.getString("firstName"));
            driver.setLastName(jsonResponse.getString("lastName"));
            driver.setIdentification(jsonResponse.getString("identification"));
            driver.setPhoneNumber(jsonResponse.getString("phoneNumber"));
            driver.setRating(jsonResponse.getDouble("rating"));
            driver.setImageUrl(jsonResponse.getString("imageUrl"));
            return driver;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG,"Json parsing failed");
        }
        return null;
    }


    @Override
    public void OnConnectedButtonClicked(View view) {

    }
}
