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
import com.aprilbrother.aprilbrothersdk.utils.AprilL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by mac on 2017/11/30.
 */

public class AprilLocationService extends Service implements SensorEventListener {

    private final String TAG = "AprilLocationService";
    private RemoteCallbackList<ICompassCallback> mCallbacks;
    private SensorManager mSensorManager;
    private BeaconManager mBeaconManager;

    private float mOrientation = 0f;
    private static final Region ALL_BEACONS_REGION = new Region("Beacon_SLAM", null, null, null);

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
        AprilL.enableDebugLogging(true);
        // Initial SersorManager to listen device orientation
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensors.size()>0) {
            mSensorManager.registerListener(this, (Sensor) sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Initial April BeaconManager to listen Beacon information
        mBeaconManager = new BeaconManager(getApplicationContext());
        mBeaconManager.setForegroundScanPeriod(1000,1000);
        mBeaconManager.setRangingListener(BeaconRangingListener);
        mBeaconManager.setMonitoringListener(BeaconMonitorListener);
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    Log.i(TAG, "connectToService");
                    mBeaconManager.startRanging(ALL_BEACONS_REGION);
                    mBeaconManager.startMonitoring(ALL_BEACONS_REGION);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("LocationService", "onDestroy");
        mBeaconManager.disconnect();
        mSensorManager.unregisterListener(this);
    }

    private void sendResponse(String json) {
        int len = mCallbacks.beginBroadcast();
        for (int i = 0; i < len; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onResult(json);
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private BeaconManager.RangingListener BeaconRangingListener = new BeaconManager.RangingListener() {
        @Override
        public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
            Log.i(TAG, "onBeaconsDiscovered: ");
            JSONArray jsonArray = new JSONArray();
            for (Beacon beacon : beacons) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("mode", "april");
                    jsonObject.put("uuid", beacon.getProximityUUID());
                    jsonObject.put("major", beacon.getMajor());
                    jsonObject.put("minor", beacon.getMinor());
                    jsonObject.put("rssi", beacon.getRssi());
                    jsonObject.put("power", beacon.getMeasuredPower());
                    jsonObject.put("distance", beacon.getDistance());
                    jsonObject.put("orientation", mOrientation);
                    jsonArray.put(jsonObject);
                    Log.i(TAG, "jsonObject = " + jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            sendResponse(jsonArray.toString());
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
