package com.example.tongue_drivers.fragments;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tongue_drivers.R;
import com.example.tongue_drivers.databinding.FragmentLoginBinding;
import com.example.tongue_drivers.databinding.FragmentShippingBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.jetbrains.annotations.NotNull;

public class LoginFragment extends Fragment{

    //Fields
    private FragmentLoginBinding binding;
    private OnLoginButtonListener loginButtonListener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){


        binding = FragmentLoginBinding.inflate(inflater, container, false);

        binding.fragLoginGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButtonListener.onButtonClicked(view);
            }
        });

        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            loginButtonListener = (OnLoginButtonListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()+ "must implement OnLoginButtonListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface OnLoginButtonListener{
        public void onButtonClicked(View view);
    }

}
