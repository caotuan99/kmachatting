package com.example.chatkma;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.adapters.AdapterGroupChat;
import com.example.chatkma.models.ModelGroupChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String groupID, myGroupRole="";
    private Toolbar toolbar;
    private ImageView groupIconIv;
    private TextView groupTitleTv;
    private ImageButton attachBtn, sendBtn;
    private EditText messageEt;
    private RecyclerView chatRv;
    private ActionBar actionBar;

    private ArrayList<ModelGroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;

    // Permission yêu cầu Camera
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PIC_CAMERA_CODE = 400;
    // mảng Permission requested
    private String cameraPermissions[];
    private String storagePermissions[];

    //uri chon anh
    private Uri image_uri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        actionBar=getSupportActionBar();

//        actionBar.hide();


        //init view
        toolbar= findViewById(R.id.toolbar);
        groupIconIv= findViewById(R.id.groupIconIv);
        groupTitleTv= findViewById(R.id.groupTitleTv);
        attachBtn= findViewById(R.id.attachBtn);
        sendBtn= findViewById(R.id.sendBtn);
        messageEt= findViewById(R.id.messageEt);
        chatRv= findViewById(R.id.chatRv);

        setSupportActionBar(toolbar);

        //get id of group
        Intent intent = getIntent();
        groupID= intent.getStringExtra("groupID");

        //        init Array of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth= FirebaseAuth.getInstance();

        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String message= messageEt.getText().toString().trim();
                // kiem tra
                if (TextUtils.isEmpty(message)){
                    //tin nhan rong thi khong gui
                    Toast.makeText(GroupChatActivity.this, "Không thể gửi tin nhắn rỗng...", Toast.LENGTH_SHORT).show();
                }
                else {
                    //send tin nhan
                    sendMessage(message);
                }
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image from gallery or camera
                showImagePickDialog();
            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            myGroupRole=""+ds.child("role").getValue();
                            //refresh menu item
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadGroupMessages() {
        //init list
        groupChatList=new ArrayList<>();
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupChatList.clear();
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                            groupChatList.add(model);
                        }
                        //adapter
                        adapterGroupChat= new AdapterGroupChat(GroupChatActivity.this,groupChatList);
                        //set to recyclerview
                        chatRv.setAdapter(adapterGroupChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage(String message) {
        //timestamp
        String timestamp = ""+System.currentTimeMillis();
        //setup message data
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("sender",""+firebaseAuth.getUid());
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("type","text");//text/image/file

        //add in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //message sent
                        //clear messageEt
                        messageEt.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //message gui that bai
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void loadGroupInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupID").equalTo(groupID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String groupTitle=""+ds.child("groupTitle").getValue();
                            String groupDescription=""+ds.child("groupDescription").getValue();
                            String groupIcon=""+ds.child("groupIcon").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String createdBy=""+ds.child("createdBy").getValue();


                            groupTitleTv.setText(groupTitle);
                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_show_group).into(groupIconIv);
                            }
                            catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.ic_show_group);
                            }



                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void showImagePickDialog() {
        // Chọn Chụp ảnh hoặc Bộ sưu tập
        String options[] = {"Chụp ảnh","Bộ sưu tập"};
//        alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set title
        builder.setTitle("Chọn ảnh từ");
        // chọn item dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                xử lý thông báo item click
                if (which == 0) {
                    //Camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermisson();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1) {
                    //Bộ sưu tập clicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }

            }
        });
//        tạo và show thông báo
        builder.create().show();
    }
    private void pickFromCamera() {
        // chup anh tu camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        // put image uri
        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        // intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PIC_CAMERA_CODE);
    }
    private void pickFromGallery() {
        // pic from bo suu tap
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission() {
        // yêu cầu quyền lưu trữ bộ nhớ đc chạy
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        // kiểm tra quyền truy cập bộ nhớ đã khả dụng hay chưa
        // return true nếu đã enabled
        // return false nếu chưa endbled
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private boolean checkCameraPermission(){
        // kiểm tra quyền truy cập bộ nhớ đã khả dụng hay chưa
        // return true nếu đã enabled
        // return false nếu chưa endbled
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result && result1;

    }
    private void requestCameraPermisson(){
        // yêu cầu quyền Camera đc chạy
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        if (myGroupRole.equals("creator")||myGroupRole.equals("admin")){
            //la admin hoac creator thi hien add person option
            menu.findItem(R.id.action_add_participant).setVisible(true);
        }
        else {
            menu.findItem(R.id.action_add_participant).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_add_participant){
            Intent intent=new Intent(this,GroupParticipantAddActivity.class);
            intent.putExtra("groupID",groupID);
            startActivity(intent);
        }
        else if (id==R.id.action_group_info){
            Intent intent=new Intent(this,GroupInfoActivity.class);
            intent.putExtra("groupID",groupID);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        //permission kich hoat
                        pickFromCamera();
                    }
                    else {
                        // permission k dc kich hoat
                        Toast.makeText(this, "Hãy cho phép truy cập Camera và bộ nhớ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length >0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //permission kich hoat
                        pickFromGallery();
                    }
                    else {
                        // permission k dc kich hoat
                        Toast.makeText(this, "Hãy cho phép truy cập Bộ nhớ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }
    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK){
//            if (requestCode == IMAGE_PICK_GALLERY_CODE){
//                // anh chon tu bo suu tap, get uri cho anh
//                image_uri = data.getData();
//
//                //dung uri de upload lne firebase database
//                    sendImageMessage(image_uri);
//                try {
//                    sendImageMessage(image_uri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            if (requestCode == IMAGE_PIC_CAMERA_CODE){
//                //anh chon tu anh chup camera, get uri cho anh
//                sendImageMessage(image_uri);
//                try {
//                    sendImageMessage(image_uri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                // anh chon tu bo suu tap, get uri cho anh
                image_uri = data.getData();

                //dung uri de upload lne firebase database

                    sendImageMessage(image_uri);
            }
            if (requestCode == IMAGE_PIC_CAMERA_CODE){
                //anh chon tu anh chup camera, get uri cho anh
                    sendImageMessage(image_uri);



            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_uri) {
        //progress dialog
        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Vui lòng chờ...");
        pd.setMessage("Đang gửi ảnh...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        //file name and path in firebase storage
        String filenamePath ="ChatImage/" + ""+System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        //upload image
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful());
                            Uri p_downloadUri = p_uriTask.getResult();
//                            String p_downloadUri = p_uriTask.getResult().toString();
                            if (p_uriTask.isSuccessful()){
                                //timestamp
                                String timestamp = ""+System.currentTimeMillis();
                                //setup message data
                                HashMap<String,Object> hashMap= new HashMap<>();
                                hashMap.put("sender",""+firebaseAuth.getUid());
                                hashMap.put("message",""+p_downloadUri);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("type","image");//text/image/file

                                //add in db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                ref.child(groupID).child("Messages").child(timestamp)
                                        .setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //message sent
                                                //clear messageEt
                                                messageEt.setText("");
                                                pd.dismiss();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                //message gui that bai
                                                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
    }

}