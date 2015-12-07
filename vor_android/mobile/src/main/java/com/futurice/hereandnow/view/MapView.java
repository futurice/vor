package com.futurice.hereandnow.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;

import uk.co.senab.photoview.PhotoView;

public class MapView extends PhotoView {
    private static final float animationSpeed = .1f;
    private static final int FRAME_RATE = 30;
    private static final float updateRadius = 0.5f;

    float mapLocationX, mapLocationY;
    private Paint markerPaint;
    private float locationOnScreenX, locationOnScreenY, currentLocationX, currentLocationY;
    float markerRadius;

    private Handler h;

    Bitmap mBitmap;
    Canvas mCanvas;
    Context context;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        markerPaint = new Paint();
        markerPaint.setAntiAlias(true);
        markerPaint.setColor(Color.GREEN);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(8f);

        mapLocationX = 0f;
        mapLocationY = 0f;

        locationOnScreenX = -1f;
        locationOnScreenY = -1f;
        currentLocationX = locationOnScreenX;
        currentLocationY = locationOnScreenY;

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

        float distanceX = Math.abs(locationOnScreenX - currentLocationX);
        float distanceY = Math.abs(locationOnScreenY - currentLocationY);

        if (distanceX > updateRadius) {
            if (currentLocationX < locationOnScreenX) {
                currentLocationX += (animationSpeed * distanceX);
            } else if (currentLocationX > locationOnScreenX) {
                currentLocationX -= (animationSpeed * distanceX);
            }
        }

        if (distanceY > updateRadius) {
            if (currentLocationY < locationOnScreenY) {
                currentLocationY += (animationSpeed * distanceY);
            } else if (currentLocationY > locationOnScreenY) {
                currentLocationY -= (animationSpeed * distanceY);
            }
        }

        // Draw the marker for the phone's location.
        if (locationOnScreenX >= 0 && locationOnScreenY >= 0) {
            canvas.drawCircle(currentLocationX, currentLocationY, markerRadius, markerPaint);
        }
        h.postDelayed(runnable, FRAME_RATE);
    }

    /**
     *
     * @param newX the new X coordinate.
     * @param newY the new Y coordinate.
     * @param moveEvent True if the method is called during a move event, false otherwise
     */
    public void setDisplayedLocation(float newX, float newY, Boolean moveEvent) {
        if (moveEvent) {
            float differenceX = Math.abs(newX - locationOnScreenX);
            float differenceY = Math.abs(newY - locationOnScreenY);

            if (newX > locationOnScreenX) {
                currentLocationX += differenceX;
            } else if (newX < locationOnScreenX) {
                currentLocationX -= differenceX;
            }

            if (newY > locationOnScreenY) {
                currentLocationY += differenceY;
            } else if (newY < locationOnScreenY) {
                currentLocationY -= differenceY;
            }
        }

        locationOnScreenX = newX;
        locationOnScreenY = newY;
    }

    public void scaleRadius(float scaleFactor) {
        markerRadius *= scaleFactor;
    }

    public void setLocation(float newX, float newY) {
        mapLocationX = newX;
        mapLocationY = newY;
    }

    public float getMapLocationX() {
        return this.mapLocationX;
    }

    public float getMapLocationY() {
        return this.mapLocationY;
    }
}
