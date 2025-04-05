package com.website.dto;

public class AddressDTO {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private int userId;

    // Getters and Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
