package com.example.ian.htpcremote;

import android.inputmethodservice.KeyboardView;

/**
 * Keyboard listener for future use when soft keyboard is used instead of text box
 * <p/>
 * Created by Ian on 1/3/2015.
 */
public class KeyBoardListener implements KeyboardView.OnKeyboardActionListener {

    Sender sender;

    public KeyBoardListener(Sender cs) {
        sender = cs;
    }

    @Override
    public void onPress(int primaryCode) {
        sender.sendCommand(Sender.TEXT, primaryCode);
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeUp() {
    }
}
