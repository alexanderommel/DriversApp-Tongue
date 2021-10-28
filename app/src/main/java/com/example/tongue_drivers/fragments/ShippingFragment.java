package com.example.tongue_drivers.fragments;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.tongue_drivers.R;
import com.example.tongue_drivers.databinding.FragmentShippingBinding;
import com.example.tongue_drivers.viewmodels.DriverViewModel;
import com.example.tongue_drivers.viewmodels.ShippingConnectionViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.content.ContentValues.TAG;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class ShippingFragment extends Fragment {

    //Fields
    private FragmentShippingBinding binding;
    private OnHomeButtonListener homeButtonListener;
    private OnConnectedButtonListener connectedButtonListener;
    private DriverViewModel driverViewModel;
    private Boolean isClicked;
    public ValueAnimator onConnectedValueAnimator;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    public static int requestPermission = 170005;
    private ShippingConnectionViewModel shippingConnectionViewModel;
    private GoogleMap googleMap;
    private ShippingMapsFragment mapFragment;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isClicked = Boolean.FALSE;
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentShippingBinding.inflate(inflater, container, false);
        mapFragment = (ShippingMapsFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Log.w("TAG", String.valueOf(mapFragment==null));

        Log.w("TAG","OnCreateView");
        driverViewModel = new ViewModelProvider(getActivity()).get(DriverViewModel.class);
        shippingConnectionViewModel = new ViewModelProvider(this).get(ShippingConnectionViewModel.class);
        // Permissions
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(4000);

        binding.fragShippingProfileBar.shippingProfileHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeButtonListener.OnHomeButtonClicked(view);
            }
        });

        binding.fragShippingProfileBar.shipProfBarButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (shippingConnectionViewModel.isConnected()){
                    Log.w("TAG","Disconnecting");
                    animateConnectedButton(true);
                    connectedButtonListener.OnConnectedButtonClicked(view);
                    shippingConnectionViewModel.connect(Boolean.FALSE,driverViewModel.getDriver().getIdToken());
                    onConnectedValueAnimator.end();

                }else {
                    Log.w("TAG","Connecting");
                    animateConnectedButton(false);
                    connectedButtonListener.OnConnectedButtonClicked(view);
                    shippingConnectionViewModel.connect(Boolean.TRUE,driverViewModel.getDriver().getIdToken());
                    onConnectedValueAnimator.start();
                }
            }
        });


        shippingConnectionViewModel.getStomp().subscribe(new Observer() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull Object o) {
                // We receive a Shipping object
                updateUiOnShippingRequested();
            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        onConnectedValueAnimator = configOnConnectedAnimator();

        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        ShippingMapsFragment mapFragment = (ShippingMapsFragment) getChildFragmentManager().findFragmentById(R.id.fragment_shipping_google);
        Log.w("TAG2", String.valueOf(mapFragment==null));

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(getActivity(), permissions, requestPermission);
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    Log.w("LOCATION","Successful retrieval");
                    if (shippingConnectionViewModel.isConnected()) {
                        //shippingConnectionViewModel.getStomp().sendLocation(location);
                        mapFragment.alignCameraOnNextPosition(location);
                    }
                }
            },null);
        }catch (SecurityException securityException){

        }
        /*fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {


                    }
                });

         */
    }


    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            homeButtonListener = (ShippingFragment.OnHomeButtonListener) context;
            connectedButtonListener = (ShippingFragment.OnConnectedButtonListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.
                    toString()+ "must implement OnHomeButtonListener and OnConnectedButtonListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onConnectedValueAnimator.cancel();
        onConnectedValueAnimator.end();
        binding = null;
    }

    private void rotateCameraOnLocationUpdate(){

    }

    private ValueAnimator configOnConnectedAnimator(){
        int blackColor = ContextCompat.getColor(getActivity(), com.example.tongue_drivers.R.color.black);
        int greyColor = ContextCompat.getColor(getActivity(), R.color.tongueBlue);

        ValueAnimator valueAnimator =
                ValueAnimator.ofObject(new ArgbEvaluator(), blackColor,greyColor);

        valueAnimator.setDuration(1200);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                binding.fragShippingProfileBar.shipProfBarImage.
                        setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });
        return valueAnimator;
    }

    private void updateUiOnShippingRequested(){
        binding.slidingUp.callOnClick();
    }


    private void animateConnectedButton(Boolean isClicked){
        ValueAnimator valueAnimator;
        int blackColor = ContextCompat.getColor(getActivity(), com.example.tongue_drivers.R.color.black);
        int greyColor = ContextCompat.getColor(getActivity(), R.color.tongueGrey);
        if (isClicked)
            valueAnimator  = ValueAnimator.ofObject(new ArgbEvaluator(), blackColor,greyColor);
        else
            valueAnimator  = ValueAnimator.ofObject(new ArgbEvaluator(), greyColor,blackColor);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                binding.fragShippingProfileBar.shipProfBarButton.
                        setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                if (isClicked){
                    binding.fragShippingProfileBar.shipProfBarTextConnected.
                            setText(R.string.Disconnected);
                    binding.fragShippingProfileBar.shipProfBarTextConnected.
                            setTextColor(ContextCompat.getColor(getActivity(),R.color.black));
                }else {
                    binding.fragShippingProfileBar.shipProfBarTextConnected.
                            setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
                    binding.fragShippingProfileBar.shipProfBarTextConnected.
                            setText(R.string.Connected);
                }
            }
        });
        valueAnimator.start();
    }


    public interface OnHomeButtonListener{
        public void OnHomeButtonClicked(View view);
    }

    public interface OnConnectedButtonListener{
        public void  OnConnectedButtonClicked(View view);
    }
}
