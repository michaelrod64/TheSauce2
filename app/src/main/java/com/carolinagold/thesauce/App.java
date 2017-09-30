package com.carolinagold.thesauce;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by woodyjean-louis on 12/4/16.
 */

public class App extends Application {

    FirebaseDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        //database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("message");

        //FireBase.setAndroidContext(this);
    }
}
