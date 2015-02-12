package me.eighttenlabs.toggle;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Ian on 12/25/2014.
 * Class to send messages to a server on the pc
 */
public class Sender {

    public static final String CLOSE_CONNECTION = "CLOSE";
    public static final String LOGIN = "LOGIN";

    public static final String ACKNOWLEDGE = "ACK:";
    public static final int SOCKET = 8745;
    public InetAddress serverIP;
    boolean awaitingConfirmation;
    private Context context;
    private String serverMessage;
    private String sentMessage;
    private boolean run = false;
    private PrintWriter bufferOut;
    private BufferedReader bufferIn;
    private String uid;
    private boolean systemStop;

    public Sender(Context context, String address) {
        this.context = context;
        try {
            serverIP = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String type, int mes) {
        sendCommand(type, mes + "");
    }

    public void pressButton(String name) {
        sendCommand(Constants.SPECIAL, name + ":1");
    }

    public void releaseButton(String name) {
        sendCommand(Constants.SPECIAL, name + ":2");
    }

    public void sendMouseMove(int x, int y) {
        sendCommand(Constants.MOUSE_MOVE, x + ":" + y);
    }

    public void sendCommand(String type, String mes) {
        if (!awaitingConfirmation) {
            sentMessage = type + ":" + mes;
            if (bufferOut != null && !bufferOut.checkError()) {
                bufferOut.println(sentMessage);
                bufferOut.flush();
                awaitingConfirmation = true;
            }
        }
    }


    public void stopClient() {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        uid = tManager.getDeviceId();
        sendCommand(CLOSE_CONNECTION, uid);

        run = false;

        if (bufferOut != null) {
            bufferOut.close();
        }

        systemStop = true;
        bufferOut = null;
        bufferIn = null;
        serverMessage = null;
    }

    public boolean isSystemStop() {
        return systemStop;
    }

    public void run() {
        systemStop = false;

        run = true;
        awaitingConfirmation = false;
        try {
            InetAddress serverAddress = serverIP;
            Socket socket;
            try {
                socket = new Socket(serverAddress, SOCKET);
            } catch (ConnectException e) {
                Thread.sleep(100);
                try {
                    socket = new Socket(serverAddress, SOCKET);
                } catch (ConnectException f) {
                    socket = null;
                }
            }
            try {
                if (socket != null) {
                    bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    run = login();
                    awaitingConfirmation = false;
                } else {
                    run = false;
                }
                while (run) {
                    serverMessage = bufferIn.readLine();
                    if (awaitingConfirmation) {
                        if (serverMessage.equals(ACKNOWLEDGE + sentMessage)) {
                            bufferOut.println(ACKNOWLEDGE);
                            bufferOut.flush();
                            awaitingConfirmation = false;
                            sentMessage = null;
                            serverMessage = null;
                        } else {
                            bufferOut.println("Fail");
                            bufferOut.flush();
                            awaitingConfirmation = false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean login() {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        uid = tManager.getDeviceId();
        String deviceName = BluetoothAdapter.getDefaultAdapter().getName();
        if (deviceName == null) {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                deviceName = model;
            } else {
                deviceName = manufacturer + " " + model;
            }
        }
        sendCommand(LOGIN, uid + ":" + deviceName);
        String loginResponse = "defaultString";
        try {
            loginResponse = bufferIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (loginResponse.equals("ACCEPT")) {
            return true;
        }
        if (loginResponse.equals("REJECT")) {
            return false;
        }
        return false;
    }
}

