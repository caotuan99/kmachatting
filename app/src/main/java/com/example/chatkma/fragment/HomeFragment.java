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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.AddPostActivity;
import com.example.chatkma.MainActivity;
import com.example.chatkma.R;
import com.example.chatkma.adapters.AdapterPosts;
import com.example.chatkma.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    FirebaseUser user;


    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        // recyclerview va thuoc tinh
        recyclerView = view.findViewById(R.id.postRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // hien thi post moi nhat, den cac post cu
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recyclerView
        recyclerView.setLayoutManager(layoutManager);
        user=FirebaseAuth.getInstance().getCurrentUser();
        //init post list
        postList = new ArrayList<>();
        loadPost();
        return view;
    }

    private void loadPost() {
        // path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        postList.clear();
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelPost modelPost = snapshot.getValue(ModelPost.class);
                postList.add(modelPost);

                //adapter
                adapterPosts= new AdapterPosts(getActivity(), postList);
                //setAdapter to recyclerview
                recyclerView.setAdapter(adapterPosts);
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
        //get all data tu ref
    }
    private void searchPosts(String searchQuery){
        // path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data tu ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if (modelPost.getpTitle()!=null && modelPost.getpTitle().contains(searchQuery.toLowerCase())||
                            modelPost.getpDescr()!=null && modelPost.getpDescr().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }

//                    postList.add(modelPost);

                    //adapter
                    adapterPosts= new AdapterPosts(getActivity(), postList);
                    //setAdapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // truong hop loi
                Toast.makeText(getActivity(), ""+databaseError, Toast.LENGTH_SHORT).show();

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
        //inflating
        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Admin");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null) menu.findItem(R.id.action_add_post).setVisible(false);
                else menu.findItem(R.id.action_add_post).setVisible(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //search view de search post bye post Title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //call khi nguoi dung an vao search btn
                if (!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else {
                    loadPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //call khi nguoi dung an vao key search tren ban phim
                if (!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else {
                    loadPost();
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
        if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}