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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class GroupCreateActivity extends AppCompatActivity {

    // Permission yêu cầu Camera
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PIC_CAMERA_CODE = 400;
    // mảng Permission requested
    String cameraPermissions[];
    String storagePermissions[];

    //uri chon anh
    Uri image_uri = null;

    //actionbar
    private ActionBar actionBar;

    //firebase
    private FirebaseAuth firebaseAuth;

    //UI view
    private ImageView groupIconIv;
    private EditText groupTitleEt, groupDescriptionEt;
    private FloatingActionButton createGroupBtn;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        
//        actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
//        actionBar.setTitle("Tạo nhóm lớp học phần");

        //init UI views
        groupIconIv=findViewById(R.id.groupIconIv);
        groupTitleEt=findViewById(R.id.groupTitleEt);
        groupDescriptionEt=findViewById(R.id.groupDescriptionEt);
        createGroupBtn=findViewById(R.id.createGroupBtn);

        //        init Array of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();

            }
        });

        // xu ly click event
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateGroup();
            }
        });
    }

    private void startCreateGroup() {
        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Đang tạo nhóm lớp...");
        //input title, description
        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDescription= groupDescriptionEt.getText().toString().trim();
        //kiem tra
        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Vui lòng nhập tên nhóm lớp...", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        //timestamp: for groupIcon image, groupId, timeCreated,...
        String g_timestamp = ""+System.currentTimeMillis();
        if (image_uri==null){
            //create without image
            createGroup(
                    ""+g_timestamp,
                    ""+groupTitle,
                    ""+groupDescription,
                    "");
        }
        else {
            //create group with image
            //upload image
            //image name and path
            String fileNameAndPath ="Group_Imgs/"+"image"+g_timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image upload, get url
                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!p_uriTask.isSuccessful());
                            Uri p_downloadUri = p_uriTask.getResult();
                            if (p_uriTask.isSuccessful()){
                                createGroup(
                                        ""+g_timestamp,
                                        ""+groupTitle,
                                        ""+groupDescription,
                                        ""+p_downloadUri);

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //that bai
                            progressDialog.dismiss();
                            Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createGroup(String g_timestamp, String groupTitle, String groupDescription, String groupIcon) {
        HashMap<String, String> hashMap=new HashMap<>();
        hashMap.put("groupID",""+g_timestamp);
        hashMap.put("groupTitle",""+groupTitle);
        hashMap.put("groupDescription",""+groupDescription);
        hashMap.put("groupIcon",""+groupIcon);
        hashMap.put("timestamp",""+g_timestamp);
        hashMap.put("createdBy",""+firebaseAuth.getUid());

        //create group
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // create thanh cong
                        //setup member
                        HashMap<String, String> hashMap1=new HashMap<>();
                        hashMap1.put("uid",firebaseAuth.getUid());
                        hashMap1.put("role","creator");
                        hashMap1.put("timestamp", g_timestamp);

                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                        ref1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Participants added
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupCreateActivity.this, "Tạo nhóm thành công", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //that bai
                        progressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user !=null){
//            actionBar.setSubtitle(user.getEmail());
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                // anh chon tu bo suu tap, get uri cho anh
                image_uri = data.getData();
                groupIconIv.setImageURI(image_uri);
            }
            if (requestCode == IMAGE_PIC_CAMERA_CODE){
                //anh chon tu anh cup camera, get uri cho anh
                groupIconIv.setImageURI(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}