package com.nirupama.prasad.mapmysjsu;

import android.content.Intent;
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
import android.widget.Toast;

import static java.security.AccessController.getContext;



//Geographical coordinates of map:
// Top left: 37°20'08.9"N 121°53'09.4"W
// Top right: 37.335798, -121.885934
// Bottom left: 37.331626, -121.882812
// Bottom right: 37.334603, -121.876557

public class MapActivity extends AppCompatActivity  {

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

    public static final int TOTAL_BUILDING_COUNT = 6;


    public static final String[] GEOCOORDINATES = new String[]{
        "37.337359, -121.881909",
        "37.335716, -121.885213",
        "37.333492, -121.883756",
        "37.336361, -121.881282",
        "37.336530, -121.878717",
        "37.333385, -121.880264"
    };


    public static final String[] ADDRESSES = new String[] {
        "Charles W. Davidson College of Engineering, 1 Washington Square, San Jose, CA 95112",
        "Dr. Martin Luther King, Jr. Library, 150 East San Fernando Street, San Jose, CA 95112",
        "Yoshihiro Uchida Hall, San Jose, CA 95112",
        "Student Union Building, San Jose, CA 95112",
        "Boccardo Business Complex, San Jose, CA 95112",
        "San Jose State University South Garage, 330 South 7th Street, San Jose, CA 95112"
    };

    public static final String[] LOCATIONS = new String[] {
        "Engineering Building",
        "King Library",
        "Yoshihiro Uchida Hall",
        "Student Union",
        "BBC",
        "South Parking Garage"
    };

    public static final float[][] coordinates = new float[][] {
        //           TLX  TRY  TRX  BRY
        new float[] {749, 529, 960, 720}, //ENGR BUILDING
        new float[] {193, 493, 312, 690}, //KING LIBRARY
        new float[] {107, 974, 319, 1146}, //YOSHIHIRO HALL
        new float[] {745, 758, 1046, 881}, //STUDENT UNION
        new float[] {1160, 880, 1333, 990}, //BBC
        new float[] {458, 1332, 708, 1504} //SOUTH PARKING
    };

    public Building[] map_buildings = new Building[TOTAL_BUILDING_COUNT];


    //Setup all activity in constructor
    MapActivity()
    {
        for (int i = 0; i < TOTAL_BUILDING_COUNT; i++){
            map_buildings[i] = new Building( LOCATIONS[i], coordinates[i]);
            map_buildings[i].setBuilding_address(ADDRESSES[i]);
            map_buildings[i].setBuilding_coordinates(GEOCOORDINATES[i]);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
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


        //Set up touch
        mapImageView.setOnTouchListener(map_touch_listener);


        //Enable/disable touch
        //mapImageView.setOnTouchListener(this);
        //Enable or disable scaler
        //mapScaleGestureDetector = new ScaleGestureDetector(this, new MapScaleListener());

        //Start map at the center
        CenterMapImage();

    }

    private View.OnTouchListener map_touch_listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float screenX = event.getX();
                float screenY = event.getY();
                float viewX = screenX - v.getLeft();
                float viewY = screenY - v.getTop();
                Log.d("MapActivity",  "X: " + viewX + " Y: " + viewY + " ScreenX: " + screenX + " ScreenY:" +screenY);

                ProcessTouchCoordinate(v, viewX, viewY);
                return true;
            }
            return false;
        }
    };

    private void ProcessTouchCoordinate(View v, float x, float y){

        for(int i = 0; i < TOTAL_BUILDING_COUNT; i++){
            if(map_buildings[i].IsWithinPixelBounds(x,y)) {
                //Toast.makeText(v.getContext(), map_buildings[i].building_name, Toast.LENGTH_SHORT).show();
                Intent bldgIntent = new Intent(this, BuildingActivity.class);
                bldgIntent.putExtra("BUILDING_DETAILS", new String[] {
                        map_buildings[i].building_name,
                        map_buildings[i].building_address,
                        map_buildings[i].building_coordinates
                });
                startActivity(bldgIntent);
            }
        }


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


}
