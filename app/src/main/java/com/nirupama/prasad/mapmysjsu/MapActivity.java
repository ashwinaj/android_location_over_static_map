package com.nirupama.prasad.mapmysjsu;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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


public class MapActivity extends AppCompatActivity {


    //Resource handles
    public static AutoCompleteTextView map_search_bar;
    public static MarkerView marker;
    public static CircleMarkerView circlemarker;
    public static int intXAxisPlotOffset = -20, intYAxisPlotOffset = 600;
    public static ImageView mapImageView;
    public static Matrix mapMatrix = new Matrix();


    //Geolocation statics
    public final static double OneEightyDeg = 180.0d;
    //public static double ImageSizeW = 1440.0, ImageSizeH = 1944.0;
    public static double ImageSizeW = 1407, ImageSizeH = 1486.0;
    public static double offsetImageViewX = 38.0, offsetImageViewY = 0.0;


    //Current location
    public static Location locCurrentLocation;
    public static Location locCurrentHardCodedLocation;
    public static String strCurrentUserLatitude = "";
    public static String strCurrentUserLongitude = "";
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final int REQUEST_CODE = 1337;
    public static final int LOCATION_REQUEST_CODE = REQUEST_CODE;
    public static final int LOCATION_MIN_TIME = 60000;
    public static final int LOCATION_MIN_DISTANCE = 10;
    public static String strCurrentUserLocation = "";

    // working loc: "37.335230,-121.883185"; //Somewhere in the middle for testing


    public static String strMapTopLeftLat = "37.335802";
    public static String strMapTopLeftLong = "-121.885910";

    //37.338877, -121.879668
    public static String strMapTopRightLat = "37.338877";
    public static String strMapTopRightLong = "-121.879668";

    public static String strMapBottomLeftLat = "37.331626";
    public static String strMapBottomLeftLong = "-121.882812";

    public static String strMapBottomRightLat = "37.334603";
    public static String strMapBottomRightLong = "-121.876557";

    public static Location locMapTopLeft, locMapTopRight, locMapBottomLeft, locMapBottomRight;

    private static LocationManager mLocationManager;

    public static final int TOTAL_BUILDING_COUNT = 6;

    private static final int[] BUILDING_RESOURCE_NAMES = new int[]{
            R.drawable.sjsu_engineering_web,
            R.drawable.king,
            R.drawable.yoshihiro,
            R.drawable.student_union,
            R.drawable.bbc,
            R.drawable.south_garage
    };

    public static final String[] GEOCOORDINATES = new String[]{
            "37.337359,-121.881909",
            "37.335716,-121.885213",
            "37.333492,-121.883756",
            "37.336361,-121.881282",
            "37.336530,-121.878717",
            "37.333385,-121.880264"
    };


    public static final String[] ADDRESSES = new String[]{
            "Charles W. Davidson College of Engineering, 1 Washington Square, San Jose, CA 95112",
            "Dr. Martin Luther King, Jr. Library, 150 East San Fernando Street, San Jose, CA 95112",
            "Yoshihiro Uchida Hall, San Jose, CA 95112",
            "Student Union Building, San Jose, CA 95112",
            "Boccardo Business Complex, San Jose, CA 95112",
            "San Jose State University South Garage, 330 South 7th Street, San Jose, CA 95112"
    };

    public static final String[] LOCATIONS = new String[]{
            "Engineering Building",
            "King Library",
            "Yoshihiro Uchida Hall",
            "Student Union",
            "BBC",
            "South Parking Garage"
    };

    public static final float[][] coordinates = new float[][]{
            //           TLX  TRY  TRX  BRY
            new float[]{749, 529, 960, 720}, //ENGR BUILDING
            new float[]{193, 493, 312, 690}, //KING LIBRARY
            new float[]{107, 974, 319, 1146}, //YOSHIHIRO HALL
            new float[]{745, 758, 1046, 881}, //STUDENT UNION
            new float[]{1160, 880, 1333, 990}, //BBC
            new float[]{458, 1332, 708, 1504} //SOUTH PARKING
    };

    public Building[] map_buildings = new Building[TOTAL_BUILDING_COUNT];

