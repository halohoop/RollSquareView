package com.halohoop.rollsquareview_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.halohoop.rollsquareview.widgets.RollSquareViewMain;

public class MainActivity extends AppCompatActivity {

    private RollSquareViewMain rollSquareView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rollSquareView1 = (RollSquareViewMain) findViewById(R.id.rollSquareView1);
    }

    public void startRoll(View view) {
    }
}
