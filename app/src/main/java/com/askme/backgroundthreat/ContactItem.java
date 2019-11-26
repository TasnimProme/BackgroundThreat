package com.askme.backgroundthreat;

import java.util.ArrayList;

public class ContactItem {

    private String displayName;
    private String photoUrl;
    private ArrayList<PhoneContact> arrayListPhone = new ArrayList<>();
    private ArrayList<EmailContact> arrayListEmail = new ArrayList<>();
    private ArrayList<PostalAddress> arrayListAddress = new ArrayList<>();


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public ArrayList<PhoneContact> getArrayListPhone() {
        return arrayListPhone;
    }

    public void setArrayListPhone(ArrayList<PhoneContact> arrayListPhone) {
        this.arrayListPhone = arrayListPhone;
    }

    public ArrayList<EmailContact> getArrayListEmail() {
        return arrayListEmail;
    }

    public void setArrayListEmail(ArrayList<EmailContact> arrayListEmail) {
        this.arrayListEmail = arrayListEmail;
    }

    public ArrayList<PostalAddress> getArrayListAddress() {
        return arrayListAddress;
    }

    public void setArrayListAddress(ArrayList<PostalAddress> arrayListAddress) {
        this.arrayListAddress = arrayListAddress;
    }
}




