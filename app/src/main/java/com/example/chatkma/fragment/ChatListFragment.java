package com.example.chatkma.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chatkma.GroupCreateActivity;
import com.example.chatkma.MainActivity;
import com.example.chatkma.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatListFragment extends Fragment {
    FirebaseAuth firebaseAuth;

    public ChatListFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        return view;
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
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();

        }

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);// hien thi menu option o flagment
        super.onCreate(savedInstanceState);
    }

    //Option menu
    @Override

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        //hide add post icon
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        //check admin dang bi loi
        String uidAdmin = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String uidPdt ="ljJm82KDT3fVdPFqwzQRWq7EfTC2";
        if (!uidAdmin.equals(uidPdt)){
            menu.findItem(R.id.action_create_group).setVisible(false);
        }


//        String emailAdmin= FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        String emailPdt="phongdaotao@edu.com";
//        if (!emailAdmin.equals(emailPdt)){
//            menu.findItem(R.id.action_create_group).setVisible(false);
//        }

        //check admin
        super.onCreateOptionsMenu(menu, inflater);
    }


    // xử lý menu

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item ID
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}