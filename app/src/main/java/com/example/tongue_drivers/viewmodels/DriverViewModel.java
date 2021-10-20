package com.example.tongue_drivers.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.tongue_drivers.models.Driver;

public class DriverViewModel extends ViewModel {

    private Driver driver = new Driver();

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}
