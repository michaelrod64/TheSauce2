package com.carolinagold.thesauce;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;
import static android.Manifest.permission_group.STORAGE;

public class PostCreator extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener, PermissionHandler.PermissionsCallBack {


    private String uId;
    private String displayName;
    private String caption;

    private ImageView photoImageView;
    private EditText captionEditText;
    private TextView locationTextView;
    private Bitmap bitmap;
    private Uri pictureUri;

    private static final int RESULT_FROM_GALLERY = 1;
    private static final int RESULT_FROM_CAMERA = 2;

    public boolean photoChosen = false;

    private GoogleApiClient googleAPIClient;
    private Location currentLocation;
    private LocationRequest locationRequest;

    private double latitude;
    private double longitude;

    private File output=null;

    private FirebaseDatabase dbRef = FirebaseDatabase.getInstance();



    private String decodedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creator);

        uId = this.getIntent().getStringExtra("uId");
        displayName = this.getIntent().getStringExtra("displayName");

        photoImageView = (ImageView) findViewById(R.id.edit_image);
        captionEditText = (EditText) findViewById(R.id.caption_text);
        locationTextView = (TextView)findViewById(R.id.location_text);

        googleAPIClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).addOnConnectionFailedListener(this).
                addApi(LocationServices.API).build();


        //String array[] = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA};
        //Log.i("TEST", "" + array);

       //new PermissionHandler(PostCreator.this, PostCreator.this, array);

//        ActivityCompat.requestPermissions(PostCreator.this,
//                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
//                1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(PostCreator.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void onStart() {
        googleAPIClient.connect();
        super.onStart();
    }
    public void onStop() {
        Log.i("test", "is stopped");

        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleAPIClient,this);
        googleAPIClient.disconnect();
        super.onStop();
    }
    public void onConnected(Bundle connectionHint) {
        Log.i("test", "is connected");
        String errorMessage = "";
        LocationSettingsRequest.Builder settingsBuilder;
        PendingResult<LocationSettingsResult> pendingResult;
        LocationSettingsResult settingsResult;

        locationRequest = new LocationRequest();

        locationRequest.setInterval(getResources().getInteger(
                R.integer.time_between_location_updates_ms));
        locationRequest.setFastestInterval(getResources().getInteger(
                R.integer.time_between_location_updates_ms) / 2);
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        settingsBuilder = new LocationSettingsRequest.Builder();
        settingsBuilder.addLocationRequest(locationRequest);
        pendingResult = LocationServices.SettingsApi.checkLocationSettings(
                googleAPIClient,settingsBuilder.build());

        startLocationUpdates();
    }
    private void startLocationUpdates() {
        Log.i("TEST", "starting location updates");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleAPIClient,locationRequest,this);
        } catch (SecurityException e) {
            Toast.makeText(this,"Cannot get location updates, please enable",Toast.LENGTH_LONG).show();
            finish();
        }

    }
    public void onLocationChanged(Location newLocation) {

        Log.i("TEST", "entered onLocationChanged");

            currentLocation = newLocation;

            TextView locationText;
            String currentText;
            Time timeOfChange;

            locationText = (TextView) findViewById(R.id.location_text);

            //currentText = locationText.getText().toString();


            timeOfChange = new Time();
            timeOfChange.set(currentLocation.getTime());
            //currentText += timeOfChange.format("%A %D %T") + "   ";
            //currentText += "\nProvider " + currentLocation.getProvider() + " found location\n";

            latitude = newLocation.getLatitude();
            longitude = newLocation.getLongitude();

            decodedAddress = getCompleteAddressString(latitude, longitude);

            currentText = decodedAddress;


            locationText.setText(currentText);



    }

    public void onConnectionFailed(ConnectionResult result) {
        Log.i("test", "connection failed");
    }
    //-----------------------------------------------------------------------------
    public void onConnectionSuspended(int cause) {
        Log.i("test", "connection suspended");

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                android.location.Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("Current loction address", "" + strReturnedAddress.toString());
            } else {
                Log.w("Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Current loction address", "Canont get Address!");
        }
        return strAdd;
    }

    public void myClickHandler(View view) {

        switch (view.getId()) {
            case R.id.edit_image:

                new AlertDialog.Builder(this)
                        .setTitle("Camera or Gallery")
                        .setMessage("Would you like to take a photo from the Gallery or the Camera?")
                        .setPositiveButton(getResources().getString(R.string.choose_gallery), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent galleryIntent;
                               // if(haveREStoragePermission && haveEXStoragePermission) {
                                    galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galleryIntent, RESULT_FROM_GALLERY);
                                //}
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.choose_camera), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {






                                //if (haveCameraPermission) {
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivityForResult(takePictureIntent, RESULT_FROM_CAMERA);
                                    }
                               // }
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            case R.id.post_button:

                Button postButton = (Button)findViewById(R.id.post_button);
                postButton.setClickable(false);
                if(!photoChosen) {
                    Toast.makeText(this,"Pick a photo!",Toast.LENGTH_LONG).show();
                    postButton.setClickable(true);
                }
                else {


                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                    String strDate = "Current Date : " + mdformat.format(calendar.getTime());

                    caption = (String)((EditText)findViewById(R.id.caption_text)).getText().toString();

                    Post post = new Post(uId,displayName, bitmap, strDate, decodedAddress, caption);
                    post.pushToCloud(this);


                    //startActivity(new Intent(PostCreator.this, MainActivity.class));



                }
        }
    }
    public void donePosting() {
        setResult(RESULT_OK);
        finish();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            photoImageView.setImageBitmap(bitmap);




            photoImageView.setImageBitmap(bitmap);
            photoImageView.setImageURI(pictureUri);
            photoChosen = true;
        }
        if (requestCode == RESULT_FROM_GALLERY && resultCode == RESULT_OK && null != data) {
           Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            ImageView imageView = (ImageView) findViewById(R.id.edit_image);
            bitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(bitmap);
            photoChosen = true;
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    boolean haveCameraPermission = false;
    boolean haveREStoragePermission = false;
    boolean haveEXStoragePermission = false;


    @Override
    public void resultFromRequest(String permission, Integer granted) {
        switch (permission) {
            case CAMERA:
                if(granted == PackageManager.PERMISSION_GRANTED)
                    haveCameraPermission = true;

                break;
            case READ_EXTERNAL_STORAGE:
                if (granted == PackageManager.PERMISSION_GRANTED)
                    haveREStoragePermission = true;
                break;
            case WRITE_EXTERNAL_STORAGE:
                if (granted == PackageManager.PERMISSION_GRANTED)
                    haveEXStoragePermission = true;
        }
    }
}
