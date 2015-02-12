package me.eighttenlabs.toggle;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;


public class ServerSelectActivity extends ActionBarActivity {

    public static final int SOCKET = 4567;

    ArrayList<Server> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkSearch search = new NetworkSearch();
        ArrayList<Server> servers = null;
        try {
            servers = search.execute("").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        list = servers;
        if (list == null || list.isEmpty()) {
            setContentView(R.layout.activity_server_select_emptylist);
            findViewById(R.id.button_rescan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rescanNetwork();
                }
            });
            return;
        }
        setContentView(R.layout.activity_server_select);
        ListView serverList = (ListView) findViewById(R.id.server_list);
        ServerAdapter adapter = new ServerAdapter(ServerSelectActivity.this, list);
        serverList.setAdapter(adapter);
        serverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Server server = (Server) parent.getItemAtPosition(position);
                PairServer ps = new PairServer(list, server);
                list = null;
                ps.execute();
                Intent intent = new Intent(ServerSelectActivity.this, MainActivity.class);
                intent.putExtra("server_ip", server.ip);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (list != null && !list.isEmpty()) {
            new PairServer(list, null).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_rescan:
                rescanNetwork();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void rescanNetwork() {
        new PairServer(list, null).execute();
        this.recreate();
    }

    private class PairServer extends AsyncTask<Void, Void, Void> {
        public static final String PAIR = "PAIR";
        public static final String NOPAIR = "NOPAIR";

        public Server server;
        public ArrayList<Server> list;

        public PairServer(ArrayList<Server> servers, Server pairingServer) {
            list = servers;
            server = pairingServer;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] pair = PAIR.getBytes();
                byte[] noPair = NOPAIR.getBytes();
                for (Server s : list) {
                    if (s.equals(server)) {
                        DatagramPacket packet = new DatagramPacket(pair, pair.length, InetAddress.getByName(s.ip), SOCKET);
                        socket.send(packet);
                    } else {
                        DatagramPacket packet = new DatagramPacket(noPair, noPair.length, InetAddress.getByName(s.ip), SOCKET);
                        socket.send(packet);
                    }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class NetworkSearch extends AsyncTask<String, String, ArrayList<Server>> {

        public static final String SEND_CODE = "DISCOVER_REMOTESERVER_REQUEST";
        public static final String RECEIVE_CODE = "DISCOVER_REMOTESERVER_RESPONSE";

        @Override
        protected ArrayList<Server> doInBackground(String... params) {
            ArrayList<Server> availableServers = new ArrayList<>();
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);

                byte[] sendData = SEND_CODE.getBytes();

                try {
                    DatagramPacket datagramPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), SOCKET);
                    socket.send(datagramPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue;
                    }

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }
                        try {
                            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, broadcast, SOCKET);
                            socket.send(packet);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                int ipAddress = wm.getConnectionInfo().getIpAddress();
                String address = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                socket.setSoTimeout(1000);
                while (true) {
                    try {
                        byte[] receiveBuffer = new byte[15000];
                        DatagramPacket received = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(received);

                        String message = new String(received.getData()).trim();
                        if (message.substring(0, RECEIVE_CODE.length()).equals(RECEIVE_CODE)) {
                            availableServers.add(new Server(message.substring(RECEIVE_CODE.length() + 1), received.getAddress().toString().substring(1)));
                        }
                    } catch (SocketTimeoutException e) {
                        socket.close();
                        return availableServers;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (socket != null) {
                socket.close();
            }
            return availableServers;
        }
    }
}
