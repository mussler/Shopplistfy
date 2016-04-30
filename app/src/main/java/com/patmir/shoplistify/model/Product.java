package com.patmir.shoplistify.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Patryk on 21/03/2016.
 */
public class Product implements Parcelable {
    private boolean checkBox;
    private String name;
    private int quantity;
    private String pushHash;

    public Product(String _name, int _quantity){
        checkBox = false;
        name = _name;
        quantity = _quantity;
    }
    public String getName(){return  name;}
    public void setName(String name) { this.name = name;}
    public void setQuantity(int quantity) {this.quantity = quantity;}
    public int getQuantity() {return quantity;}
    public boolean getCheckBox(){return  checkBox;}
    public void setCheckBox(boolean flag){
        this.checkBox = flag;
    }
    private Product(Parcel in) {
        checkBox = in.readByte() != 0;
        name = in.readString();
        quantity = in.readInt();
    }
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return quantity + " " + name + ": " + checkBox;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeByte((byte) (checkBox ? 1 : 0));
        out.writeInt(quantity);
    }

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getPushHash() {
        return pushHash;
    }

    public void setPushHash(String pushHash) {
        this.pushHash = pushHash;
    }
}

