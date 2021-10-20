package com.example.tongue_drivers.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.tongue_drivers.databinding.FragmentHomeBinding;
import com.example.tongue_drivers.databinding.FragmentMainBinding;

public class MainFragment extends Fragment{

    //Fields
    private FragmentMainBinding binding;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){


        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }
}
