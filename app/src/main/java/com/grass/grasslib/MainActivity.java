package com.grass.grasslib;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.grass.views.MyRemindEditText;

public class MainActivity extends AppCompatActivity {

    private MyRemindEditText mMyRemindEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyRemindEditText = findViewById(R.id.myremind);
        mMyRemindEditText.setOnRemindTextAppearListener(new MyRemindEditText.OnRemindTextAppearListener() {
            @Override
            public void remindTextAppear() {
                Toast.makeText(MainActivity.this,"@出现",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
