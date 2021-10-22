package com.example.tongue_drivers;

import android.content.Intent;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tongue_drivers.databinding.ActivityMainBinding;
import com.example.tongue_drivers.databinding.FragmentHomeBinding;
import com.example.tongue_drivers.fragments.HomeFragment;
import com.example.tongue_drivers.fragments.LoginFragment;
import com.example.tongue_drivers.fragments.ShippingFragment;
import com.example.tongue_drivers.models.Driver;
import com.example.tongue_drivers.viewmodels.DriverViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginButtonListener,
        HomeFragment.OnLogoutButtonListener,
        ShippingFragment.OnHomeButtonListener {
    
    //Fields
    private ActivityMainBinding binding;
    private FragmentHomeBinding homeBinding;
    private DrawerLayout drawer;
    private NavController navController;
    private GoogleSignInClient googleSignInClient;
    private DriverViewModel driverViewModel;
    private static final int RC_SIGN_IN = 9001;

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

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        driverViewModel = new ViewModelProvider(this).get(DriverViewModel.class);

    }

    @Override
    protected void onStart() {
        super.onStart();

        googleSignInClient.silentSignIn().addOnSuccessListener(this, new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                Log.w(TAG,"Silent Sign In");
                //populateSuccessfulLoginData(googleSignInAccount);
                Driver driver = new Driver();
                driver.setId(googleSignInAccount.getId());
                Log.w(TAG,"Sending IdToken to backend");
                sendIdTokenToServer(googleSignInAccount.getIdToken(),driver);
                if (driverViewModel!=null){
                    homeBinding.setDriver(driverViewModel);
                    Log.w(TAG,"Navigate from MainFragment to ShippingFragment");
                    navController.navigate(R.id.action_mainFragment_to_shippingFragment);
                }else {
                    googleSignInClient.signOut();
                    navController.navigate(R.id.action_mainFragment_to_loginFragment);
                }

                //navController.popBackStack();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.w(TAG,"Failed Silent Sign In");
                Log.w(TAG,"Navigate from MainFragment to LoginFragment");
                navController.navigate(R.id.action_mainFragment_to_loginFragment);
            }
        });

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Boolean handled = handleSignInResult(task);
            if (handled){
                homeBinding.setDriver(driverViewModel);
                navController.navigate(R.id.action_loginFragment_to_shippingFragment);
            }
        }
    }

    private void googleSignIn(){
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private Boolean handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Log.w(TAG,account.getEmail());
            Log.w(TAG,account.getIdToken());
            Log.w(TAG,account.getId());

            Driver driver = new Driver();
            driver.setId(account.getId());
            sendIdTokenToServer(account.getIdToken(),driver);


            //populateSuccessfulLoginData(account);
            return driverViewModel.getDriver()!=null;
            // WHEN SIGN IN FINISHED, CHANGE THE FRAGMENT
        }catch (ApiException e){
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
        return Boolean.FALSE;
    }



    @Override
    public void onLogoutClicked(View view) {
        switch (view.getId()) {
            case R.id.home_panel_logout_button:
                googleSignOut();
                break;
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

    private void sendIdTokenToServer(String idToken,Driver driver){
        Boolean solved = Boolean.FALSE;
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.101.12:8081/drivers/login";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
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
                Log.w(TAG,"Successfully driver attaching to view model");
                driverViewModel.setDriver(driver);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(TAG,"Json parsing failed");
            }
        }, error -> {
            Log.w(TAG,"String request failed");
            Log.w(TAG,"Error "+error.networkResponse.statusCode);
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("idToken",idToken);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    @Override
    public void OnHomeButtonClicked(View view) {
        navController.navigate(R.id.action_shippingFragment_to_homeFragment);
    }
}
