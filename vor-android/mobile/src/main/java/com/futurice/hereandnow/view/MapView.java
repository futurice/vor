package com.futurice.hereandnow.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

import com.futurice.hereandnow.utils.PeopleManager;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public class MapView extends PhotoView {
    private static final float animationSpeed = .05f;
    private static final int FRAME_RATE = 30;
    private static final float updateRadius = 1.f;

    float markerRadius;

    private Handler h;

    float displayedWidth, displayedHeight;

    Bitmap mBitmap;
    Canvas mCanvas;
    Context context;

    private OnMapDrawListener onMapDrawListener;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        markerRadius = 20.f;

        h = new Handler();
    }

    private Runnable runnable = () -> invalidate();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (onMapDrawListener == null) {
            return;
        }

        for (PeopleManager.Person person : onMapDrawListener.getPersons()) {
            person.updateCurrentLocation(animationSpeed, updateRadius); // Animate the markers.

            if (person.getLocationOnScreenX() >= 0 && person.getLocationOnScreenY() >= 0) {
                canvas.drawCircle(person.getCurrentLocationX(),
                        person.getCurrentLocationY(),
                        markerRadius,
                        person.getPaint());
            }
        }

        h.postDelayed(runnable, FRAME_RATE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        float[] matrix = new float[9];
        getImageMatrix().getValues(matrix);

        final float scaleX = matrix[Matrix.MSCALE_X];
        final float scaleY = matrix[Matrix.MSCALE_Y];

        final Drawable drawable = getDrawable();
        final int originalWidth = drawable.getIntrinsicWidth();
        final int originalHeight = drawable.getIntrinsicHeight();

        displayedWidth = originalWidth * scaleX;
        displayedHeight = originalHeight * scaleY;
    }

    public float getDisplayedWidth() {
        return this.displayedWidth;
    }

    public float getDisplayedHeight() {
        return this.displayedHeight;
    }

    public void scaleRadius(float scaleFactor) {
        markerRadius *= scaleFactor;
    }

    public interface OnMapDrawListener {
        ArrayList<PeopleManager.Person> getPersons();
    }

    public void setOnMapDrawListener(OnMapDrawListener listener) {
        onMapDrawListener = listener;
    }
}
