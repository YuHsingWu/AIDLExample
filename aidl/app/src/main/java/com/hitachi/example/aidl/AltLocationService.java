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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

/**
 * Created by mac on 2017/11/30.
 */

public class AltLocationService extends Service implements BeaconConsumer, SensorEventListener {

    private final String TAG = "AltLocationService";
    private RemoteCallbackList<ICompassCallback> mCallbacks;
    private SensorManager mSensorManager;
    private BeaconManager beaconManager;

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
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("LocationService", "onDestroy");
        beaconManager.unbind(this);
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

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.i(TAG, "onBeaconsDiscovered: ");
                JSONArray jsonArray = new JSONArray();
                for (Beacon beacon : collection) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("mode", "alt");
                        jsonObject.put("uuid", beacon.getId1());
                        jsonObject.put("major", beacon.getId2());
                        jsonObject.put("minor", beacon.getId3());
                        jsonObject.put("rssi", beacon.getRssi());
                        jsonObject.put("power", beacon.getTxPower());
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
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new org.altbeacon.beacon.Region("myMonitoringUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new org.altbeacon.beacon.Region("myRangingUniqueId", null, null, null));
            Log.i(TAG, "start success");
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.i(TAG, "RemoteException");
        }
    }
}
