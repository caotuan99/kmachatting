package com.example.chatkma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DangKyActivity extends AppCompatActivity {
//    views
    EditText mEmailEt, mPasswordEt, mHotenEt, mMaSvEt;
    Button mRegisterBtn;
    TextView mHaveAccTv;
    RadioGroup mRadioGroupCheck;
    RadioButton mCheckSv, mCheckGv;
    String Type;




    //    tien trinh hien thi khi dang nhap User
    ProgressDialog progressDialog;
    ProgressDialog progressDialog2;
//  Khai bao firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        // Action Bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Tạo Tài Khoản");
        // enable back return
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        // init truy xuat toi Control email
        mEmailEt = findViewById(R.id.emailEt);
        // truy xuat password nhap o form
        mPasswordEt = findViewById(R.id.passwordEt);
        // truy xuat nut dang ky
        mRegisterBtn = findViewById(R.id.register_btn);
        // truy xuat Ho va ten
        mHotenEt = findViewById(R.id.HotenEt);
        //truy xuat toi Ma GV/SV
        mMaSvEt = findViewById(R.id.MaSvEt);

        //Radio check type GV/SV
        mRadioGroupCheck = (RadioGroup) findViewById(R.id.rdCheck);
        mCheckSv= (RadioButton) findViewById(R.id.rdSv);
        mCheckGv= (RadioButton) findViewById(R.id.rdGv);


        mRadioGroupCheck.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //trả id Radio Button
                switch (checkedId){
                    case R.id.rdGv:
                        Toast.makeText(DangKyActivity.this, "Bạn là Giảng viên", Toast.LENGTH_SHORT).show();
                        Type = "Giáo viên";
                        break;
                    case R.id.rdSv:
                        Toast.makeText(DangKyActivity.this, "Bạn là Sinh Viên", Toast.LENGTH_SHORT).show();
                        Type = "Sinh viên";
                        break;
                }

            }
        });


        // Gợi ý đăng nhập
        mHaveAccTv = findViewById(R.id.haveAccTv);
        //firebase onCreate()
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đăng ký tài khoản thành công.");
        progressDialog2 = new ProgressDialog(this);
        progressDialog2.setMessage("Đang đăng ký tài khoản");

        // nhận sự kiện khi check box thay đổi trạng thái



        // xu ly register on btn
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input Email, mật khẩu
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String Hoten = mHotenEt.getText().toString().trim();
                String MaSv = mMaSvEt.getText().toString().trim();


                //xac nhan
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //check lại email
                    mEmailEt.setError("Email không hợp lệ");
                    mEmailEt.setFocusable(true);

                }
                else if (password.length()<6){
                    //check lại Password
                    mPasswordEt.setError("Mật khẩu phải nhiều hơn 6 ký tự");
                    mPasswordEt.setFocusable(true);

                }
                else if (Hoten.isEmpty()){
                    mHotenEt.setError("Họ tên không được để trống");
                    mHotenEt.requestFocus();
                    return;

                }
                else if (MaSv.isEmpty()){
                    mMaSvEt.setError("Vui lòng nhập mã GV/SV");
                    mMaSvEt.requestFocus();
                    return;
                }
                else {
                    registerUser(email, password); //đăng ký tài khoản
//                    startActivity(new Intent(DangKyActivity.this, LoginActivity.class));


                }



            }
        });
        // xử lý Login textview onClick listener

        mHaveAccTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DangKyActivity.this, LoginActivity.class));
                finish();
            }
        });



    }

    private void registerUser(String email, String password) {
        //khi email và mật khẩu hợp lệ, hiện thị thanh progress và bắt đầu đăng ký
        progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, Bỏ qua thông báo và bắt đầu đăng ký
                                progressDialog.dismiss();
                                FirebaseUser user = mAuth.getCurrentUser();
                                //lấy thông tin từ user
                                String email = user.getEmail();
                                String uid = user.getUid();
                                // sử dụng HashMap
                                HashMap<Object, String> hashMap = new HashMap<>();
                                String Hoten = mHotenEt.getText().toString().trim();
                                String MaSv = mMaSvEt.getText().toString().trim();

                                //put thông tin vào hashmap
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("Hoten",Hoten);
                                hashMap.put("Phone", "");
                                hashMap.put("image","");
                                hashMap.put("onlineStatus","online");
                                hashMap.put("typingTo","noOne");
                                hashMap.put("cover","");
                                hashMap.put("MaSv",MaSv);
                                hashMap.put("Type",Type);
//                                // cài đặt firebase Data
//                                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                //Lưu date vs user "Users"
//                                DatabaseReference reference = database.getReference("Users");
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                //Đặt data vs Hashmap
                                ref.child(uid).setValue(hashMap);


                            } else {
                                // If sign in fails, display a message to the user.
                                progressDialog.dismiss();
                                Toast.makeText(DangKyActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(DangKyActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // tới hoạt đông trước
        return super.onSupportNavigateUp();
    }
}