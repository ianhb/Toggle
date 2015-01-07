package com.example.ian.htpcremote;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Ian on 12/25/2014.
 * Class to send messages to a server on the pc
 */
public class Sender {

    public static final String MOUSE_CLICK = "CLICK";
    public static final String MOUSE_RELEASE = "RELEASE";
    public static final String MOUSE_SCROLL = "SCROLL";
    public static final String MOUSE_MOVE = "MOVE";
    public static final String MOUSE_STOP = "STOP";
    public static final String TEXT = "TEXT";

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

        bufferOut = null;
        bufferIn = null;
        serverMessage = null;
    }

    public void run() {
        run = true;
        awaitingConfirmation = false;
        try {
            InetAddress serverAddress = serverIP;
            Socket socket = new Socket(serverAddress, SOCKET);

            Log.d("TCP", "Socket Opened");
            try {
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                uid = tManager.getDeviceId();
                sendCommand(LOGIN, uid);
                awaitingConfirmation = false;

                while (run) {
                    Log.d("TCP", "Reading Message");
                    serverMessage = bufferIn.readLine();
                    Log.d("TCP", serverMessage);
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
                Log.d("TCP", "Error sending or receiving message");
            }

            socket.close();
        } catch (Exception e) {
            Log.d("TCP", "Error opening or closing socket");
            e.printStackTrace();
        }
    }
}

