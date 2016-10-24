package com.nirupama.prasad.mapmysjsu;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;


//Coordinates:
// Top left: 37°20'08.9"N 121°53'09.4"W
// Top right: 37.335798, -121.885934
// Bottom left: 37.331626, -121.882812
// Bottom right: 37.334603, -121.876557

public class MapActivity extends AppCompatActivity {

    //In order to make the map pinch zoomable
    ImageView mapImageView;
    Matrix mapMatrix = new Matrix();
    float mapInitScale = 1f;
    ScaleGestureDetector mapScaleGestureDetector;


    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    //Data for autocompleting map search bar
    /* ADDRESSES:
            "King Library:  Dr. Martin Luther King, Jr. Library, 150 East San Fernando Street, San Jose, CA 95112",
            "Engineering Building:  San José State University Charles W. Davidson College of Engineering, 1 Washington Square, San Jose, CA 95112",
            "Yoshihiro Uchida Hall:  Yoshihiro Uchida Hall, San Jose, CA 95112",
            "Student Union:  Student Union Building, San Jose, CA 95112",
            "BBC : Boccardo Business Complex, San Jose, CA 95112",
            "South   Parking   Garage:  San Jose State University South Garage, 330 South 7th Street, San Jose, CA 95112"*/
    private static final String[] LOCATIONS = new String[] {
        "King Library",
        "Engineering Building",
        "Yoshihiro Uchida Hall",
        "Student Union",
        "BBC",
        "South Parking Garage:"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Get the image view
        mapImageView = (ImageView) findViewById(R.id.mapImageView);

        //Get the search bar
        AutoCompleteTextView map_search_bar = (AutoCompleteTextView) findViewById(R.id.map_search_bar);

        //Set up map toolbar
        Toolbar map_toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(map_toolbar);

        //Set up status bar
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorToolBar));

        //Set up autocomplete
        ArrayAdapter<String> searchArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, LOCATIONS);
        map_search_bar.setAdapter(searchArrayAdapter);
        map_search_bar.setThreshold(0);


        //Enable/disable touch
        //mapImageView.setOnTouchListener(this);
        //Enable or disable scaler
        //mapScaleGestureDetector = new ScaleGestureDetector(this, new MapScaleListener());

        //Start map at the center
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


    /*
    //Map scaler methods
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

    //@Override
    public boolean onTouchEvent(MotionEvent event) {
        mapScaleGestureDetector.onTouchEvent(event);
        return true;
    }


    //Zoom and pinch events
    public boolean onTouchPinchZoom(View v, MotionEvent event) {
        // handle touch events here
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    if(dx != 0 && dy != 0) {
                        matrix.postTranslate(dx, dy);
                    }
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }*/
}
