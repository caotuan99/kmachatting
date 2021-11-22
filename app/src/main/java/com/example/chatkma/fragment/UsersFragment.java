package com.example.chatkma.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.GroupCreateActivity;
import com.example.chatkma.MainActivity;
import com.example.chatkma.R;
import com.example.chatkma.adapters.AdapterUsers;
import com.example.chatkma.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    androidx.appcompat.widget.SearchView SearchView;
    //firebase auth
    FirebaseAuth firebaseAuth;

    public UsersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.user_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        firebaseAuth = FirebaseAuth.getInstance();

        //init user list
        usersList = new ArrayList<>();
        // get all users
        getAllUsers();
        return view;
    }

    private void getAllUsers() {
        // get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // lay infor tu database "Users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        usersList.add(modelUsers);
                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    //set adapter to recycerview
                    recyclerView.setAdapter(adapterUsers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String query) {
        // get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // lay infor tu database "Users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUser = ds.getValue(ModelUsers.class);
                    //get tat ca nguoi dung tru tai khoan nguoi dung dang dang nhap
                    if (!modelUser.getUid().equals(fUser.getUid())){

                        if (modelUser.getHoten()!= null && modelUser.getHoten().contains(query.toLowerCase()) ||
                                modelUser.getEmail()!= null && modelUser.getEmail().contains(query.toLowerCase())
                               ||modelUser.getMaSv()!= null && modelUser.getMaSv().contains(query.toLowerCase()))
                        {
                            usersList.add(modelUser);

                        }

                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();

                    //set adapter to recycerview
                    recyclerView.setAdapter(adapterUsers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        //check admin to show create Group
        String uidAdmin = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String uidPdt ="ljJm82KDT3fVdPFqwzQRWq7EfTC2";
        if (!uidAdmin.equals(uidPdt)){
            menu.findItem(R.id.action_create_group).setVisible(false);
        }
//      action search
        MenuItem item = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);

                }
                else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);

                }
                else {
                    getAllUsers();
                }

                return false;
            }
        });


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