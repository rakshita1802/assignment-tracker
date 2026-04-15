package com.tracker.model;

public class User {
    private int    userId;
    private String name;
    private String email;
    private String password;
    private Role   role;
    private String phoneNumber;

    public User() {}

    public User(int userId, String name, String email,
                String password, Role role, String phoneNumber) {
        this.userId      = userId;
        this.name        = name;
        this.email       = email;
        this.password    = password;
        this.role        = role;
        this.phoneNumber = phoneNumber;
    }

    public int    getUserId()                  { return userId; }
    public void   setUserId(int id)            { this.userId = id; }
    public String getName()                    { return name; }
    public void   setName(String n)            { this.name = n; }
    public String getEmail()                   { return email; }
    public void   setEmail(String e)           { this.email = e; }
    public String getPassword()                { return password; }
    public void   setPassword(String p)        { this.password = p; }
    public Role   getRole()                    { return role; }
    public void   setRole(Role r)              { this.role = r; }
    public String getPhoneNumber()             { return phoneNumber; }
    public void   setPhoneNumber(String phone) { this.phoneNumber = phone; }

    @Override
    public String toString() { return name + " [" + role + "]"; }
}