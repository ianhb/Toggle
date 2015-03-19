package me.eighttenlabs.toggleapp;

import android.support.v4.view.VelocityTrackerCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * Mouse Controller to track and compute finger movements on device and send commands to server
 * <p/>
 * Created by Ian on 1/10/2015.
 */
public class MouseControl {

    ControllerActivity activity;
    private VelocityTracker tracker = null;

    public MouseControl(ControllerActivity activity) {
        this.activity = activity;
    }

    public void onTouch(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (tracker == null) {
                    tracker = VelocityTracker.obtain();
                } else {
                    tracker.clear();
                }
                tracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                tracker.addMovement(event);
                tracker.computeCurrentVelocity(100);
                parseTracker(event, tracker);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                tracker.recycle();
                break;
        }
    }

    private void parseTracker(MotionEvent event, VelocityTracker tracker) {
        switch (event.getPointerCount()) {
            case 1:
                activity.sender.sendMouseMove((int) VelocityTrackerCompat.getXVelocity(tracker, event.getPointerId(0)), (int) VelocityTrackerCompat.getYVelocity(tracker, event.getPointerId(0)));
                break;
            case 2:
                float xDif = VelocityTrackerCompat.getXVelocity(tracker, event.getPointerId(0)) / VelocityTrackerCompat.getXVelocity(tracker, event.getPointerId(1));
                float yDif = VelocityTrackerCompat.getYVelocity(tracker, event.getPointerId(event.getActionIndex())) / VelocityTrackerCompat.getYVelocity(tracker, event.getPointerId(event.getActionIndex()));
                if ((int) xDif == 1 && yDif >= 0) {
                    activity.sender.sendCommand(Constants.MOUSE_SCROLL, (int) VelocityTrackerCompat.getYVelocity(tracker, event.getPointerId(0)) / 20);
                }
                //TODO Pinch Zoom
                break;
        }
    }

}
