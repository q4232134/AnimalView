package jiaozhu.com.animalview.pannel.Interface;

import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by jiaozhu on 16/6/8.
 */
public abstract class OnTouchListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
    View.OnTouchListener listener;

    public OnTouchListener(@Nullable View.OnTouchListener listener) {
        this.listener = listener;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (onTouchEvent(v, event)) {
            return true;
        }
        if (listener != null) {
            return listener.onTouch(v, event);
        } else {
            return false;
        }
    }

    public abstract boolean onTouchEvent(View v, MotionEvent event);
}
