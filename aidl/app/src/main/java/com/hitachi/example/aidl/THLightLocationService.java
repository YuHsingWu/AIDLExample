package com.hitachi.example.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.THLight.USBeacon.App.Lib.BatteryPowerData;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;

public class THLightLocationService extends Service {
    private String TAG = "THLightLocationService";
    private RemoteCallbackList<ICompassCallback> mCallbacks;
    private float mOrientation = 0f;
    private iBeaconScanManager mBeaconManager = null;

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

        mBeaconManager = new iBeaconScanManager(this, new iBeaconScanManager.OniBeaconScan() {
            @Override
            public void onScaned(iBeaconData iBeaconData) {
                Log.i(TAG, "iBeaconData = " + iBeaconData.beaconUuid);
            }

            @Override
            public void onBatteryPowerScaned(BatteryPowerData batteryPowerData) {
                Log.i(TAG, "BatteryPowerData = " + batteryPowerData.toString());
            }
        });
        mBeaconManager.startScaniBeacon(99999999);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBeaconManager.stopScaniBeacon();
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
}
