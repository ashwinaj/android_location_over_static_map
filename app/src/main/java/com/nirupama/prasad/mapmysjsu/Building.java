package com.nirupama.prasad.mapmysjsu;


public class Building {

    public String building_name;
    public String building_address;


    //Building pixel data set
    public static final int TOP_LEFT_X = 0;
    public static final int TOP_RIGHT_Y = 1;
    public static final int TOP_RIGHT_X = 2;
    public static final int BOTTOM_RIGHT_Y = 3;
    public float[] pixel_coordinates;

    Building(String name, float[] coordinates){

        building_name = name;
        pixel_coordinates = new float[4];
        pixel_coordinates[TOP_LEFT_X] = coordinates[TOP_LEFT_X];
        pixel_coordinates[TOP_RIGHT_Y] = coordinates[TOP_RIGHT_Y];
        pixel_coordinates[TOP_RIGHT_X] = coordinates[TOP_RIGHT_X] ;
        pixel_coordinates[BOTTOM_RIGHT_Y] = coordinates[BOTTOM_RIGHT_Y];

    }

    public String getBuilding_address() {
        return building_address;
    }

    public void setBuilding_address(String building_address) {
        this.building_address = building_address;
    }

    public float[] getPixel_coordinates() {
        return pixel_coordinates;
    }

    public void setPixel_coordinates(float[] pixel_coordinates) {
        this.pixel_coordinates = pixel_coordinates;
    }

    //Calculate if within bounds
    public boolean IsWithinPixelBounds(float x, float y){

        if (x > pixel_coordinates[TOP_LEFT_X] &&  x < pixel_coordinates[TOP_RIGHT_X]) {
            if(y > pixel_coordinates[TOP_RIGHT_Y] && y < pixel_coordinates[BOTTOM_RIGHT_Y]){
                return true;
            }
        }

        return false;
    }



}
