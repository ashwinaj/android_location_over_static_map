package com.nirupama.prasad.mapmysjsu;

import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

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
    }

    private class MapScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mapInitScale = mapInitScale * detector.getScaleFactor();
            mapInitScale = Math.max(0.1f, Math.min(mapInitScale, 5f));
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
