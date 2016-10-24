package com.nirupama.prasad.mapmysjsu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BuildingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        //Set up building toolbar
        Toolbar map_toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(map_toolbar);
        getSupportActionBar().setSubtitle("BUILDING DETAILS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up status bar
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorToolBar));

        //Retrieve building data
        String[] building_details = getIntent().getStringArrayExtra("BUILDING_DETAILS");


        //Set up basic info about the building
        TextView building_info_textview = (TextView) findViewById(R.id.activity_building_textView);
        String str_building_info_joined_string = "";
        for(String s: building_details) {
            str_building_info_joined_string = str_building_info_joined_string + "\n\n\n\n" + s ;
        }
        building_info_textview.setText(str_building_info_joined_string);


    }


}
