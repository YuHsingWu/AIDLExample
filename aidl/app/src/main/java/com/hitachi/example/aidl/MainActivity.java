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
    ICompassService mService;
    protected ICompassCallback callback = new ICompassCallback.Stub() {
        @Override
        public void onResult(float result) {
            Log.i("MainActivity", "onResult :" + result);
        }
    };


    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ICompassService.Stub.asInterface(service);
            // mService为AIDL服务
            try {
                mService.registerCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Toast.makeText(MainActivity.this, "onServiceConnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Toast.makeText(MainActivity.this, "onServiceDisconnected", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnBind = (Button) findViewById(R.id.btn_bind);
        Button btnUnbind = (Button) findViewById(R.id.btn_unbind);
        Button btnCall = (Button) findViewById(R.id.btn_call);
        btnBind.setOnClickListener(this);
        btnUnbind.setOnClickListener(this);
        btnCall.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bind:
                Intent intent = new Intent();
                intent.setPackage("com.hitachi.example.aidl");
                intent.setAction("com.hitachi.example.aidl.LocationService");
                bindService(intent, conn, Context.BIND_AUTO_CREATE);
                break;
            case R.id.btn_unbind:
                if(mService != null) {
                    unbindService(conn);
                }
                break;
            case R.id.btn_call:
                if (null != mService) {
                    try {
                        Toast.makeText(MainActivity.this, "Compass" + mService.getCompass(), Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}

