package com.example.charlesbai321.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import java.util.concurrent.Semaphore;

/**
 * Created by charlesbai321 on 28/12/17.
 */

//https://inducesmile.com/android/android-camera2-api-example-tutorial/
    //(Small rant) ^This website first of all tells you that reading the camera2 example project
    //can be intimidating ( which I whole-heartedly agree with ), so I thought it was going to provide
    //a while thought out, nice documentation and tutorial of how to set up the camera class, but
    //when you scroll down, all he does it tell you to copy paste a bunch of existing code, some
    //of which is personalized to the point where it won't run on other people's devices???
    //If I wanted to copy paste code without knowing how it worked, I would do it from
    // Google's official github examples. This is hardly a "tutorial"!!!
public class CameraDisplay {

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);//NO IDEA WHAT THIS IS LOL

    Activity a;
    Context c;
    TextureView tv;
    private String cameraID;
    protected CameraDevice camDevice;
    protected CameraCaptureSession camSession;
    //next two are for building the preview
    protected CaptureRequest.Builder capBuilder;
    protected CaptureRequest capRequest; //not sure if I need this one
    private Size dimensions;
    public static final int REQUEST_CAMERA_PERMISSION = 4;
    //these are used so that they run on a separate thread and doesn't freeze up the UI
    private Handler bgHandler;
    private HandlerThread bgThread;
    private boolean hasPermissions;

    //this is the callback for the surface (I guess you can sort of treat it as a field to the class)
    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener(){

        //this method is called when a surface texture is available
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            //TODO: implement this method
            openCamera(i, i1);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            //the texture size shouldn't change, but should it change, this function will be
            //called
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false; //<- this is default implementation
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    /**
     * called when cameradevice changes state (don't know what states there are)
     */
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            camDevice = cameraDevice;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            camDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            camDevice.close(); //close on some sort of error
            camDevice = null; //remove the reference
        }
    };

    public CameraDisplay(Context c, Activity a, TextureView tv){
        this.c = c;
        this.a = a;
        hasPermissions = false;
        tv.setSurfaceTextureListener(textureListener);
        //TODO: check if I have permissions
    }

    private void openCamera(int width, int height){
        if(ContextCompat.checkSelfPermission(a, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            //TODO: will have to see how to set the camera as black later
            Toast.makeText(c, "Permission denied!", Toast.LENGTH_SHORT);
        }

        CameraManager cm = (CameraManager) a.getSystemService(Context.CAMERA_SERVICE);
        try{
            
        }
    }



    //start a thread for the camera things because they can take up time and memory and freeze up
    //the UI
    private void startThread(){
        bgThread = new HandlerThread("Camera");
        bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());
    }

    //end the thread that was created by startThread()
    private void endThread(){
        bgThread.quitSafely();
        try{
            bgThread.join();
            bgThread = null; //delete the thread object with the garbage collector
            bgHandler = null;
        }
        catch(InterruptedException e){
            e.printStackTrace(); //this error has to be caught, but this shouldn't happen, so we just
            //tell it to create an error and end the application
        }
    }



}
