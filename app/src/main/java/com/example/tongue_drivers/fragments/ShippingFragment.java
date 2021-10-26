package com.example.tongue_drivers.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tongue_drivers.R;
import com.example.tongue_drivers.databinding.FragmentShippingBinding;
import com.example.tongue_drivers.viewmodels.DriverViewModel;
import static android.content.ContentValues.TAG;
import org.jetbrains.annotations.NotNull;


public class ShippingFragment extends Fragment {

    //Fields
    private FragmentShippingBinding binding;
    private OnHomeButtonListener homeButtonListener;
    private OnConnectedButtonListener connectedButtonListener;
    private DriverViewModel driverViewModel;
    private Boolean isClicked;
    public ValueAnimator onConnectedValueAnimator;

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isClicked = Boolean.FALSE;
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        binding = FragmentShippingBinding.inflate(inflater, container, false);
        driverViewModel = new ViewModelProvider(getActivity()).get(DriverViewModel.class);

        binding.fragShippingProfileBar.shippingProfileHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeButtonListener.OnHomeButtonClicked(view);
            }
        });

        binding.fragShippingProfileBar.shipProfBarButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (isClicked){
                    animateConnectedButton(true);
                    connectedButtonListener.OnConnectedButtonClicked(view);
                    isClicked = Boolean.FALSE;
                    onConnectedValueAnimator.end();

                }else {
                    animateConnectedButton(false);
                    connectedButtonListener.OnConnectedButtonClicked(view);
                    isClicked = Boolean.TRUE;
                    onConnectedValueAnimator.start();
                }
            }
        });

        onConnectedValueAnimator = configOnConnectedAnimator();

        View root = binding.getRoot();
        return root;
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
