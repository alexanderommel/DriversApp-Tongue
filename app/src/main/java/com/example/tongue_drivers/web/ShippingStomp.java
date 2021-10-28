package com.example.tongue_drivers.web;

import android.location.Location;
import android.util.Log;

import com.example.tongue_drivers.config.TongueNetworkSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.CompletableTransformer;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class ShippingStomp extends Observable {

    //Fields
    private Gson gson;
    private static ShippingStomp instance;
    private CompositeDisposable compositeDisposable;
    private StompClient stompClient;
    private final String domain = TongueNetworkSettings.domain;
    private final String port = TongueNetworkSettings.port;
    private final String connectionEndPoint="/connect";
    private final String driversSubscriptionPath="/user/queue/drivers";
    private final String shareLocationEndPoint="/app/drivers/share_location";
    private List<Observer> observers;

    private ShippingStomp() {
        gson = new GsonBuilder().create();
        observers = new ArrayList<>();
    }

    @Override
    protected void subscribeActual(Observer observer) {
        observers.add(observer);
    }

    public static ShippingStomp getInstance(){
        if (instance==null){
            instance = new ShippingStomp();
        }
        return instance;
    }

    private void resetSubscriptions(){
        if (compositeDisposable!=null)
            compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
    }

    public void connect(String idToken){
        //Setting
        String uri = "ws://"+domain+":"+port+connectionEndPoint+"/websocket"+"?idToken="+idToken;
        Log.w("STOMP","URI:"+uri);
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,uri
        );
        resetSubscriptions();
        //Headers
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("LOGIN","SESSION="+idToken));
        //Setting
        stompClient.withClientHeartbeat(10000).withServerHeartbeat(10000);
        resetSubscriptions();
        Disposable disposable = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()){
                        case OPENED:
                            Log.w("TAG","Stomp Connection Opened");
                            break;
                        case ERROR:
                            Log.w("TAG","Stomp Connection Error");
                            break;
                        case CLOSED:
                            Log.w("TAG","Stomp Connection Closed");
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Log.w("TAG","Stomp Connection failed server HeartBeat");
                    }
                });
        compositeDisposable.add(disposable);

        //Subscriptions
        Disposable disposable1 = stompClient.topic(driversSubscriptionPath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.w("TAG","Received message from "+driversSubscriptionPath);
                    for (Observer ob:observers
                         ) {
                        ob.onNext(null);
                    }
                    //Send data to observers
                }, throwable -> {
                    Log.w("TAG","Message from "+driversSubscriptionPath+" couldn't be processed");
                    //Send error to observers
                });
        compositeDisposable.add(disposable1);

        stompClient.connect(headers);
    }

    private CompletableTransformer applySchedulers(){
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void sendLocation(Location location,String idToken){
        if (!stompClient.isConnected()) return;
        compositeDisposable.add(stompClient.send(shareLocationEndPoint,"Echo")
                .compose(applySchedulers())
                .subscribe(() -> {
                    Log.w("STOMP","Location Shared successfully");
                }, throwable -> {
                    Log.w("STOMP","Error sharing location");
                }));
    }

    public void disconnect(){
        stompClient.disconnect();
    }
}