    public static String strHardCodedCurrentLocation = "37.333492,-121.883756";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Intializing
        marker = new MarkerView(this);

        //Get latitudes and longitudes of map ready
        locMapTopLeft = GetLocationFromStrings(strMapTopLeftLat, strMapTopLeftLong);
        locMapTopRight = GetLocationFromStrings(strMapTopRightLat, strMapTopRightLong);
        locMapBottomLeft = GetLocationFromStrings(strMapBottomLeftLat, strMapBottomLeftLong);
        locMapBottomRight = GetLocationFromStrings(strMapBottomRightLat, strMapBottomRightLong);
        locCurrentHardCodedLocation = ConvertStringToLatLng(strHardCodedCurrentLocation);

        //Moving to here because Android complains about constructor
        for (int i = 0; i < TOTAL_BUILDING_COUNT; i++) {
            map_buildings[i] = new Building(LOCATIONS[i], coordinates[i]);
            map_buildings[i].setBuilding_address(ADDRESSES[i]);
            map_buildings[i].setBuilding_coordinates(GEOCOORDINATES[i]);
            map_buildings[i].setBuilding_image_resource_name(BUILDING_RESOURCE_NAMES[i]);
        }

        //Get the image view
        mapImageView = (ImageView) findViewById(R.id.mapImageView);

        //Get the search bar
        map_search_bar = (AutoCompleteTextView) findViewById(R.id.map_search_bar);

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

        //Start map at the center
        CenterMapImage();

        //Test out current location code
        GetCurrentLocation(this);
        Log.d("MainActivity", strCurrentUserLocation);

