package com.example.tongue_drivers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tongue_drivers.databinding.ActivityMainBinding;
import com.example.tongue_drivers.fragments.HomeFragment;
import com.example.tongue_drivers.fragments.LoginFragment;
import com.example.tongue_drivers.fragments.ShippingFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginButtonListener,
        HomeFragment.OnLogoutButtonListener,
        ShippingFragment.OnHomeButtonListener {
    
    //Fields
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private NavController navController;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private Boolean authenticated=Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
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



        googleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<GoogleSignInAccount> task) {
                Log.w(TAG,"Already authenticated");
                handleSignInResult(task);
            }
        });

        if (authenticated==Boolean.FALSE){
            Log.w(TAG,"Navigate from MainFragment to LoginFragment");
            navController.navigate(R.id.action_mainFragment_to_loginFragment);
        }

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
            handleSignInResult(task);
        }
    }

    private void googleSignIn(){
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.w(TAG,account.getEmail());
            Log.w(TAG,account.getIdToken());
            Log.w(TAG,account.getId());
            navController.navigate(R.id.action_loginFragment_to_shippingFragment);
            authenticated=Boolean.TRUE;
            // WHEN SIGN IN FINISHED, CHANGE THE FRAGMENT
        }catch (ApiException e){
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
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

    private void googleSignOut(){
        googleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        navController.navigate(R.id.action_homeFragment_to_loginFragment);
                        authenticated=Boolean.FALSE;
                    }
                });
    }

    @Override
    public void OnHomeButtonClicked(View view) {
        navController.navigate(R.id.action_shippingFragment_to_homeFragment);
    }
}
