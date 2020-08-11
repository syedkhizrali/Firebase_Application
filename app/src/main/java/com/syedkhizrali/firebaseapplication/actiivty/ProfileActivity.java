package com.syedkhizrali.firebaseapplication.actiivty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telecom.CallAudioState;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.syedkhizrali.firebaseapplication.R;

import java.io.IOException;
import java.util.BitSet;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_name;
    private Button btn_save;
    private ImageView iv_selectImage;
    private int REQUEST_CODE = 201;
    private Uri uri;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //method calls
        init();
        loadUser();
        iv_selectImage.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        //method calls

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            finish();
        }
    }

    private void loadUser() {
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            if(user.getPhotoUrl() != null){


                Glide.with(ProfileActivity.this)
                        .load(user.getPhotoUrl().toString())
                        .into(iv_selectImage);
            }
            if(user.getDisplayName() != null){
                String name = user.getDisplayName();
                et_name.setText(name);
            }
        }


    }

    private void init() {
        btn_save = findViewById(R.id.btn_save);
        iv_selectImage = findViewById(R.id.iv_selectImage);
        et_name = findViewById(R.id.et_name);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_selectImage:
                selectImage();
                break;
            case R.id.btn_save:
                saveUser();
                break;
        }
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){ 
           uri = data.getData();

           try {
               Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
               iv_selectImage.setImageBitmap(bitmap);
               uploadImageToFirebase();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }

    private void uploadImageToFirebase() {
        StorageReference reference =
                FirebaseStorage.
                        getInstance().
                        getReference("profilephotos/"+System.currentTimeMillis()+".jpg");

        if(uri != null){
            progressBar.setVisibility(View.VISIBLE);
            reference.putFile(uri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    imageUri = taskSnapshot.getUploadSessionUri().toString();
                }

            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveUser(){
        String name = et_name.getText().toString();

        if(name.isEmpty()){
            et_name.setError("Name Required!");
            et_name.requestFocus();
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && imageUri != null){
            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(Uri.parse(imageUri))
                    .build();
            user.updateProfile(request)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, "Updated!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}