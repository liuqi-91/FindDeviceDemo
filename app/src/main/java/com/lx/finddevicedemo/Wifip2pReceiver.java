package com.lx.finddevicedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_DEVICE;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_STATE;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION;

class Wifip2pReceiver  extends BroadcastReceiver {
    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mChannel;
    private final Wifip2pActionListener mListener;

    public Wifip2pReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Wifip2pActionListener listener) {
        mWifiP2pManager= wifiP2pManager;
        mChannel= channel;
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

        switch (intent.getAction()) {
            // wifi p2p 状态变化
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                if (state == WIFI_P2P_STATE_ENABLED) {
                    mListener.wifiP2pEnabled(true);
                }else {
                    mListener.wifiP2pEnabled(false);
                }
                break;
                //对等设备改变
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        mListener.onPeersInfo(peers.getDeviceList());
                    }
                });
                break;
                //连接状态变化
            case WIFI_P2P_CONNECTION_CHANGED_ACTION:
                if (state == WIFI_P2P_STATE_ENABLED) {
                    mWifiP2pManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            mListener.onConnection(info);
                        }
                    });
                } else {
                    mListener.onDisconnection();
                }
                break;
                //设备信息变化
            case WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                WifiP2pDevice device = intent.getParcelableExtra(EXTRA_WIFI_P2P_DEVICE);
                mListener.onDeviceInfo(device);
                break;
                default:
                    break;
        }
    }
}
