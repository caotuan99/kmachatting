package com.example.chatkma;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.adapters.AdapterParticipantAdd;
import com.example.chatkma.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {

    //init view
    private RecyclerView usersRv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupID;
    private String myGroupRole;
    private ArrayList<ModelUsers> userList;
    private AdapterParticipantAdd adapterParticipantAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Thêm thành viên");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth= FirebaseAuth.getInstance();

        usersRv= findViewById(R.id.usersRv);
        groupID=getIntent().getStringExtra("groupID");
        loadGroupInfo();
        getAllUser();
    }

    private void searchUsers(String query) {
        // get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // lay infor tu database "Users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUser = ds.getValue(ModelUsers.class);
                    //get tat ca nguoi dung tru tai khoan nguoi dung dang dang nhap
                    if (!modelUser.getUid().equals(fUser.getUid())){

                        if (modelUser.getHoten()!= null && modelUser.getHoten().contains(query.toLowerCase()) ||
                                modelUser.getEmail()!= null && modelUser.getEmail().contains(query.toLowerCase())
                                ||modelUser.getMaSv()!= null && modelUser.getMaSv().contains(query.toLowerCase()))
                        {
                            userList.add(modelUser);

                        }

                    }
                    //adapter
                    adapterParticipantAdd=new AdapterParticipantAdd(GroupParticipantAddActivity.this,userList,""+groupID,""+myGroupRole);
                    //refresh adapter
                    adapterParticipantAdd.notifyDataSetChanged();

                    //set adapter to recycerview
                    usersRv.setAdapter(adapterParticipantAdd);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAllUser() {
        //init list
        userList=new ArrayList<>();
        //load users from firebase
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    //get all users accept currently signed in
                    if (!firebaseAuth.getUid().equals(modelUsers.getUid())){
                        //not my uid
                        userList.add(modelUsers);
                    }
                }
                //setup adapter
                adapterParticipantAdd=new AdapterParticipantAdd(GroupParticipantAddActivity.this,userList,""+groupID,""+myGroupRole);
                //set adapter to recyclerview
                usersRv.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupID").equalTo(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String groupID=""+ds.child("groupID").getValue();
                    String groupTitle=""+ds.child("groupTitle").getValue();
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String createBy=""+ds.child("createBy").getValue();
                    String timestamp=""+ds.child("timestamp").getValue();

                    actionBar.setTitle("Thêm thành viên");
                    ref1.child(groupID).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        myGroupRole=""+dataSnapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle+"("+myGroupRole+")");

                                        getAllUser();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);

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
                    getAllUser();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);

                }
                else {
                    getAllUser();
                }

                return false;
            }
        });
        return true;

    }
}