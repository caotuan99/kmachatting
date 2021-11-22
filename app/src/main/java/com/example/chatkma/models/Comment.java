package com.example.chatkma.models;

public class Comment {
    private String time,noidung,image,uid;

    public Comment() {
    }

    public Comment(String time, String noidung, String uid) {
        this.time = time;
        this.noidung = noidung;
        this.uid = uid;
    }

    public Comment(String time, String noidung, String image, String uid) {
        this.time = time;
        this.noidung = noidung;
        this.image = image;
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNoidung() {
        return noidung;
    }

    public void setNoidung(String noidung) {
        this.noidung = noidung;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
