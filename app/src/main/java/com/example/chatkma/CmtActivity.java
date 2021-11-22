package com.example.chatkma;


import android.Manifest;
import android.annotation.SuppressLint;
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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.Interface.IClickCmt;
import com.example.chatkma.adapters.CommentAdapter;
import com.example.chatkma.models.Comment;
import com.example.chatkma.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CmtActivity extends AppCompatActivity {
    private ModelPost post;
    private  ImageView uPictureIv, pImageIV,imglike,imgCmt;
    private TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikeTv,tvlike;
    private ImageButton btnSend,imageCmt;
    private LinearLayout likeBtn, commentBtn, shareBtn;
    private EditText edtCmt;
    private RecyclerView rcv_cmt;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private LinearLayout layout_showImg;
    FirebaseUser user;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PIC_CAMERA_CODE = 400;
    // mảng Permission requested
    String cameraPermissions[];
    String storagePermissions[];

    //user info
    String name, email, uid, dp;

    //uri chon anh
    Uri image_uri = null;

    //progress bar
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmt);
        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();
        post= (ModelPost) bundle.getSerializable("Post");
        uPictureIv=findViewById(R.id.uPictureIv);
        pImageIV=findViewById(R.id.pImageIv);
        btnSend=findViewById(R.id.btnSend);
        edtCmt=findViewById(R.id.edtCmt);
        tvlike=findViewById(R.id.tvlike);
        imglike=findViewById(R.id.imglike);
        layout_showImg=findViewById(R.id.layout_showImg);
        uNameTv=findViewById(R.id.uNameTv);
        pTimeTv=findViewById(R.id.pTimeTv);
        imageCmt=findViewById(R.id.imageCmt);
        imgCmt=findViewById(R.id.imgCmt);
        pd = new ProgressDialog(this);
        pTitleTv=findViewById(R.id.pTitleTv);
        pDescriptionTv=findViewById(R.id.pDescriptionTv);
        pLikeTv=findViewById(R.id.pLikeTv);
        likeBtn=findViewById(R.id.likebtn);
        commentBtn=findViewById(R.id.commentbtn);
        rcv_cmt=findViewById(R.id.rcv_cmt);
        user= FirebaseAuth.getInstance().getCurrentUser();
        String uid= post.getUid();
        String uEmail= post.getuEmail();
        String uName= post.getuName();
        String uDp= post.getuDp();
        String pId= post.getpId();
        String pTitle= post.getpTitle();
        String pDescription= post.getpDescr();
        String pImage= post.getpImage();
        String pTimeStamp= post.getpTime();
        // chuyen timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        } catch (Exception e){
            e.getStackTrace();
        }
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        uNameTv.setText(uName);
        pTimeTv.setText(pTime);
        pTitleTv.setText(pTitle);
        pDescriptionTv.setText(pDescription);
        setLike(post);
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_post).into(uPictureIv);
        }
        catch (Exception e){

        }

        //set post image
        // neu k co anh thi an ImageView di
        if (pImage.equals("noImage")){
            //hide imageView
            pImageIV.setVisibility(View.GONE);

        }
        else {
            try {
                Picasso.get().load(pImage).into(pImageIV);
            }
            catch (Exception e){

            }
        }
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike");
        reference.addChildEventListener(new ChildEventListener() {
            int like=0;
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.getValue()!=null) {
                    like =like+1;
                    pLikeTv.setText(String.valueOf(like)+" Lượt thích");

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                like =like-1;
                pLikeTv.setText(String.valueOf(like)+" Lượt thích");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if(tvlike.getText().equals("Thích")){
                    tvlike.setText("Đã thích");
                    tvlike.setTextColor(R.color.xanhsang);
                    imglike.setImageResource(R.drawable.icon_like_blue);
                    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike").child(user.getUid());
                    reference.setValue("Like");




                }else {
                    tvlike.setText("Thích");
                    imglike.setImageResource(R.drawable.icon_like);
                    tvlike.setTextColor(R.color.colorBlack);
                    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike").child(user.getUid());
                    reference.removeValue();

                }

            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(edtCmt.getText().toString())&& image_uri==null) {
                    Toast.makeText(CmtActivity.this,"Không để trống nội dung",Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if(image_uri==null)
                    sendComment(edtCmt.getText().toString());
                    else sendCommentWithImage(edtCmt.getText().toString(),image_uri);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcv_cmt.setLayoutManager(layoutManager);
        commentList=new ArrayList<>();
        commentAdapter= new CommentAdapter(commentList, this, new IClickCmt() {
            @Override
            public void onClickUser(Comment comment,int i) {

                if(comment.getImage()!=null){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference("Comments").child(user.getUid()+comment.getTime());
                storageReference.delete();
                }
                DatabaseReference reference1= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("Comments");
                reference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                            Comment comment1= dataSnapshot.getValue(Comment.class);
                            if(comment1.getUid().equals(comment.getUid())&&comment1.getTime().equals(comment.getTime())){
                                dataSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        commentAdapter.remove(i);
                                        Toast.makeText(CmtActivity.this,"Đã xóa",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
        rcv_cmt.setAdapter(commentAdapter);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("Comments");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment comment= snapshot.getValue(Comment.class);
                if(comment!=null) commentList.add(comment);
                commentAdapter.notifyDataSetChanged();
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
        imageCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showImagePickDialog();

            }
        });

    }

    private void sendCommentWithImage(String noidung, Uri uri) {
        pd.setMessage("Đang bình luận...");
        pd.show();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        StorageReference storageReference= FirebaseStorage.getInstance().getReference("Comments").child(user.getUid()+timeStamp);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()){
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("Comments");
                    HashMap<Object,String> hashMap=new HashMap<>();
                    hashMap.put("time",timeStamp);
                    hashMap.put("noidung",noidung);
                    hashMap.put("image",downloadUri);
                    hashMap.put("uid",user.getUid());
                    reference.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            edtCmt.setText("");
                            image_uri=null;
                            layout_showImg.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(CmtActivity.this, "Bình luật không thành công", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
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
                layout_showImg.setVisibility(View.VISIBLE);
                imgCmt.setImageURI(image_uri);
            }
            if (requestCode == IMAGE_PIC_CAMERA_CODE){
                //anh chon tu anh cup camera, get uri cho anh
                layout_showImg.setVisibility(View.VISIBLE);
                imgCmt.setImageURI(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private void sendComment(String noidung) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("Comments");
        HashMap<Object,String> hashMap=new HashMap<>();
        String timestamp=String.valueOf(System.currentTimeMillis());
        hashMap.put("time",timestamp);
        hashMap.put("noidung",noidung);
        hashMap.put("image",null);
        hashMap.put("uid",user.getUid());
        reference.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                edtCmt.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CmtActivity.this, "Bình luật không thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLike(ModelPost post ) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike").child(user.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null){
                    tvlike.setText("Thích");
                    imglike.setImageResource(R.drawable.icon_like);
                }
                else {
                    tvlike.setText("Đã thích");
                    imglike.setImageResource(R.drawable.icon_like_blue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}