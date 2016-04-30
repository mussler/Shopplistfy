package com.patmir.shoplistify.model;

import android.app.Application;
import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.patmir.shoplistify.activity.MainActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Patryk on 22/03/2016.
 */
public final class DataSet extends Application{
    private static DataSet dataSet = null;
    private static ArrayList<ProductList> data;
    private String userId = "0";
    private static String token;
    private static Settings settings;
    private static User user = null;
    Firebase ref = new Firebase("https://shoplistify.firebaseio.com/shoplistify/users");
    Firebase userRef;
    private AuthData mAuthData;
    private Firebase.AuthStateListener mAuthStateListener;

    private DataSet(){
        data = new ArrayList<>();
        settings = new Settings();
      }

    public static synchronized void setToken(String token) {
        DataSet.token = token;
    }
    public static synchronized String getToken(){
        return DataSet.token;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        DataSet.user = user;
    }


    public boolean initFirebase() {
        Log.e("Firebase", "Init");
        ref.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                userId = authData.getUid();
                Log.e("Firebase", "User id: " + userId);
                userRef = ref.child("/" + userId);
                userRef.addListenerForSingleValueEvent(userInitListener);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                userRef = ref.child("/" + 0);
                userRef.addListenerForSingleValueEvent(userInitListener);
            }
        });
        return true;
    }

    ValueEventListener userInitListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                data = new ArrayList<>();
                Log.e("LOG", "FIRE!");
                if (snapshot.child("Settings").exists()){
                    settings = snapshot.child("Settings").getValue(Settings.class);
                } else {
                   userRef.child("Settings").setValue(settings);
                }
                if (snapshot.child("ProductLists").exists()) {
                    for (DataSnapshot snap : snapshot.child("ProductLists").getChildren()
                            ) {
                        Log.e("LOG", "Found Product List");
                        String name = (String) snap.child("name").getValue();
                        int category = ((Long) snap.child("category").getValue()).intValue();
                        ProductList newPl = new ProductList(name, category);
                        newPl.setPushHash(snap.getKey());
                        if(snap.child("Products").exists()) {
                            Log.e("LOG", "Found Products");
                            for (DataSnapshot productSnap : snap.child("Products").getChildren()) {
                                String pName = (String) productSnap.child("name").getValue();
                                int pQuantity = ((Long) productSnap.child("quantity").getValue()).intValue();
                                boolean pCheckbox = (Boolean) productSnap.child("checkbox").getValue();
                                Product p = new Product(pName, pQuantity);
                                p.setCheckBox(pCheckbox);
                                p.setPushHash(productSnap.getKey());
                                newPl.addProduct(p);
                            }
                        }
                        data.add(newPl);
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };

    public void saveCache()  {
        userRef.child("Settings").setValue(settings);
        for (ProductList pl : data
                ) {
            if(pl.getPushHash()== null){
                Firebase plRef = userRef.child("ProductLists").push();
                pl.setPushHash(plRef.getKey());
            }
            Firebase plRef = userRef.child("ProductLists").child(pl.getPushHash());
            plRef.child("name").setValue(pl.getName());
            plRef.child("category").setValue(pl.getCategory());
            for (Product p: pl.getProducts()
                 ) {
                if(p.getPushHash()==null){
                    Firebase pRef = plRef.child("Products").push();
                    p.setPushHash(pRef.getKey());
                }
             Firebase pRef = plRef.child("Products").child(p.getPushHash());
                pRef.child("name").setValue(p.getName());
                pRef.child("quantity").setValue(p.getQuantity());
                pRef.child("checkbox").setValue(p.getCheckBox());
            }

        }
    }

    public static synchronized ArrayList<ProductList> getData(){
        return data;
    }
    public static synchronized void setData(ArrayList<ProductList> _data){
        data = _data;
    }
    public static synchronized Settings getSettings(){return settings;}
    public static synchronized void setSettings(Settings _settings){ settings = _settings;}
    public static synchronized DataSet getInstance() {
        Log.e("DATASET LOG", "Getting DataSet Instance");
        if (dataSet == null) {
            dataSet = new DataSet();
        }

        Log.e("DATASET LOG", "Returning dataset");
        return dataSet;
    }



}
