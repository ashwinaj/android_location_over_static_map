package com.nirupama.prasad.mapmysjsu;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MapActivity extends AppCompatActivity {

    //In order to make the map pinch zoomable
    ImageView mapImageView;
    Matrix mapMatrix = new Matrix();
    float mapInitScale = 1f;
    ScaleGestureDetector mapScaleGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapImageView = (ImageView) findViewById(R.id.mapImageView);
        mapScaleGestureDetector = new ScaleGestureDetector(this, new MapScaleListener());

        CenterMapImage();

    }

    private void CenterMapImage() {
        //Get image dimensions
        Drawable mapDrawable = mapImageView.getDrawable();
        float imageWidth = mapDrawable.getIntrinsicWidth();
        float imageHeight = mapDrawable.getIntrinsicHeight();

        //Get Screen dimensions
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        //Now center the image to scale to the view's center
        RectF mapDrawableRect = new RectF(0, 0, imageWidth, imageHeight);
        RectF viewImageRect = new RectF(0, 0, screenWidth, screenHeight);
        mapMatrix.setRectToRect(mapDrawableRect, viewImageRect, Matrix.ScaleToFit.CENTER);
        mapImageView.setImageMatrix(mapMatrix);
    }

    private class MapScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float newScaleFactor = detector.getScaleFactor();
            mapInitScale = newScaleFactor * mapInitScale;
            mapInitScale = Math.max(1f, Math.min(mapInitScale, 5f));
            Log.d("MapActivity", "MapScaleListener, initscale = " + mapInitScale + " new scale = " + newScaleFactor);
            mapMatrix.setScale(mapInitScale, mapInitScale);

            mapImageView.setImageMatrix(mapMatrix);
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mapScaleGestureDetector.onTouchEvent(event);
        return true;
    }
}
