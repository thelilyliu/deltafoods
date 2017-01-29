package com.example.erickamada.delta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class deltaMessage extends AppCompatActivity {
    private Button backButton;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_delta_message);

        //backButton = (Button) findViewById(R.id.back_Button);

        String calories="Calories: \t\t"+MainActivity.cal+"\t\t"+MainActivity.reqcal;
        String fat="\nFat:\t\t"+MainActivity.fat+"\t\t"+MainActivity.reqfat;
        String cholesterol="\nCholesterol: \t\t"+MainActivity.chol+"\t\t"+MainActivity.reqchol;
        String sodium="\nSodium: \t\t"+MainActivity.sodium+"\t\t"+MainActivity.reqsodium;
        String carbohydrates="\nCarbohydrates: \t\t"+MainActivity.carb+"\t\t"+MainActivity.reqcarb;
        String protein="\nProtein: \t\t"+MainActivity.pro+"\t\t"+MainActivity.reqpro;

        String info=calories+fat+cholesterol+sodium+carbohydrates+protein;
        Intent intent = getIntent();
        String message = intent.getStringExtra("DATA");
        TextView textView = new TextView(this);
        textView.setTextSize(20);
        textView.setText(info);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_delta_message);
        layout.addView(textView);


    }
}

