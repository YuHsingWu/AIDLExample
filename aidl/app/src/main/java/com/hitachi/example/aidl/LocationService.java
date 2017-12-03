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

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by mac on 2017/11/30.
 */

public class LocationService extends Service implements SensorEventListener {

    private final String TAG = "LocationService";
    private RemoteCallbackList<ICompassCallback> mCallbacks;
    private SensorManager mSensorManager;
    private BeaconManager mBeaconManager;
    JSONObject jsonObject = new JSONObject();

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
        Log.i("LocationService", "onCreate");

        // Initial SersorManager to listen device orientation
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensors.size()>0) {
            mSensorManager.registerListener(this, (Sensor) sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Initial April BeaconManager to listen Beacon information
        mBeaconManager = new BeaconManager(getApplicationContext());
        mBeaconManager.setRangingListener(BeaconRangingListener);
        mBeaconManager.setMonitoringListener(BeaconMonitorListener);


        try {
            jsonObject.put("UUID", "saldjlajdoahjfoisdoif");
            jsonObject.put("Major", "0");
            jsonObject.put("Minor", "0");
            jsonObject.put("distance", 1.1f);
            jsonObject.put("x", 2.2f);
            jsonObject.put("y", 3.3f);
            jsonObject.put("orientation", 0.0f);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("LocationService", "onDestroy");
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
        Log.i("LocationService", "Orientation = " + mOrientation);
        sendResponse();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private BeaconManager.RangingListener BeaconRangingListener = new BeaconManager.RangingListener() {
        @Override
        public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
            Log.i(TAG, "onBeaconsDiscovered: ");
            for (Beacon beacon : beacons) {
                if (beacon.getRssi() > 0) {
                    Log.i(TAG, "UUID = " + beacon.getProximityUUID());
                    Log.i(TAG, "Major = " + beacon.getMajor());
                    Log.i(TAG, "Minor = " + beacon.getMinor());
                    Log.i(TAG, "rssi = " + beacon.getRssi());
                    Log.i(TAG, "mac = " + beacon.getMacAddress());
                    Log.i(TAG, "distance = " + beacon.getDistance());
                }
            }
        }
    };

    private BeaconManager.MonitoringListener BeaconMonitorListener = new BeaconManager.MonitoringListener() {
        @Override
        public void onEnteredRegion(Region region, List<Beacon> list) {

        }

        @Override
        public void onExitedRegion(Region region) {

        }
    };
}
