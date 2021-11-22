package com.example.chatkma.fragment;

import static android.app.Activity.RESULT_OK;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.AddPostActivity;
import com.example.chatkma.MainActivity;
import com.example.chatkma.R;
import com.example.chatkma.adapters.AdapterPosts;
import com.example.chatkma.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {


//    firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String storagePath = "Users_Profile_Cover_Imgs/";
    // view từ xml
    ImageView avatarTv, coverTv;
    TextView nameTv, emailTv, phoneTv, maSvTv;
    FloatingActionButton fab;
    ProgressDialog pd;
    RecyclerView rcv_post;
    // Permission yêu cầu Camera
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PIC_CAMERA_CODE = 400;
    // mảng Permission requested
    String cameraPermissons[];
    String storagePermissions[];
    //uri chon anh
    Uri image_uri;
    // check avatar hoac anh bia
    String profileOrCoverPhoto;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

//        init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();


//        init Array of permissions
        cameraPermissons = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

//        init view
        avatarTv = view.findViewById(R.id.avatarIv);
        coverTv = view.findViewById(R.id.coverTv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        maSvTv = view.findViewById(R.id.maSvTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab= view.findViewById(R.id.fab);
        pd = new ProgressDialog(getActivity());
        rcv_post=view.findViewById(R.id.rcv_post);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // hien thi post moi nhat, den cac post cu
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recyclerView
        rcv_post.setLayoutManager(layoutManager);
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // kiem tra den khi nhan dc data
                for (DataSnapshot ds: snapshot.getChildren()){
                    // get data
                    String name = ""+ ds.child("Hoten").getValue();
                    String email = "Email: "+ ds.child("email").getValue();
                    String MaSv = "Mã: "+ ds.child("MaSv").getValue();
                    String phone = "Điện Thoại: "+ ds.child("Phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();
                    //set data

                    nameTv.setText(name);
                    emailTv.setText(email);
                    maSvTv.setText(MaSv);
                    phoneTv.setText(phone);
                    try {
//                        nếu ảnh đã đc đặt
                        Picasso.get().load(image).into(avatarTv);

                    }
                    catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face_macdinh).into(avatarTv);
                    }
                    try {
//                        nếu ảnh đã đc đặt
                        Picasso.get().load(cover).into(coverTv);

                    }
                    catch (Exception e) {
                    }

                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList=new ArrayList<>();
        loadPost();
        showHideWhenScroll();



        return view;
    }
    private void loadPost() {
        // path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        postList.clear();
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelPost post=snapshot.getValue(ModelPost.class);
                if(post.getUid().equals(user.getUid()))
                    postList.add(post);
                adapterPosts= new AdapterPosts(getActivity(), postList);
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
    private void showHideWhenScroll() {
        rcv_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //dy > 0: scroll up; dy < 0: scroll down
                if (dy > 0) fab.hide();
                else fab.show();
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
    private boolean checkStoragePermission(){
        // kiểm tra quyền truy cập bộ nhớ đã khả dụng hay chưa
        // return true nếu đã enabled
        // return false nếu chưa endbled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result;

    }
    private void requestStoragePermisson(){
        // yêu cầu quyền lưu trữ bộ nhớ đc chạy
        ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        // kiểm tra quyền truy cập bộ nhớ đã khả dụng hay chưa
        // return true nếu đã enabled
        // return false nếu chưa endbled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result && result1;

    }
    private void requestCameraPermisson(){
        // yêu cầu quyền Camera đc chạy
        ActivityCompat.requestPermissions(getActivity(), cameraPermissons, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Chỉnh sửa Avatar","Chỉnh sửa ảnh bìa ","Chỉnh sửa Tên","Chỉnh sửa SĐT"};
//        alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // set title
        builder.setTitle("Chọn hành động");
        // chọn item dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                xử lý thông báo item click
                if (which == 0) {
                    //Sửa avatar click
                    pd.setMessage("Updating Avatar");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if (which == 1) {
                    //sửa ảnh bìa click
                    pd.setMessage("Updating ảnh bìa");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if (which == 2){
                    //sửa tên click
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("Hoten");
                }
                else if (which == 3){
                    //sửa sđt click
                    pd.setMessage("Updating SĐT");
                    showNamePhoneUpdateDialog("Phone");
                }

            }
        });
//        tạo và show thông báo
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cập nhật "+key);
        //set layout dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Nhập "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        // them button in dialog
        builder.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Đã cập nhật", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(), "Vui lòng nhập"+key, Toast.LENGTH_SHORT).show();

                }

            }
        });
        // button Cancel
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();

    }

    private void showImagePicDialog() {
        // Chọn Chụp ảnh hoặc Bộ sưu tập
        String options[] = {"Chụp ảnh","Bộ sưu tập"};
//        alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                        requestStoragePermisson();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                        Toast.makeText(getActivity(), "Hãy cho phép truy cập Camera và bộ nhớ", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Hãy cho phép truy cập Bộ nhớ", Toast.LENGTH_SHORT).show();
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
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PIC_CAMERA_CODE){
                //anh chon tu anh cup camera, get uri cho anh

                uploadProfileCoverPhoto(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();
        String filePathAndName = storagePath+""+ profileOrCoverPhoto+""+user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // image dc upload len storage, chuyen uri vao user's database
                        Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        // kiem tra neu anh da dc upload hay chua va uri da nhan dc
                        if (uriTask.isSuccessful()){
                            //image uploaded
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto,downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //url da co trong database users
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Ảnh đã được update.", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // loi them url vao database
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Lỗi Update ảnh", Toast.LENGTH_SHORT).show();


                                        }
                                    });

                        }
                        else {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // co loi thi dua ra thong bao
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void pickFromCamera() {
        // chup anh tu camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        // put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

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