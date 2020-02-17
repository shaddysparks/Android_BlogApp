package com.example.yohan.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class Register extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button btnCraeteAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private Toolbar regToolbar;

    private ProgressDialog mregProgressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDisplayName = findViewById(R.id.reg_displayName);
        mEmail = findViewById(R.id.reg_Email);
        mPassword = findViewById(R.id.reg_Password);
        btnCraeteAccount = findViewById(R.id.btnCreate);
        regToolbar = (Toolbar)findViewById(R.id.register_app_bar);
        setSupportActionBar(regToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Account");

        regToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Register.this,Login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

            }
        });

        mregProgressdialog = new ProgressDialog(this);



        mAuth = FirebaseAuth.getInstance();


        btnCraeteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    mregProgressdialog.setTitle("Registering User");
                    mregProgressdialog.setMessage("Please Wait while we create Your Account");
                    mregProgressdialog.setCanceledOnTouchOutside(false);
                    mregProgressdialog.show();
                    registerUser(display_name,email,password);
                }else {
                    Toast.makeText(Register.this,"Please Enter Your Details In every Forms",Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    private void registerUser(final String display_name, final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                                    mregProgressdialog.dismiss();
                                    Intent i = new Intent(Register.this,MainActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();



                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            mregProgressdialog.hide();
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //  updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Register.this, "Authentication failed.: "+e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Register.this,Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}
