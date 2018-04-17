package com.example.charlesbai321.myapplication.Util;

/**
 * Created by charlesbai321 on 17/04/18.
 */
//http://android-coding.blogspot.ca/2012/07/dialogfragment-with-interface-to-pass.html

//only goal of this interface is so that we can transfer data from the static dialog fragment class
//to our activity class
public interface DataTransfer {

    void updateData(String data);

    void updateList();
}
