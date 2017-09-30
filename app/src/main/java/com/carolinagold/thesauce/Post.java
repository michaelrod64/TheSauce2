package com.carolinagold.thesauce;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by woodyjean-louis on 12/9/16.
 */

public class Post extends Object implements Serializable {

    private final String firebase_storage_bucket_name = "gs://thesauce2-56d66.appspot.com";

    private String userProfilePicturePath;
    public String uId;
    private String userName;
    private String imagePath;
    private String date;
    private String location;
    private String caption;
    private String name;
    private String postKey;
    private Bitmap bitmap;

    public Post() {

    }

    public Post(@NonNull String uId, String userName, @NonNull Bitmap bitmap, String date, String location, String caption) {
        this.uId = uId;
        this.userName = userName;
        this.bitmap = bitmap;
        this.date = date;
        this.location = location;
        this.caption = caption;
    }

    public String getUserProfilePicturePath() {return userProfilePicturePath;}

    public String getuId() {return uId;}

    public String getUserName() {return userName;}

    public String getImagePath() {return imagePath;}

    public String getDate() {return date;}

    public String getLocation() {return location;}

    public String getCaption() {return caption;}

   public String getName() {return name;}

    public String getPostKey() {return postKey;}


    public boolean pushToCloud(final Context context) {


        // Will store the profile image and post image in storage
        // Create a storage reference from our app
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(firebase_storage_bucket_name);
        Calendar calendar = Calendar.getInstance();
        storageRef = storageRef.child("Post").child(uId).child(Long.toString(calendar.getTimeInMillis()));

        try {
            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imagePath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    name = taskSnapshot.getMetadata().getName();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                    // Creates a post in the real time database with paths to the image file in storage

                    final DatabaseReference myPostRef = database.getReference("Post");
                    final String key = myPostRef.push().getKey();
                    postKey = key;
                    myPostRef.child(uId).child(key).child("uId").setValue(uId);
                    myPostRef.child(uId).child(key).child("location").setValue(location);
                    myPostRef.child(uId).child(key).child("caption").setValue(caption);
                    myPostRef.child(uId).child(key).child("date").setValue(date);
                    myPostRef.child(uId).child(key).child("name").setValue(name);
                    myPostRef.child(uId).child(key).child("postKey").setValue(postKey);
                    myPostRef.child(uId).child(key).child("imagePath").setValue(downloadUrl.toString());

                    DatabaseReference myUserProfileInfoRef = database.getReference("userProfileInfo").child(uId);

                    myUserProfileInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                                Log.i(Logs.POINT_OF_INTEREST, "In post!!!");

                                String str = dataSnapshot.getKey();

                                switch (str) {
                                    case "userProfilePicturePath":
                                        String profileImagePath = dataSnapshot.getValue(String.class);
                                        myPostRef.child(uId).child(key).child("userProfilePicturePath").setValue(profileImagePath);
                                        break;
                                    case "userName":
                                        String uName = dataSnapshot.getValue(String.class);
                                        myPostRef.child(uId).child(key).child("userName").setValue(uName);
                                }

                            }


                            PostCreator postCreator = (PostCreator) context;
                            postCreator.donePosting();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.i(Logs.POINT_OF_INTEREST, "Error in Post");
                        }


                    });



                }
            });
        } catch (Exception e) {
            Log.i(Logs.ERROR_IN_TRY, "Exception happened");
            e.printStackTrace();
            Toast.makeText(context, "Error uploading photo", Toast.LENGTH_SHORT);
            return false;
        }




        return true;
    }

    public boolean deleteFromCloud(final Context context) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl(firebase_storage_bucket_name);

        // Create a reference to the file to delete
        StorageReference desertRef = storageRef.child("Post").child(uId).child(name);

        // Delete the file
        desertRef.delete().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
             //deletion success
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                final DatabaseReference myPostRef = database.getReference("Post").child(uId);
                myPostRef.child(postKey).removeValue();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.i(Logs.POINT_OF_INTEREST, "Error deleting");
                Toast.makeText(context, "Error Deleting File", Toast.LENGTH_LONG);
                exception.printStackTrace();
            }
        });
        return  true;
    }
}
