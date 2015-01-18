package com.example.ian.toggle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Custom Edittext with defined TextWatcher, IME Options and Backspace included
 * <p/>
 * Created by Ian on 1/16/2015.
 */
public class TypeBox extends EditText {

    MainActivity activity;

    public TypeBox(Context context) {
        super(context);
    }

    public TypeBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TypeBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setActivity(final MainActivity act) {
        activity = act;
        addTextChangedListener(new Watcher());
        setImeOptions(EditorInfo.IME_ACTION_NONE);
        this.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        activity.sender.pressButton(Constants.ENTER);
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        activity.sender.releaseButton(Constants.ENTER);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        return new TypeInputConnection(super.onCreateInputConnection(outAttrs), true);
    }

    private class TypeInputConnection extends InputConnectionWrapper {
        public TypeInputConnection(InputConnection target, Boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {

            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                activity.sender.pressButton(Constants.BACK);
            } else if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                activity.sender.releaseButton(Constants.BACK);
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    private class Watcher implements TextWatcher {
        TypeBox text;

        public Watcher() {
            this.text = activity.text;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                String string = s.toString();
                string = string.substring(string.length() - 1);
                activity.sender.sendCommand(Constants.TEXT, string);
                text.setText("");
            }
        }
    }
}
