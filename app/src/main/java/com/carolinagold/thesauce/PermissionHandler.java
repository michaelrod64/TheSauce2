package com.carolinagold.thesauce;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by woodyjean-louis on 12/4/16.
 */

public class PermissionHandler extends Object implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST = 0;
    String[] permissionsArray;
    AppCompatActivity callingActivity;
    PermissionsCallBack callBack;


    public PermissionHandler(AppCompatActivity callingActivity, @NonNull PermissionsCallBack callBack , @NonNull String[] permission) throws NullPointerException {
        this.permissionsArray = permission;
        this.callingActivity= callingActivity;
        this.callBack = callBack;
        askForPermission();

    }

    private void askForPermission() throws NullPointerException {

        if (callingActivity != null) {
            for(int i = 0; i < permissionsArray.length; i++) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    callBack.resultFromRequest(permissionsArray[i], PackageManager.PERMISSION_GRANTED);
                    try {
                        finalize();
                    } catch (Throwable t) {
                        Log.i(Logs.ERROR_IN_TRY, "something went wrong when calling finalize");
                        t.printStackTrace();
                    }
                }
                if (ContextCompat.checkSelfPermission(callingActivity, permissionsArray[i]) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(callingActivity, permissionsArray[i])) {
                        // Show an explanation on why you need an to use this feature of the phone
                        // Show using a dialog
                        Snackbar.make(callingActivity.getWindow().getDecorView().findViewById(android.R.id.content),
                                R.string.permission_rationale,
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.ok, new View.OnClickListener() {
                                    @Override
                                    @TargetApi(Build.VERSION_CODES.M)
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(callingActivity,permissionsArray, PERMISSION_REQUEST);
                                    }
                                });


                    } else {
                        // The user did not recently deny the request, so you can ask for the first time
                        ActivityCompat.requestPermissions(callingActivity,permissionsArray, PERMISSION_REQUEST);
                        break;
                    }
                } else {
                    callBack.resultFromRequest(permissionsArray[i], PackageManager.PERMISSION_GRANTED);
                    try {
                        finalize();
                    } catch (Throwable t) {
                        Log.i(Logs.ERROR_IN_TRY, "something went wrong when calling finalize");
                        t.printStackTrace();
                    }
                }
            }
        } else {
            throw new NullPointerException();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {

                for(int i = 0; i < permissions.length; i++)
                    callBack.resultFromRequest(permissions[i], grantResults[i]);

                try {
                    finalize();
                } catch (Throwable t) {
                    Log.i(Logs.ERROR_IN_TRY, "something went wrong when calling finalize");
                    t.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        callingActivity = null;
        callBack = null;
        permissionsArray = null;
    }

    interface PermissionsCallBack {

        public void resultFromRequest(String permission, Integer granted);
    }
}
