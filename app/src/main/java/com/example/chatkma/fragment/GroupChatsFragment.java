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
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.GroupCreateActivity;
import com.example.chatkma.MainActivity;
import com.example.chatkma.R;
import com.example.chatkma.adapters.AdapterGroupChatList;
import com.example.chatkma.models.ModelGroupChatList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupChatsFragment extends Fragment {

    private RecyclerView groupsRv;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelGroupChatList> groupChatLists;
    private AdapterGroupChatList adapterGroupChatList;

    public GroupChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_group_chats, container, false);

        groupsRv= view.findViewById(R.id.groupsRv);

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupChatList();
        return view;
    }

    private void loadGroupChatList() {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    // nếu uid người dùng hiện tại có trong danh sách người tham gia Participants thì hiển thị group ở Recyclerview
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChatList model = ds.getValue(ModelGroupChatList.class);
                        groupChatLists.add(model);
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void searchGroupChatList(final String query) {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    // nếu uid người dùng hiện tại có trong danh sách người tham gia Participants
                    // thì hiển thị group ở Recyclerview
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){

                        //SEARCH group vs title group trong search
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){

                            ModelGroupChatList model = ds.getValue(ModelGroupChatList.class);
                            groupChatLists.add(model);
                        }

                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                    searchGroupChatList(s);

                }
                else {
                    loadGroupChatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchGroupChatList(s);

                }
                else {
                    loadGroupChatList();
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

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}