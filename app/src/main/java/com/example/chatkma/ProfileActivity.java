package com.example.chatkma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.adapters.AdapterPosts;
import com.example.chatkma.models.ModelPost;
import com.example.chatkma.models.ModelUsers;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private ModelUsers users;
    private ImageView avatarIv;
    private TextView nameTv,emailTv,maSvTv;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    private RecyclerView rcv_post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();
        users= (ModelUsers) bundle.getSerializable("User");
        avatarIv=findViewById(R.id.avatarIv);
        nameTv=findViewById(R.id.nameTv);
        emailTv=findViewById(R.id.emailTv);
        maSvTv=findViewById(R.id.maSvTv);
        if(users.getImage()!=null&& !users.getImage().equals("")) Picasso.get().load(users.getImage()).into(avatarIv);
        nameTv.setText(users.getHoten());
        emailTv.setText(users.getEmail());
        maSvTv.setText(users.getMaSv());
        rcv_post=findViewById(R.id.rcv_post);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // hien thi post moi nhat, den cac post cu
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recyclerView
        rcv_post.setLayoutManager(layoutManager);
        postList=new ArrayList<>();
        loadPost();



    }

    private void loadPost() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        postList.clear();
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelPost post=snapshot.getValue(ModelPost.class);
                if(post.getUid().equals(users.getUid()))
                    postList.add(post);
                adapterPosts= new AdapterPosts(ProfileActivity.this, postList);
                //setAdapter to recyclerview
                rcv_post.setAdapter(adapterPosts);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}