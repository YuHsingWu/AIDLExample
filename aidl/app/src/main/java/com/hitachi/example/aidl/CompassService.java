package com.hitachi.example.aidl;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

/**
 * Created by mac on 2017/11/30.
 */

public class CompassService extends Service implements SensorEventListener {

    private RemoteCallbackList<ICompassCallback> mCallbacks;
    private SensorManager mSensorManager;

    private float mOrientation = 0f;

    @Override
    public IBinder onBind(Intent intent) {
        mCallbacks = new RemoteCallbackList<ICompassCallback>();
        return mBinder;
    }

    private final ICompassService.Stub mBinder = new ICompassService.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public float getCompass() throws RemoteException {
            return mOrientation;
        }

        @Override
        public void registerCallback(ICompassCallback cb) throws RemoteException {
            if(cb != null) {
                mCallbacks.register(cb);
            }
        }

        @Override
        public void unregisterCallback(ICompassCallback cb) throws RemoteException {
            if(cb != null) {
                mCallbacks.unregister(cb);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("CompassService", "onCreate");
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensors.size()>0) {
            mSensorManager.registerListener(this, (Sensor) sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("CompassService", "onDestroy");
        mSensorManager.unregisterListener(this);
    }

    private void sendResponse() {
        int len = mCallbacks.beginBroadcast();
        for (int i = 0; i < len; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onResult(mOrientation);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mCallbacks.finishBroadcast();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // angle between the magnetic north direction
        // 0=North, 90=East, 180=South, 270=West
        mOrientation = event.values[0];
        Log.i("CompassService", "Orientation = " + mOrientation);
        sendResponse();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
