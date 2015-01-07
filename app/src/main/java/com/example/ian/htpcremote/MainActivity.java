package com.example.ian.htpcremote;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    Button left;
    Button right;
    Button up;
    Button down;
    Button click_left;
    Button click_right;
    Button scroll_up;
    Button scroll_down;
    EditText text;

    Sender sender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String ip = getIntent().getStringExtra("server_ip");
        new ConnectTask().execute(ip);

        left = (Button) findViewById(R.id.button_left);
        right = (Button) findViewById(R.id.button_right);
        up = (Button) findViewById(R.id.button_up);
        down = (Button) findViewById(R.id.button_down);

        scroll_up = (Button) findViewById(R.id.button_scroll_up);
        scroll_down = (Button) findViewById(R.id.button_scroll_down);

        click_left = (Button) findViewById(R.id.button_click_left);
        click_right = (Button) findViewById(R.id.button_click_right);

        text = (EditText) findViewById(R.id.text);

        Button sendText = (Button) findViewById(R.id.send_text);
        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = text.getText().toString();
                sender.sendCommand(Sender.TEXT, message);
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseMove(-1, 0, event);
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseMove(1, 0, event);
                return false;
            }
        });
        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseMove(0, 1, event);
                return false;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseMove(0, -1, event);
                return false;
            }
        });


        scroll_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseScroll(-1, event);
                return false;
            }
        });

        scroll_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseScroll(1, event);
                return false;
            }
        });


        click_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseClick(1, event);
                return false;
            }
        });
        click_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendMouseClick(2, event);
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sender != null) {
            Log.d("TCP", "Closing Socket");
            sender.stopClient();
            sender = null;
        }
    }

    private void sendMouseClick(int button, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            sender.sendCommand(Sender.MOUSE_CLICK, button);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            sender.sendCommand(Sender.MOUSE_RELEASE, button);
        }
    }

    private void sendMouseScroll(int x, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            sender.sendCommand(Sender.MOUSE_SCROLL, x);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            sender.sendCommand(Sender.MOUSE_SCROLL, 0);
        }
    }

    private void sendMouseMove(int x, int y, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            sender.sendCommand(Sender.MOUSE_MOVE, x + ":" + y);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            sender.sendCommand(Sender.MOUSE_STOP, null);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ConnectTask extends AsyncTask<String, String, Sender> {
        @Override
        protected Sender doInBackground(String... params) {
            Log.d("TCP", "Connecting to Server");
            sender = new Sender(getApplicationContext(), params[0]);
            sender.run();
            return null;
        }
    }
}
