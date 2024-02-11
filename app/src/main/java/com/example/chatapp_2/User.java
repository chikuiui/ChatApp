package com.example.chatapp_2;

public class User {
    private String name;
    private String email;
    private String profilePhoto;


    public User(){}

    public User(String name,String email,String profilePhoto){
        this.name = name;
        this.email = email;
        this.profilePhoto = profilePhoto;
    }

    // Getters and Setters
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getProfilePhoto() {return profilePhoto;}
    public void setProfilePhoto(String profilePhoto) {this.profilePhoto = profilePhoto;}
}
