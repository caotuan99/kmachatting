package com.example.chatkma;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatkma.fragment.ChatListFragment;
import com.example.chatkma.fragment.GroupChatsFragment;
import com.example.chatkma.fragment.HomeFragment;
import com.example.chatkma.fragment.ProfileFragment;
import com.example.chatkma.fragment.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {
    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;
    private BottomNavigationView mNavigationView;
//    private ViewPager2 mViewPager2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //ActionBar and title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Home");
//        mViewPager2 = findViewById(R.id.viewpager);
        mNavigationView = findViewById(R.id.navigation);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1,"");
        ft1.commit();

        mNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        actionBar.setTitle("Trang chủ");
                        HomeFragment fragment1 = new HomeFragment();
                        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                        ft1.replace(R.id.content, fragment1,"");
                        ft1.commit();
                        break;
                    case R.id.nav_profile:
                        actionBar.setTitle("Profile");
                        ProfileFragment fragment2 = new ProfileFragment();
                        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                        ft2.replace(R.id.content, fragment2,"");
                        ft2.commit();
                        break;
                    case R.id.nav_users:
                        actionBar.setTitle("Người dùng");
                        UsersFragment fragment3 = new UsersFragment();
                        FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                        ft3.replace(R.id.content, fragment3,"");
                        ft3.commit();
                        break;
                    case R.id.nav_chat:
                        actionBar.setTitle("Chats");
                        ChatListFragment fragment4 = new ChatListFragment();
                        FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                        ft4.replace(R.id.content, fragment4,"");
                        ft4.commit();
                        break;
                    case R.id.nav_group:
                        actionBar.setTitle("Nhóm Chat");
                        GroupChatsFragment fragment5 = new GroupChatsFragment();
                        FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                        ft5.replace(R.id.content, fragment5,"");
                        ft5.commit();
                        break;

                }

                return true;
            }
        });

    }




    private void checkUserStatus() {
        // get user hiện hành
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user !=null){
            //user đã đăng nhập
            //set email đã đăng nhập in user
//            mProfileTV.setText(user.getEmail());
        }
        else {
            //user chưa đăng nhập, chuyển tới mainActivity\
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start của ứng dụng
        checkUserStatus();
        super.onStart();
    }

}