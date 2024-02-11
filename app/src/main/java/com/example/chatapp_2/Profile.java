package com.example.chatapp_2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp_2.databinding.ActivityFriendsBinding;
import com.example.chatapp_2.databinding.ActivityProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class Profile extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private Uri imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.txtUserName.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());

        // if we signup/login from google then put that photo.
        String profilePictureUrl = String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        Glide.with(this).load(profilePictureUrl).error(R.drawable.account_img)
                .placeholder(R.drawable.account_img).into(binding.profileImg);

        binding.btnLogout.setOnClickListener(v ->{
            FirebaseAuth.getInstance().signOut();
            // if use google sign in then sign out from google so next time we log in it will show us options.
            GoogleSignIn.getClient(Profile.this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

            // this below line start MainActivity and clear the task stack ensuring that MainActivity first and only present.
            startActivity(new Intent(Profile.this,MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        binding.profileImg.setOnClickListener( v -> {
            Intent photoIntent = new Intent(Intent.ACTION_PICK);
            photoIntent.setType("image/*");
            startActivityForResult(photoIntent,1);
        });

        binding.btnUploadImg.setOnClickListener( v -> {
            if(imgPath == null){
                Toast.makeText(this, "Image Not Selected ! Click on image ion to select image", Toast.LENGTH_SHORT).show();
            }else{
                uploadingImage();
            }
        });


    }

    private void uploadingImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        //  uploads an image file (imgPath) to Firebase Storage, generates a unique reference for the image,
        //  and then retrieves the download URL of the uploaded image
        FirebaseStorage.getInstance().getReference("images").child(UUID.randomUUID().toString())
                .putFile(imgPath).addOnCompleteListener( task -> {
                    if(task.isSuccessful()){
                        task.getResult().getStorage().getDownloadUrl().addOnCompleteListener( task1 -> {
                            if(task1.isSuccessful()){
                                updateProfilePicture(task.getResult().toString());
                            }
                        });
                        Toast.makeText(this, "Image Uploading", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(this, "Error Uploading", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }).addOnProgressListener( snapshot -> {
                   double progress = 100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount();
                   progressDialog.setMessage("Uploading " + (int)progress + "%");
                });
    }

    private void updateProfilePicture(String url) {
        FirebaseDatabase.getInstance().getReference("user")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("profilePicture").setValue(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            // means successful get the image from local storage.
            imgPath = data.getData();
            getImageInImageView();
        }else{
            Toast.makeText(this, "Request Code -> "+ requestCode, Toast.LENGTH_SHORT).show();
        }
    }

    private void getImageInImageView() {
        Bitmap bitmap = null;
        try {
            // now get this image in bitmap because its in string form.
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgPath);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        binding.profileImg.setImageBitmap(bitmap);
    }
}