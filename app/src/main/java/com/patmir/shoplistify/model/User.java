package com.patmir.shoplistify.model;

import android.net.Uri;

/**
 * Created by Patryk on 30/04/2016.
 */
public class User {
    private String name;
    private String email;
    private Uri pic_url;

    public User(String _name, String _email, Uri _pic_url){
        name = _name;
        email = _email;
        pic_url = _pic_url;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getPic_url() {
        return pic_url;
    }

    public void setPic_url(Uri pic_url) {
        this.pic_url = pic_url;
    }
}
