package com.example.tongue_drivers.viewmodels;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.tongue_drivers.web.ShippingStomp;

public class ShippingConnectionViewModel extends ViewModel {

    private Boolean connected=Boolean.FALSE;
    private ShippingStomp stomp=ShippingStomp.getInstance();

    public Boolean isConnected() {
        return connected;
    }

    public ShippingStomp getStomp() {
        return stomp;
    }

    public void connect(Boolean connected,String sessionId) {
        Log.w("STOMP","IdToken provided to STOMP is: "+sessionId);
        if (connected){
            stomp.connect(sessionId);
        }else {
            stomp.disconnect();
        }

        this.connected = connected;
    }
}
