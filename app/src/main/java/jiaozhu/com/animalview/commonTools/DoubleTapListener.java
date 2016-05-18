package jiaozhu.com.animalview.commonTools;

import android.view.MotionEvent;

import uk.co.senab.photoview.DefaultOnDoubleTapListener;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by jiaozhu on 16/5/18.
 */
public abstract class DoubleTapListener extends DefaultOnDoubleTapListener {
    public DoubleTapListener(PhotoViewAttacher photoViewAttacher) {
        super(photoViewAttacher);
    }

    @Override
    public boolean onDoubleTap(MotionEvent ev) {
        onDoubleCLick();
        return super.onDoubleTap(ev);
    }

    public abstract void onDoubleCLick();
}
