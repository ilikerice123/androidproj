package com.example.charlesbai321.myapplication.Util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by charlesbai321 on 18/01/18.
 */

public class GPSService extends Service {
    public static final int USE_GPS = 3;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
