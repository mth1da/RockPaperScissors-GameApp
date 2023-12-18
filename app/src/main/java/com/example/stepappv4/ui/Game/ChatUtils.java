package com.example.stepappv4.ui.Game;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

// The base of this code was created by following the video series: https://www.youtube.com/watch?v=SZyuFLb_wWU
// The code can be found on github: https://github.com/qaifikhan/AndroidTutorials/tree/master/BluetoothChatApp
// Afterwards it was modified by us to fit our needs.

public class ChatUtils {
    private Context context;
    private final Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    private final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final String APP_NAME = "RockPaperScissors";

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private int state;


    public ChatUtils(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;

        state = STATE_NONE;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int getState() {
        return state;
    }

    public synchronized void setState(int state) {
        this.state = state;
        handler.obtainMessage(Game_01_Matching.MESSAGE_STATE_CHANGED, state, -1).sendToTarget();
    }

    private synchronized void start() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);
    }

    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // NEW
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_NONE);
    }

    public void connect(BluetoothDevice device) {
        if (state == STATE_CONNECTING) {
            connectThread.cancel();
            connectThread = null;
        }

        connectThread = new ConnectThread(device);
        connectThread.start();

        // NEW
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_CONNECTING);
    }

    public void write(byte[] buffer) {
        ConnectedThread connThread;
        synchronized (this) {
            if (state != STATE_CONNECTED) {
                return;
            }

            connThread = connectedThread;
        }

        connThread.write(buffer);
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                } else {
                    tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, APP_UUID);
                }
            } catch (IOException e) {
                Log.e("Accept->Constructor", e.toString());
            }

            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e("Accept->Run", e.toString());
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    Log.e("Accept->Close", e.toString());
                }
            }

            if (socket != null) {
                switch (state) {
                    case STATE_LISTEN:
                    case STATE_CONNECTING:
                        connected(socket, socket.getRemoteDevice());
                        break;
                    case STATE_NONE:
                    case STATE_CONNECTED:
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.e("Accept->CloseSocket", e.toString());
                        }
                        break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("Accept->CloseServer", e.toString());
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;

            BluetoothSocket tmp = null;
            try {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                } else {
                    tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
                }
                //tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e("Connect->Constructor", e.toString());
            }

            socket = tmp;
        }

        public void run() {
            try {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                } else {
                    socket.connect();
                }
            } catch (IOException e) {
                Log.e("Connect->Run", e.toString());
                try {
                    socket.close();
                } catch (IOException e1) {
                    Log.e("Connect->CloseSocket", e.toString()); // TODO: Shouldn't the toString run on e1 instead of e?
                }
                connectionFailed();
                return;
            }

            synchronized (ChatUtils.this) {
                connectThread = null;
            }
            connected(socket, device);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Connect->Cancel", e.toString());
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            boolean tester = true;

            while (tester) {
                try {
                    bytes = inputStream.read(buffer);

                    handler.obtainMessage(Game_01_Matching.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    tester = false;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                handler.obtainMessage(Game_01_Matching.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {

            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {

            }
        }
    }

    private void connectionLost() {
        Message message = handler.obtainMessage(Game_01_Matching.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        // If you change this string, change it in the handler in Game_01_Matchig.java too!
        bundle.putString(Game_01_Matching.TOAST, "Connection Lost");
        message.setData(bundle);
        handler.sendMessage(message);
        ChatUtils.this.stop();
    }

    private synchronized void connectionFailed() {
        Message message = handler.obtainMessage(Game_01_Matching.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Game_01_Matching.TOAST, "Can't connect to the device");
        message.setData(bundle);
        handler.sendMessage(message);

        ChatUtils.this.start();
    }

    public synchronized void connectFailerOnPurpose() {
        ChatUtils.this.start();
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        Message message = handler.obtainMessage(Game_01_Matching.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        } else {
            bundle.putString(Game_01_Matching.DEVICE_NAME, device.getName());
        }
        message.setData(bundle);
        handler.sendMessage(message);

        setState(STATE_CONNECTED);
    }
}

