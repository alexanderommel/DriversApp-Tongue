package com.example.tongue_drivers.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tongue_drivers.databinding.FragmentShippingBinding;

import org.jetbrains.annotations.NotNull;

public class ShippingFragment extends Fragment {

    //Fields
    private FragmentShippingBinding binding;
    private OnHomeButtonListener homeButtonListener;

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        binding = FragmentShippingBinding.inflate(inflater, container, false);
        binding.fragShippingProfileBar.shippingProfileHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeButtonListener.OnHomeButtonClicked(view);
            }
        });
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            homeButtonListener = (ShippingFragment.OnHomeButtonListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()+ "must implement OnHomeButtonListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public interface OnHomeButtonListener{
        public void OnHomeButtonClicked(View view);
    }
}
