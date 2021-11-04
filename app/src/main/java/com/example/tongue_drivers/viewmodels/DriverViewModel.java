package com.example.tongue_drivers.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.tongue_drivers.models.Driver;

public class DriverViewModel extends ViewModel {

    private Boolean authenticated=Boolean.FALSE;

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    private Driver driver = new Driver();

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}
