package com.gson8.floatingballwindow;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    Switch mSwitch;

    PopupService mPopupService;

    private ServiceConnection mServiceConnection;

    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitch = (Switch) findViewById(R.id.id_open_floating);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onMyCheckedChanged(isChecked);
            }
        });

        myBindService();

    }

    public void onMyCheckedChanged(boolean isChecked) {
        if(isChecked) {
            mPopupService.show();
        } else {
            mPopupService.dimiss();
        }
    }

    private void myBindService() {

        mServiceIntent = new Intent(MainActivity.this, PopupService.class);

        if(mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mPopupService = ((PopupService.PopupBinder) service).getService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    private void myUnBindService() {
        if(null != mServiceConnection) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
    }

/*
    @Override
    protected void onPause() {
        myUnBindService();
        super.onPause();
    }
*/

    @Override
    protected void onStop() {
        myUnBindService();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        myUnBindService();
        super.onDestroy();
    }


/*
    @Override
    protected void onResume() {
        myBindService();
        super.onResume();
    }

*/

}
