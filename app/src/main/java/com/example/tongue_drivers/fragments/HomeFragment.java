package com.example.tongue_drivers.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tongue_drivers.databinding.FragmentHomeBinding;
import com.example.tongue_drivers.databinding.FragmentLoginBinding;
import com.example.tongue_drivers.viewmodels.DriverViewModel;

import org.jetbrains.annotations.NotNull;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class HomeFragment extends Fragment {

    //Fields
    private FragmentHomeBinding binding;
    private OnLogoutButtonListener logoutButtonListener;
    private DriverViewModel driverViewModel;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.fragmentHomePanel.homePanelLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutButtonListener.onLogoutClicked(view);
            }
        });

        driverViewModel = new ViewModelProvider(getActivity()).get(DriverViewModel.class);
        binding.setDriver(driverViewModel);
        Log.w(ContentValues.TAG,"Driver: "+binding.getDriver().getDriver().getName());
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            logoutButtonListener = (HomeFragment.OnLogoutButtonListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()+ "must implement OnLogoutButtonListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface OnLogoutButtonListener{
        public void onLogoutClicked(View view);
    }

}
