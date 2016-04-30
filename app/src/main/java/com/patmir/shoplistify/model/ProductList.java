package com.patmir.shoplistify.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Patryk on 21/03/2016.
 */
public class ProductList implements Parcelable{
    private ArrayList<Product> products;
    private String name;
    private int category;
    private String pushHash;

    public ProductList(String name) {
        this.name = name;
        products = new ArrayList<Product>();
        category = 0;
    }

    public ProductList(String name, int category){
        this.name = name;
        products = new ArrayList<Product>();
        if(category < 0) {
            this.category = 0;
        } else {
            this.category = category;
        }
    }


    protected ProductList(Parcel in) {
        products = in.createTypedArrayList(Product.CREATOR);
        name = in.readString();
        category = in.readInt();
    }

    public static final Creator<ProductList> CREATOR = new Creator<ProductList>() {
        @Override
        public ProductList createFromParcel(Parcel in) {
            return new ProductList(in);
        }

        @Override
        public ProductList[] newArray(int size) {
            return new ProductList[size];
        }
    };
    public int getSize(){
        return products.size();
    }
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public Product getProduct(int position) {
        return products.get(position);
    }

    public void addProduct(Product p) {
        products.add(p);
    }
    public void addProduct(Product p, int position){
        products.add(position, p);
    }

    public Product removeProduct(int pos) {
        products.remove(pos);
        return null;
    }

    @Override
    public int describeContents() {
        return getSize();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(products);
        dest.writeString(name);
    }
    public void clearAll(){
        products.clear();
    }
    public void addAll(ArrayList<Product> pr){
        for (Product p : pr
                ) {
            this.products.add(p);
        }
    }
    public ArrayList<Product> getProducts(){
        return products;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category){
        this.category = category;
    }

    public String getPushHash() {
        return pushHash;
    }

    public void setPushHash(String pushHash) {
        this.pushHash = pushHash;
    }
}

