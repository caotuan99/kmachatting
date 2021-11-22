package com.example.chatkma.models;

import java.io.Serializable;

public class ModelUsers  implements Serializable {
    // dung ten da lua o firebase database
    String Hoten, email, search, phone, image, cover, MaSv, uid;

    public ModelUsers() {
    }

    public ModelUsers(String Hoten, String email, String search, String phone, String image, String cover, String maSv, String uid) {
        this.Hoten = Hoten;
        this.email = email;
        this.search = search;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.MaSv = maSv;
        this.uid = uid;
    }

    public String getHoten() {
        return Hoten;
    }

    public void setHoten(String hoten) {
        this.Hoten = hoten;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getMaSv() {
        return MaSv;
    }

    public void setMaSv(String maSv) {
        this.MaSv = maSv;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}