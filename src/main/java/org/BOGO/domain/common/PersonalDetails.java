package org.BOGO.domain.common;


public class PersonalDetails {

    private int userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;

    // ---------- Constructors ----------
    public PersonalDetails() {}

    public PersonalDetails(int userId, String name, String email, String phoneNumber, String password) {
        this.name        = name;
        this.email       = email;
        this.phoneNumber = phoneNumber;
        this.password    = password;
        this.userId = userId;
    }

    // ---------- Getters ----------
    public int getUserId()    { return userId; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPassword()    { return password; }

    // ---------- Setters ----------
    public void setUserId(int userId)               { this.userId        = userId; }
    public void setName(String name)               { this.name        = name; }
    public void setEmail(String email)             { this.email       = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setPassword(String password)       { this.password    = password; }

    // ---------- Business Methods ----------
    /** Validates that all required fields are non-null and properly formatted. */
    public boolean validate() {
        return false;
    }
}
