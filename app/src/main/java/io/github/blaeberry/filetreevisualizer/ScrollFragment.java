package io.github.blaeberry.filetreevisualizer;

import android.app.Fragment;
import android.graphics.Point;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

/**
 * Created by Evan on 1/26/2016.
 */
public class ScrollFragment extends Fragment {

    private GestureDetectorCompat gestureDetector;
    private static final int SCROLL_REFRESH_RATE = 15;
    private static final String SCROLL_TAG = "sFrag";
    private float leftBound, rightBound, topBound, bottomBound;
    RelativeLayout layout;
    private static int screenWidth, screenHeight;
    private OverScroller scroller;

    public void setupScroll(RelativeLayout layout) {
        this.layout = layout;

        Point dimensions = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(dimensions);
        screenWidth = dimensions.x;
        screenHeight = dimensions.y;

        topBound = 0.f;
        leftBound = 0.f;
        rightBound = layout.getWidth();
        bottomBound = layout.getHeight();

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        gestureDetector = new GestureDetectorCompat(layout.getContext(), new SimpleGestureListener());
        scroller = new OverScroller(layout.getContext());
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            scroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(SCROLL_TAG, "onFling: " + event1.toString() + event2.toString() + '\n' + "**"
                    + velocityX + "***" + velocityY);

            scroller.fling(layout.getScrollX(), layout.getScrollY(),
                    (int)-velocityX, (int)-velocityY,
                    (int)leftBound,
                    (int) rightBound - screenWidth,
                    (int)topBound,
                    (int) bottomBound - screenHeight);
            (new updateScroll(scroller)).start();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int newX = layout.getScrollX() + (int) distanceX;
            int newY = layout.getScrollY() + (int) distanceY;
            layout.scrollTo(newX, newY);
            return true;
        }
    }

    private class updateScroll extends Thread {
        private OverScroller sc;

        public updateScroll(OverScroller sc) {
            this.sc = sc;
        }

        @Override
        public void run() {
            while (!sc.isFinished()) {
                int currX = sc.getCurrX(), currY = sc.getCurrY();
                sc.computeScrollOffset();
                layout.scrollTo(currX, currY);
                Log.d(SCROLL_TAG, "scrollX: " + layout.getScrollX() + "| scrollY: " +
                        layout.getScrollY() /* '\n' + "| currX: " + currX + "| currY: " + currY*/);
                try {
                    Thread.sleep(SCROLL_REFRESH_RATE);
                } catch (InterruptedException e) {
                    Log.d(SCROLL_TAG, e.getMessage());
                }
            }
            sc.springBack(layout.getScrollX(), layout.getScrollY(),
                    (int)leftBound, (int)rightBound,
                    (int)topBound, (int)bottomBound);
            Log.d(SCROLL_TAG, leftBound + "*R* " + rightBound + "*T* " + topBound + "*B* " + bottomBound);
        }
    }

}
