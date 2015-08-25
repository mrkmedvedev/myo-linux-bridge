/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.markargus.myolinuxbridge;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;

import java.io.IOException;

public class BackgroundService extends Service implements BluetoothServer.IBluetoothServerListener{
    private static final String TAG = "BackgroundService";
    private BluetoothServer bluetoothServer;

    private Toast mToast;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            showToast(getString(R.string.connected));
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            showToast(getString(R.string.disconnected));
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    break;
                case REST:
                    break;
                case DOUBLE_TAP:
                    break;
                case FIST:
                    sendMessage("1");
                    break;
                case WAVE_IN:
                    sendMessage("2");
                    break;
                case WAVE_OUT:
                    sendMessage("3");
                    break;
                case FINGERS_SPREAD:
                    sendMessage("4");
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        bluetoothServer = new BluetoothServer();
        bluetoothServer.setListener(this);

        try {
            bluetoothServer.start();
        } catch (BluetoothServer.BluetoothServerException e) {
            e.printStackTrace();
        }

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            showToast("Couldn't initialize Hub");
            stopSelf();
            return;
        }

        hub.setLockingPolicy(Hub.LockingPolicy.STANDARD);
        hub.addListener(mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Service is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        Hub.getInstance().shutdown();
    }

    private void showToast(String text) {
        Log.w(TAG, text);
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    private void sendMessage(String message){
        byte[] data = message.getBytes();
        try {
            bluetoothServer.send(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BluetoothServer.BluetoothServerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStarted() {
        showToast("Bluetooth server started.");
    }

    @Override
    public void onConnected() {
        showToast("Bluetooth client connected.");
    }

    @Override
    public void onData(byte[] data) {
        showToast("Received data: " + data.toString());
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onStopped() {
//        showToast("Bluetooth client stopped.");
        try {
            bluetoothServer.start();
        } catch (BluetoothServer.BluetoothServerException e) {
            e.printStackTrace();
        }
    }
}
