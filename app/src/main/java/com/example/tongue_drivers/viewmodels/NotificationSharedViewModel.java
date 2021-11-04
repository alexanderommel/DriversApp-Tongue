package com.example.tongue_drivers.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tongue_drivers.models.NotificationMessage;

public class NotificationSharedViewModel extends ViewModel {

    private MutableLiveData<NotificationMessage> notification = new MutableLiveData<>();

    public void setNotification(NotificationMessage notificationMessage){
        notification.setValue(notificationMessage);
    }

    public MutableLiveData<NotificationMessage> getNotification() {
        return notification;
    }
}
