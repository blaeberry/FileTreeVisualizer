package io.github.blaeberry.filetreevisualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by Evan on 1/26/2016.
 */
//TODO lines should be held by children nodes so they are updated when
public class LineView extends View {
    private int startX, startY, stopX, stopY;
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

        startX = startY = stopX = stopY = childLeft = childTop = parentLeft = parentTop = 0;

        setupViewsListener();
    }

    private void setupViewsListener() {
        OnLayoutChangeListener listener = new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(left == oldLeft && top == oldTop && right == oldRight && bottom == oldBottom)
                    return;
                if(v == childView) {
                    childLeft = left; childTop = top;
                } else if (v == parentView) {
                    parentLeft = left; parentTop = top;
                } else
                    Log.wtf("Line", "NOT EITHER WAT???");
                startX = parentLeft;
                startY = parentTop;
                stopX = childLeft;
                stopY = childTop - ((int)DirectoryView.MAX_SIZE);
                LineView.this.requestLayout();
                LineView.this.invalidate();
                Log.d("line", ((DirectoryView)v).getText() + " changed! " + left + " " + top + " "
                        + right + " " + bottom);
            }
        };
        parentView.addOnLayoutChangeListener(listener);
        childView.addOnLayoutChangeListener(listener);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(Math.abs(startX - stopX), Math.abs(startY - stopY));
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        Log.d("line", "DRAWING" + startX + " " + stopX + " "
                + startY + " " + stopY);

    }

    public DirectoryView getParentView() {
        return parentView;
    }

    public DirectoryView getChildView() {
        return childView;
    }
}