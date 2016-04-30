package com.patmir.shoplistify.model;

/**
 * Created by Patryk on 13/04/2016.
 */
public class Settings {
    boolean sync;
    int textSize;

    public Settings(){
        sync = true;
        textSize = 0;
    }

    public boolean getSync(){
        return sync;
    }

    public int getTextSize(){
        return textSize;
    }

    public void setSync(boolean f){
        sync = f;
    }

    public void setTextSize(int size){
        textSize = (int) Math.floor((size%3));
    }
}
