package com.lx.finddevicedemo;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements Wifip2pActionListener {
    private final String TAG = "xiaer";
    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public WifiP2pManager mWifiP2pManager;
    public WifiP2pManager.Channel mChannel;
    public Wifip2pReceiver mWifip2pReceiver;
    public WifiP2pInfo mWifiP2pInfo;

    @BindView(R.id.textView)
    TextView textView ;
    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.button)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if(EasyPermissions.hasPermissions(this,perms)){
            Log.d(TAG, "onCreate: has permissions");
        }else{
            EasyPermissions.requestPermissions(this,"haha",1,perms);
        }

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mWifiP2pManager == null) {
            return ;
        }
        refreshLogView("### let's begin... ###");
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mWifip2pReceiver = new Wifip2pReceiver(mWifiP2pManager, mChannel, this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mWifip2pReceiver, intentFilter);

        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), this);

        try {
            Method setDeviceName = mWifiP2pManager.getClass().getMethod("setDeviceName",WifiP2pManager.Channel.class,String.class,WifiP2pManager.ActionListener.class);
            setDeviceName.setAccessible(true);
            setDeviceName.invoke(mWifiP2pManager, mChannel, "DIRECT-LX-xiaer", new WifiP2pManager.ActionListener(){

                @Override
                public void onSuccess() {
                    refreshLogView("setDeviceName success.");
                }

                @Override
                public void onFailure(int reason) {
                    refreshLogView("setDeviceName failure.");
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.button)
    public void onClick() {
        if (TextUtils.isEmpty(editText.getText())) {
            return;
        }
        String text = editText.getText().toString().trim();
        editText.setText("");
        refreshLogView(">>> input is "+text);
        switch (text) {
            case "discovery":
                discovery();
                break;
            case "stop":
                stopDiscovery();
                break;
            case "conn":
                onConnection(null);
                break;
            case "createGroup":
                createGroup();
                break;
            default:
                refreshLogView("invalid input command!!!");
                break;
        }

    }

    private void createGroup() {
        if (mWifiP2pManager == null) {
            return;
        }

        mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "createGroup onSuccess: ");
                refreshLogView("createGroup onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "createGroup onFailure: ");
                refreshLogView("createGroup onFailure");
            }
        });
    }

    public void discovery() {
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: discovery success...");
                refreshLogView("onSuccess: discovery success...");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: discovery fail...");
                refreshLogView("onFailure: discovery fail...");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void stopDiscovery() {
        mWifiP2pManager.stopPeerDiscovery(mChannel, null);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void wifiP2pEnabled(boolean enabled) {
        Log.d(TAG, "wifiP2pEnabled: enabled = "+enabled);
        refreshLogView("wifiP2pEnabled: enabled = " + enabled );
    }

    @Override
    public void onConnection(WifiP2pInfo wifiP2pInfo) {
        refreshLogView("onConnection: info = " + wifiP2pInfo == null ? null : wifiP2pInfo.toString());
        mWifiP2pInfo = wifiP2pInfo;
    }

    @Override
    public void onDisconnection() {
        Log.d(TAG, "onDisconnection: ");
    }

    @Override
    public void onDeviceInfo(WifiP2pDevice wifiP2pDevice) {
        refreshLogView("onDeviceInfo: wifiP2pDevice = " + wifiP2pDevice == null ? null : wifiP2pDevice.toString());
        Log.d(TAG, "onDeviceInfo: "+wifiP2pDevice == null ? null : wifiP2pDevice.toString());
    }

    @Override
    public void onPeersInfo(Collection<WifiP2pDevice> wifiP2pDeviceList) {
        for (WifiP2pDevice device : wifiP2pDeviceList) {
            refreshLogView("onPeersInfo: device = " + device == null ? null : device.toString());
            Log.e(TAG, "连接的设备信息：" + device.deviceName + "--------" + device.deviceAddress);
        }
    }

    @Override
    public void onChannelDisconnected() {
        refreshLogView("onChannelDisconnected: ");
        Log.d(TAG, "onChannelDisconnected: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifip2pReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            stopDiscovery();
        }
    }

    void refreshLogView(String msg){
        textView.append(msg+" \n");
        int offset=textView.getLineCount()*textView.getLineHeight();
        if(offset>textView.getHeight()){
            textView.scrollTo(0,offset-textView.getHeight());
        }
    }
}
