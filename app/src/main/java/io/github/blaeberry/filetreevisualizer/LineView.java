package io.github.blaeberry.filetreevisualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Evan on 1/26/2016.
 * This is the line that connects DirectoryViews/nodes. Updates self when either view moves.
 */
//TODO lines should be held by children nodes so they are updated when
public class LineView extends View {
    private int childLeft, childTop, parentLeft, parentTop;
    private static final float STROKE_WIDTH = 8.f;
    private Paint paint;
    private DirectoryView parentView, childView;

    public LineView(Context ctxt, DirectoryView parentView, DirectoryView childView) {
        super(ctxt);
        //TODO could potential place implementation in DirectoryNode for animation
        if(parentView == null || childView == null)
            throw new IllegalArgumentException("views cannot be null");
        this.parentView = parentView;
        this.childView = childView;
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setColor(0xffbdbdbd);

        initialViewPositions();
        setupViewsListener();
    }

    private void initialViewPositions() {
        parentLeft = parentView.getLeft();
        parentTop = parentView.getTop();
        childLeft = childView.getLeft();
        childTop = childView.getTop();
    }

    //Automatically updates self when either view it's connected to change location
    private void setupViewsListener() {
        OnLayoutChangeListener listener = new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(left == oldLeft && top == oldTop && right == oldRight && bottom == oldBottom)
                    return;
                if(v == childView) {
                    childLeft = left;
                    childTop = top;
                } else if (v == parentView) {
                    parentLeft = left;
                    parentTop = top;
                } else
                    Log.wtf("Line", "NOT EITHER WAT???");
                //stopY = childTop - ((int)DirectoryView.MAX_SIZE);
                LineView.this.requestLayout();
                LineView.this.invalidate();
                Log.d("line", ((DirectoryView)v).getText() + " changed! " + left + " " + top + " "
                        + right + " " + bottom);
            }
        };
        parentView.addOnLayoutChangeListener(listener);
        childView.addOnLayoutChangeListener(listener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(parentLeft + parentView.getWidth()/2, parentTop + parentView.getHeight(),
                childLeft + childView.getWidth()/2, childTop, paint);
        Log.d("line", "DRAWING" + parentLeft + " " + parentTop + " "
                + childLeft + " " + childTop);

    }

    public DirectoryView getParentView() {
        return parentView;
    }

    public DirectoryView getChildView() {
        return childView;
    }
}