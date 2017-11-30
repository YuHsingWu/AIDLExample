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

/**
 * Created by mac on 2017/11/30.
 */

public class CompassService extends Service implements SensorEventListener {

    // ICallback列表
    private RemoteCallbackList<ICompassCallback> mCallbacks;
    private SensorManager mSensorManager;
    private float mOrientation = 0f;


    public CompassService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        // 记得要关闭广播
        mCallbacks.finishBroadcast();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] accelerometerValues = new float[3];
        float[] magneticFieldValues = new float[3];
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticFieldValues = sensorEvent.values;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerValues = sensorEvent.values;

        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        mOrientation = values[0];
        // 要经过一次数据格式的转换，转换为度
//        values[0] = (float) Math.toDegrees(values[0]);
        Log.i("CompassService", "Orientation = " + values[0]);
        sendResponse();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
