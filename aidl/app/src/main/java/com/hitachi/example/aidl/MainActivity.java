package com.hitachi.example.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ICompassService mAltService;
    ICompassService mAlprilService;
    protected ICompassCallback callback = new ICompassCallback.Stub() {
        @Override
        public void onResult(String jsonResult) {
            Log.i("MainActivity", "onResult :" + jsonResult);
        }
    };


    private final ServiceConnection mAltConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAltService = ICompassService.Stub.asInterface(service);
            // mService为AIDL服务
            try {
                mAltService.registerCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Toast.makeText(MainActivity.this, "onServiceConnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAltService = null;
            Toast.makeText(MainActivity.this, "onServiceDisconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private final ServiceConnection mAprilConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAlprilService = ICompassService.Stub.asInterface(service);
            // mService为AIDL服务
            try {
                mAlprilService.registerCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Toast.makeText(MainActivity.this, "onServiceConnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAlprilService = null;
            Toast.makeText(MainActivity.this, "onServiceDisconnected", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnBind = (Button) findViewById(R.id.btn_bind_alt);
        Button btnUnbind = (Button) findViewById(R.id.btn_bind_april);

        btnBind.setOnClickListener(this);
        btnUnbind.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAltService != null) {
            unbindService(mAltConnection);
            mAltService = null;
        }
        if(mAlprilService != null) {
            unbindService(mAprilConnection);
            mAlprilService = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bind_alt:
                if(mAltService == null) {
                    Intent intent = new Intent();
                    intent.setPackage("com.hitachi.example.aidl");
                    intent.setAction("com.hitachi.example.aidl.THLightLocationService");
                    bindService(intent, mAltConnection, Context.BIND_AUTO_CREATE);
                } else {
                    unbindService(mAltConnection);
                    mAltService = null;
                }
                break;
            case R.id.btn_bind_april:
                if(mAlprilService == null) {
                    Intent intent = new Intent();
                    intent.setPackage("com.hitachi.example.aidl");
                    intent.setAction("com.hitachi.example.aidl.AprilLocationService");
                    bindService(intent, mAprilConnection, Context.BIND_AUTO_CREATE);
                } else {
                    unbindService(mAprilConnection);
                    mAlprilService = null;
                }
                break;
        }
    }
}

