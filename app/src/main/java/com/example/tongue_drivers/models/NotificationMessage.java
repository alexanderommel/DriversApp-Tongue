package com.example.tongue_drivers.models;

public class NotificationMessage {

    private String message;
    private String authorization_token;
    private Float longitude;
    private Float latitude;
    private Long shipping_id;
    private String jsonSummary="JSON";
    private String customerAddress;
    private String sender;

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getShipping_id() {
        return shipping_id;
    }

    public void setShipping_id(Long shipping_id) {
        this.shipping_id = shipping_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorization_token() {
        return authorization_token;
    }

    public void setAuthorization_token(String authorization_token) {
        this.authorization_token = authorization_token;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public String getJsonSummary() {
        return jsonSummary;
    }

    public void setJsonSummary(String jsonSummary) {
        this.jsonSummary = jsonSummary;
    }

    @Override
    public String toString() {
        return "NotificationMessage -> {" +
                "message='" + message + '\'' +
                ", authorization_token='" + authorization_token + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", shipping_id=" + shipping_id +
                '}';
    }

}
