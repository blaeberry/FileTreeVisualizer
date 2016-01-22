package io.github.blaeberry.filetreevisualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.View;import java.lang.Override;import java.lang.String;import java.lang.StringBuilder;

/**
 * Created by Evan on 1/19/2016.
 */

/**
 * should probably throw in some Log.d() to figure out the life cycle...
 */
public class DirectoryView extends View {

    private ShapeDrawable drawable;
    public static float TEXT_SIZE = 40, MAX_SIZE = 300;
    private float proportion;
    private int row, size;
    private Rect viewBounds;
    private Paint textPaint;
    public static final String CVIEW_TAG = "cview";
    private String text;

    public DirectoryView(Context context) {
        super(context);
        row = 1;
        setProportion(1.f);
        drawable = new ShapeDrawable(new OvalShape());
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setStyle(Paint.Style.FILL);
        text = "HELLO THIS IS A SUPER LONG MESSAGE I WONDER HOW FAR YOU WILL GET IN IT";
        viewBounds = new Rect();
    }

    //potential bugs for 2 reasons (1) if not attached to layout (2) if not called before viewBounds
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(CVIEW_TAG, "size changed!");
        int[] tempCoords = new int[2];
        getLocationOnScreen(tempCoords);
        viewBounds.left = tempCoords[0];
        viewBounds.top = tempCoords[1];
        viewBounds.right = viewBounds.left + size;
        viewBounds.bottom = viewBounds.top + size;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        determineBounds();
        determineColors();
        drawable.draw(canvas);
        Log.d(CVIEW_TAG, "x: " + viewBounds.exactCenterX() + "| y: " + viewBounds.exactCenterY()
                + "| size/2: " + (size / 2));
        String displayedText = calculateDisplayedText(text, textPaint);
        canvas.drawText(displayedText, size / 2, size / 2, textPaint);
    }

    private void determineBounds() {
        drawable.setBounds(0, 0, size, size);
    }

    private void determineColors() {
        switch (row % 6) {
            default:
                drawable.getPaint().setColor(0xff006666);
        }
    }

    public String calculateDisplayedText(String originalText, Paint paint) {
        if (paint == null) {
            Log.e(CVIEW_TAG, "Paint is undefined!");
            return null;
        }
        if (viewBounds == null) {
            Log.e(CVIEW_TAG, "viewBounds is undefined!");
            return null;
        }

        float limit = viewBounds.width();
        if (limit <= 0) {
            Log.e(CVIEW_TAG, "Unexpected viewBounds width!");
            return null;
        }

        //if the original text already fits in the view then just display that
        if (paint.measureText(originalText) < limit)
            return originalText;

        StringBuilder finalTextBuilder = new StringBuilder("...");
        float calculatedWidth = paint.measureText(finalTextBuilder.toString());
        int originalTextLength = originalText.length();

        int i;
        //add a character from the original text to the new text until no more will fit inside view
        for (i = 0; calculatedWidth < limit; i++) {
            finalTextBuilder.insert(i, originalText.charAt(i));
            calculatedWidth = paint.measureText(finalTextBuilder.toString());
        }
        String finalText = finalTextBuilder.toString();
        if (finalText.equals("..."))
            return finalText;
        return finalText.substring(0, i - 1) + finalText.substring(i);
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
        size = (int) (proportion * MAX_SIZE);
        requestLayout();
        invalidate();
    }

    public void setRow(int row) {
        this.row = row;
        requestLayout();
        invalidate();
    }

    public void setText(String text) {
        this.text = text;
        requestLayout();
        invalidate();
    }

    public float getProportion() {
        return proportion;
    }

    public int getRow() {
        return row;
    }

    public String getText() {
        return text;
    }

    public Rect getViewBounds() {
        return viewBounds;
    }
}
