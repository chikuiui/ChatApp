package com.example.chatapp_2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp_2.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isSignUp = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // if User is already logged in

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this,FriendsActivity.class));
            finish(); // this is to prevent user from going back to login page aka MainActivity
        }

        binding.login.setOnClickListener(v ->{
            binding.login.setBackgroundResource(R.drawable.text_selected);
            binding.login.setTextColor(getResources().getColor(R.color.white_card));
            // we want LOGIN view above SIGNUP so we use elevation
            binding.login.setElevation(4);
            binding.signup.setElevation(0);

            binding.signup.setBackgroundResource(R.drawable.textnotselected);
            binding.signup.setTextColor(getResources().getColor(R.color.red));

            // when i click on LOGIN view i want to hide whole TextInputLayout of [confirm password(TextInputEditText)]
            binding.usernameLayout.setVisibility(View.GONE);

            // change the SUBMIT button text from SIGNUP to LOGIN
            binding.btnSubmit.setText(R.string.log_in);
            // when we LOGIN we need to show the forget password view
            binding.txtPasswordForgot.setVisibility(View.VISIBLE);
            isSignUp = false;

        });

        binding.signup.setOnClickListener(v -> {
            binding.signup.setBackgroundResource(R.drawable.textnotselected);
            binding.signup.setTextColor(getResources().getColor(R.color.white_card));
            // we want SIGNUP view above LOGIN so we use elevation
            binding.signup.setElevation(4);
            binding.login.setElevation(0);

            binding.login.setBackgroundResource(R.drawable.textnotselected);
            binding.login.setTextColor(getResources().getColor(R.color.red));

            // when i click on SIGNUP view i want to un-hide while TextInputLayout of [confirm password(TextInputEditText)]
            binding.usernameLayout.setVisibility(View.VISIBLE);

            // change the SUBMIT button text from LOGIN to SIGNUP
            binding.btnSubmit.setText(R.string.sign_up);
            // when we SIGNUP we don't need to show the the forget password view
            binding.txtPasswordForgot.setVisibility(View.GONE);
            isSignUp = true;
        });


        binding.btnSubmit.setOnClickListener( v -> {
            if(Objects.requireNonNull(binding.email.getText()).toString().isEmpty() || Objects.requireNonNull(binding.password.getText()).toString().isEmpty()){
                if(isSignUp && Objects.requireNonNull(binding.username.getText()).toString().isEmpty()){
                    Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if(isSignUp){
                handleSignUp();
            }else{
                handleLogIn();
            }
        });

        binding.googleLogin.setOnClickListener( v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build();
            // this is the lib for the below one -> implementation("com.google.android.gms:play-services-auth:20.7.0")
            GoogleSignInClient gsc = GoogleSignIn.getClient(this,gso);
            Intent intent = gsc.getSignInIntent();
            startActivityForResult(intent,2);
        });

        binding.startingOfApp.setOnClickListener(v -> binding.startingOfApp.setVisibility(View.GONE));
    }

    private void handleSignUp(){
       String Name = Objects.requireNonNull(binding.username.getText()).toString();
       String Email = Objects.requireNonNull(binding.email.getText()).toString();
       String pass = Objects.requireNonNull(binding.password.getText()).toString();

       FirebaseAuth.getInstance().createUserWithEmailAndPassword(Email,pass).addOnCompleteListener( task -> {
          if(task.isSuccessful()){
              FirebaseDatabase.getInstance().getReference("user")
                      .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                      .setValue(new User(Name,Email,""));
              startActivity(new Intent(MainActivity.this,FriendsActivity.class));
              Toast.makeText(this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();
          }else{
              Toast.makeText(this, Objects.requireNonNull(task.getException()).getLocalizedMessage(),Toast.LENGTH_SHORT).show();
          }
       });
    }
    private void handleLogIn(){
        String Email = Objects.requireNonNull(binding.email.getText()).toString();
        String pass = Objects.requireNonNull(binding.password.getText()).toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(Email,pass).addOnCompleteListener(task ->{
           if(task.isSuccessful()){
               startActivity(new Intent(MainActivity.this,FriendsActivity.class));
               Toast.makeText(this,"Log in Successfully",Toast.LENGTH_SHORT).show();
           }else{
               Toast.makeText(this, Objects.requireNonNull(task.getException()).getLocalizedMessage(),Toast.LENGTH_SHORT).show();
           }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null && FirebaseAuth.getInstance().getCurrentUser() !=null){
            String name = account.getDisplayName();
            String email = account.getEmail();
            Uri image = account.getPhotoUrl();
            assert image != null;
            FirebaseDatabase.getInstance().getReference("user")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new User(name,email,image.toString()));
            startActivity(new Intent(MainActivity.this,FriendsActivity.class));
            Toast.makeText(this,"Google Sign Up Successfully",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try{
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(this,task1 ->{
               if(task1.isSuccessful()){
                   String name = account.getDisplayName();
                   String email = account.getEmail();
                   String image = Objects.requireNonNull(account.getPhotoUrl()).toString();
                   FirebaseDatabase.getInstance().getReference("user")
                           .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                           .setValue(new User(name,email,image.toString()));
                   startActivity(new Intent(MainActivity.this,FriendsActivity.class));
                   Toast.makeText(this, "Log in Successfully", Toast.LENGTH_SHORT).show();
               }else{
                   Toast.makeText(this, Objects.requireNonNull(task1.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
               }
            });

        }catch (ApiException e){
            Toast.makeText(this, "Exception API in google signIn", Toast.LENGTH_SHORT).show();
        }
    }
}