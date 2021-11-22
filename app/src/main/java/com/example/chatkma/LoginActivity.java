package com.example.chatkma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    //Views
    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccTv, mRecoverPassTV;
    Button mLoginBtn;
    //  khai bao firebase
    private FirebaseAuth mAuth;

    // progess dialog
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Action Bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Đăng nhập");
        // enable back return
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //firebase onCreate()
        mAuth = FirebaseAuth.getInstance();

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mLoginBtn = findViewById(R.id.login_btn);
        notHaveAccTv = findViewById(R.id.notHaveAccTv);
        mRecoverPassTV = findViewById(R.id.RecoverPassTv);

        // Xu ly Login button On Click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nhập dữ liệu
                String email = mEmailEt.getText().toString();
                String passw=mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    // email không hợp lệ, báo lỗi
                    mEmailEt.setError("Email không hợp lệ");
                    mEmailEt.setFocusable(true);
                }
                else {
                    //Email hợp lệ
                    loginUser(email, passw);

                }

            }
        });

        // Xu ly khong co Account textView

        notHaveAccTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, DangKyActivity.class));
                finish();
            }
        });
        //quên mật khẩu Textview
        mRecoverPassTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPassWordDialog();
            }
        });

        // init progess dialog
        pd = new ProgressDialog(this);


    }

    private void showRecoverPassWordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lấy lại mật khẩu");
        // set Linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        //view to set in dialog
        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);


        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        // button lấy mật khẩu
        builder.setPositiveButton("Lấy mật khẩu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input Email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);

            }
        });
        // button Cancel
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dimiss dialog
                dialog.dismiss();

            }
        });

        //show dialog
        builder.create().show();


    }

    private void beginRecovery(String email) {
        // show progess dialog
        pd.setMessage("Đang gửi Email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Đã gửi Email", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Thất bại!", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //show proper lỗi mess
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loginUser(String email, String passw) {
        // show progess dialog
        pd.setMessage("Đang đăng nhập");
        pd.show();
        mAuth.signInWithEmailAndPassword(email,passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dimiss dialog
                            pd.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //user đăng nhập, chuyển sang LoginActibity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            //dimiss dialog
                            pd.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dimiss dialog
                pd.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // tới hoạt đông trước
        return super.onSupportNavigateUp();
    }
}