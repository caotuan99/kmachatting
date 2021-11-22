package com.example.chatkma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
//    views
    Button mRegisterBtn, mLoginBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init views
        mRegisterBtn = findViewById(R.id.register_btn);
        mLoginBtn = findViewById(R.id.login_btn);
        //xu ly Register button
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bắt đầu DangKyActivity
                startActivity(new Intent(MainActivity.this, DangKyActivity.class));
            }
        });
        // xử lý login Btn Click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bắt đầu LoginActivity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
/*Follow
1.Add Internet permission
2.Tạo Register và Login
3.Tạo RegisterActivity
4.Tạo Filebase project và kết nối
        */