package com.example.chatkma;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.adapters.AdapterParticipantAdd;
import com.example.chatkma.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupID;
    private String myGroupRole="";
    private FirebaseAuth firebaseAuth;
    private ActionBar actionBar;

    //ui views
    private ImageView groupIconIv;
    private TextView descriptionTv,createdByTv,editGroupTv,addParticipantTv,leaveGroupTv,participantsTv;
    private RecyclerView participantsRv;

    private ArrayList<ModelUsers> userList;
    private AdapterParticipantAdd adapterParticipantAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconIv=findViewById(R.id.groupIconIv);
        descriptionTv=findViewById(R.id.descriptionTv);
        createdByTv=findViewById(R.id.createdByTv);
        editGroupTv=findViewById(R.id.editGroupTv);
        addParticipantTv=findViewById(R.id.addParticipantTv);
        leaveGroupTv=findViewById(R.id.leaveGroupTv);
        participantsTv=findViewById(R.id.participantsTv);
        participantsRv=findViewById(R.id.participantsRv);

        groupID=getIntent().getStringExtra("groupID");

        firebaseAuth=FirebaseAuth.getInstance();

        loadGroupInfo();
        loadMyGroupRole();
        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupParticipantAddActivity.class);
                intent.putExtra("groupID",groupID);
                startActivity(intent);
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupID").equalTo(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String groupID=""+ds.child("groupID").getValue();
                    String groupTitle=""+ds.child("groupTitle").getValue();
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String createdBy=""+ds.child("createdBy").getValue();
                    String timestamp=""+ds.child("timestamp").getValue();

                    // chuyen timestamp qua dinh dang dd/mm/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    try {
                        calendar.setTimeInMillis(Long.parseLong(timestamp));
                    } catch (Exception e){
                        e.getStackTrace();
                    }
                    String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    loadCreatorInfo(dateTime,createdBy);

                    //set group info
                    actionBar.setTitle(groupTitle);
                    descriptionTv.setText(groupDescription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group).into(groupIconIv);

                    }
                    catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.ic_group);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCreatorInfo(String dateTime, String createdBy) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name=""+ds.child("Hoten").getValue();
                    createdByTv.setText("Tạo bởi "+name+" từ "+dateTime );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadMyGroupRole(){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            myGroupRole=""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail()+"("+myGroupRole+")");

                            if (myGroupRole.equals("participant")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Rời nhóm");
                            }
                            else if (myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Rời nhóm");
                            }
                            else if (myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Xóa nhóm");
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
    private void loadParticipants(){
        userList=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //get info tu Groups->Participants
                    String uid=""+ds.child("uid").getValue();
                    //get info cua user bang uid
                    DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                                userList.add(modelUsers);
                            }
                            //adapter
                            adapterParticipantAdd=new AdapterParticipantAdd(GroupInfoActivity.this,userList,groupID,myGroupRole);
                            //set adapter
                            participantsRv.setAdapter(adapterParticipantAdd);
                            participantsTv.setText("Số thành viên ("+userList.size()+")");
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
}