        //Brave attempt at plotting current location on the map
        //There are bugs here with plotpin
        //ImageSizeW = ImageSizeW - offsetImageViewX;



    }

    public void updateCurrentUserLocationOnMap(){

        ExecuteCustomExperiments(locMapTopLeft, locMapTopRight, locMapBottomLeft, locMapBottomRight, locCurrentHardCodedLocation);

        //Only call after location is retrieved
        //float current_X = (float) GetCurrentPixelX(locMapTopLeft, locMapBottomRight, locCurrentHardCodedLocation);
        //float current_Y = (float) GetCurrentPixelY(locMapTopLeft, locMapBottomRight, locCurrentHardCodedLocation);
        //PlotCircle(this, current_X, current_Y);

    }

    private void ExecuteCustomExperiments(Location locMapTopLeft, Location locMapTopRight,
                                    Location locMapBottomLeft, Location locMapBottomRight,
                                    Location locCurrentHardCodedLocation) {

        //Test out different logical things here
        //What is the bearing from topleft to topright

        double angle = 30.0;
        double r = Math.toRadians(angle);

        double bearingTLtoTR = locMapTopLeft.bearingTo(locMapTopRight);
        double distanceTLtoTR = locMapTopLeft.distanceTo(locMapTopRight);
        double bearingTRtoTL = locMapTopRight.bearingTo(locMapTopLeft);

        //Get the old points
        float current_X = (float) GetCurrentPixelX(locMapTopLeft, locMapBottomRight, locCurrentHardCodedLocation);
        float current_Y = (float) GetCurrentPixelY(locMapTopLeft, locMapBottomRight, locCurrentHardCodedLocation);

        //Let's rotate this about the top left
        float center_X = 13;
        float center_Y = 445;

        float new_X = (float) (center_X + ((current_X - center_X) * Math.cos(r) + (current_Y - center_Y) * Math.sin(r)));
        float new_Y = (float) (center_Y + ((current_X - center_X) * -1 * (Math.sin(r)) + (current_Y - center_Y) * Math.cos(r)));

        PlotCircle(this, new_X, new_Y);

    }

    //Get text from AutoCompleteTextView
    //Match to what we know -
    //Get its coordinates - PlotPin
    public void btnSearchHandler(View v){

        AutoCompleteTextView auto_text = (AutoCompleteTextView) findViewById(R.id.map_search_bar);
        String search_text =  auto_text.getText().toString();

        if(search_text.isEmpty() == true || search_text == "" || search_text == null){
            return;
        }

        int building_count = -1;
        search_text = search_text.toLowerCase();
        for(int i=0; i < LOCATIONS.length; i++){
            if (search_text.equals(LOCATIONS[i].toLowerCase())){
                building_count = i;
                break;
            }
        }

        if (building_count == -1){
            return;
        }
        //Check only if user has entered a valid building
        if(marker != null){
            RelativeLayout map_layout = (RelativeLayout) findViewById(R.id.activity_map);
            map_layout.removeView(marker);
        }

        float[] building_pixel_coordinates = map_buildings[building_count].getPixel_coordinates();

        float pixelTopRightX = building_pixel_coordinates[2];
        float pixelTopRightY = building_pixel_coordinates[1];
        float pixelBottomRightY = building_pixel_coordinates[3];
        float pixelTopLeftX = building_pixel_coordinates[0];

        float plotPixelX = (pixelTopLeftX + pixelTopRightX)/2;
        float plotPixelY = (pixelTopRightY + pixelBottomRightY)/2;

        //Plot it
        PlotPin(this, plotPixelX, plotPixelY);

    }

    //POTENTIAL BUGS
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
         //ImageSizeW = mapImageView.getWidth();
         //ImageSizeH = mapImageView.getHeight();
    }



    @Override
    protected void onResume() {
        super.onResume();

        final AutoCompleteTextView auto_text_view = (AutoCompleteTextView) findViewById(R.id.map_search_bar);
        auto_text_view.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                Log.d("MapActivity", "after text changed");
                if(auto_text_view.getText().toString().equals("") || auto_text_view.getText().toString() == null){
                    if(marker != null){
                        RelativeLayout map_layout = (RelativeLayout) findViewById(R.id.activity_map);
                        map_layout.removeView(marker);
                    }
                }
            }
        });
    }

    private void ProcessTouchCoordinate(View v, float x, float y) {

        for (int i = 0; i < TOTAL_BUILDING_COUNT; i++) {
            if (map_buildings[i].IsWithinPixelBounds(x, y)) {
                //Toast.makeText(v.getContext(), map_buildings[i].building_name, Toast.LENGTH_SHORT).show();
                Intent bldgIntent = new Intent(this, BuildingActivity.class);
                bldgIntent.putExtra("BUILDING_DETAILS", new String[]{
                        map_buildings[i].building_name,
                        map_buildings[i].building_address,
                        map_buildings[i].building_coordinates
                });

                //NIMMA: test this later with actual provider
                //Location testloc = ConvertStringToLatLng(map_buildings[i].building_coordinates);

                bldgIntent.putExtra("BUILDING_IMAGE_NAME", map_buildings[i].getBuilding_image_resource_name());
                bldgIntent.putExtra("LAST_KNOWN_COORDINATES", strCurrentUserLocation);
                bldgIntent.putExtra("BLDG_MAP_COORDINATES", map_buildings[i].building_coordinates);
                startActivity(bldgIntent);


            }
            //DEBUG:
            //PlotPin(this, x + intXAxisPlotOffset, y + intYAxisPlotOffset);
        }

    }

    private Location GetLocationFromStrings(String strMapLat, String strMapLong) {
        Location location = ConvertStringToLatLng(strMapLat + "," + strMapLong);
        return location;
    }

    public Location ConvertStringToLatLng(String strCoord) {
        String[] latlong = strCoord.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);

        Location location = new Location("dummyprovider"); //How do we make this an actual location provider?
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }

    //For later:
    public double GetCurrentPixelY(Location upperLeft, Location lowerRight, Location current) {
        double hypotenuse = upperLeft.distanceTo(current);
        double bearing = upperLeft.bearingTo(current);
        double currentDistanceY = Math.abs(Math.cos(bearing * Math.PI / OneEightyDeg)) * hypotenuse;
        //                           "percentage to mark the position"
        double totalHypotenuse = upperLeft.distanceTo(lowerRight);
        double totalDistanceY = totalHypotenuse * Math.abs(Math.cos(upperLeft.bearingTo(lowerRight)) * Math.PI / OneEightyDeg);
        double currentPixelY = currentDistanceY / totalDistanceY * ImageSizeH;

        return currentPixelY;
    }

    public double GetCurrentPixelX(Location upperLeft, Location lowerRight, Location current) {
        double hypotenuse = upperLeft.distanceTo(current);
        double bearing = upperLeft.bearingTo(current);
        double currentDistanceX = Math.sin(bearing * Math.PI / OneEightyDeg) * hypotenuse;
        //                           "percentage to mark the position"
        double totalHypotenuse = upperLeft.distanceTo(lowerRight);
        double totalDistanceX = totalHypotenuse * Math.sin(upperLeft.bearingTo(lowerRight) * Math.PI / OneEightyDeg);
        double currentPixelX = currentDistanceX / totalDistanceX * ImageSizeW;

        return currentPixelX;
    }

    private void PlotPin(Context context, float x, float y) {
        //Normalize incoming pixels
        x = x + intXAxisPlotOffset;
        y = y + intYAxisPlotOffset;

        RelativeLayout map_layout = (RelativeLayout) findViewById(R.id.activity_map);
        marker = new MarkerView(context);
        marker.set_x_y_coord(x, y);
        map_layout.addView(marker);
    }

    private void PlotCircle(Context context, float x, float y) {
        //Normalize incoming pixels
        //x = x + intXAxisPlotOffset;
        //y = y + intYAxisPlotOffset;
        //x =12; y = 380; //BASE - TOP LEFT
        //y = y + 360;

        //x = x + 25;
        //y = y + 444;

        RelativeLayout map_layout = (RelativeLayout) findViewById(R.id.activity_map);
        circlemarker = new CircleMarkerView(context);
        circlemarker.set_x_y_coord(x, y);
        map_layout.addView(circlemarker);
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

    private class MarkerView extends View {
        private float x_coord = 1000, y_coord = 1000;

        public MarkerView(Context context) {
            super(context);
        }

        public void set_x_y_coord(float x, float y) {
            x_coord = x;
            y_coord = y;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
            canvas.drawBitmap(marker, x_coord, y_coord, null);
        }
    }

    private class CircleMarkerView extends View {
        private float x_coord = 1000, y_coord = 1000;

        public CircleMarkerView(Context context) {
            super(context);
        }

        public void set_x_y_coord(float x, float y) {
            x_coord = x;
            y_coord = y;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.current_small);
            canvas.drawBitmap(marker, x_coord, y_coord, null);
        }
    }



    //Start user current location
    @TargetApi(Build.VERSION_CODES.M)
    public void GetCurrentLocation(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Location location;
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isNetworkEnabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                strCurrentUserLatitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
                strCurrentUserLongitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
                strCurrentUserLocation = strCurrentUserLatitude + "," + strCurrentUserLongitude;
                locCurrentLocation = location;
                updateCurrentUserLocationOnMap();
                return;
            }
        }
        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST_CODE); //BUG: fix for all versions of android
    }

    //Let's handle user's location now
    //First off define a listener for location changed events
    public final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            strCurrentUserLatitude += location.getLatitude();
            strCurrentUserLongitude += location.getLongitude();

            //String location_string = "geo:37.7749,-122.4194";
            String location_string = "geo:" + strCurrentUserLatitude + "," + strCurrentUserLongitude;
            Log.d("MainActivity", location_string);

            //Update current user location and strings
            strCurrentUserLocation = strCurrentUserLatitude + "," + strCurrentUserLongitude;
            locCurrentLocation = location;

            //Call after current location is known
            updateCurrentUserLocationOnMap();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    //Now we need to handle it after getting appropriate permissions:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, mLocationListener);
                //mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private View.OnTouchListener map_touch_listener =
            new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float screenX = event.getX();
                float screenY = event.getY();
                float viewX = screenX - v.getLeft();
                float viewY = screenY - v.getTop();

                Log.d("MapActivity", "X: " + viewX + " Y: " + viewY + " ScreenX: " + screenX + " ScreenY:" + screenY);

                //NIMMA: try plotting
                //plotPin(v.getContext(), viewX, screenY);
                ProcessTouchCoordinate(v, viewX, viewY);
                return true;
            }
            return false;
        }
    };

}
