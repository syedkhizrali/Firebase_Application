package com.syedkhizrali.firebaseapplication.actiivty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.syedkhizrali.firebaseapplication.R;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tv_signin;
    private EditText et_email, et_password;
    private Button btn_register;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //method calls
        init();
        btn_register.setOnClickListener(this);
        tv_signin.setOnClickListener(this);
        //method calls
        //firebase
        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);*/
        //firebase
    }

    private void init() {
        tv_signin = findViewById(R.id.tv_signin);
        btn_register = findViewById(R.id.btn_register);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_register:
                registerUser();
                break;
            case R.id.tv_signup:
                startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                finish();
                break;
        }

    }

    private void registerUser(){
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        if(email.isEmpty()){
            et_email.setError("Email is required!");
            et_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("Invalid email address!");
            et_email.requestFocus();
            return;
        }
        if(password.isEmpty()){
            et_password.setError("Password cannot be empty!");
            et_password.requestFocus();
            return;
        }
        if(password.length()<6){
            et_password.setError("Password must be of atleast 6 characters!");
            et_password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);


        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            startActivity(new Intent(SignUpActivity.this,ProfileActivity.class));
                            finish();
                        }
                        else{
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(SignUpActivity.this, "User Already Registred", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(SignUpActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}