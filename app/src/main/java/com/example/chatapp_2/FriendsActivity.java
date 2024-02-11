package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.chatapp_2.databinding.ActivityFriendsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class FriendsActivity extends AppCompatActivity {

    private ActivityFriendsBinding binding;
    UserAdapter.OnUserClickListener onUserClickListener;
    private ArrayList<User> users;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        users = new ArrayList<>();

        binding.swipe.setOnRefreshListener(() ->{
            getUsers();
            binding.swipe.setRefreshing(false);
        });

        onUserClickListener = position -> {
            // need name of roommate to create room ,need email to sent message
            String roommate_name = users.get(position).getName();
            String roommate_email = users.get(position).getEmail();
            String roommate_image = users.get(position).getProfilePhoto();
            startActivity(new Intent(FriendsActivity.this,MessageActivity.class)
                    .putExtra("roommate_name",roommate_name)
                    .putExtra("roommate_email",roommate_email)
                    .putExtra("roommate_image",roommate_image));
        };

        // initially we get current users without refreshing it.
        getUsers();
    }

    private void getUsers() {
        // remove old users so it doesn't overlap.
        users.clear();
        FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapShot : snapshot.getChildren()){
                    User c = dataSnapShot.getValue(User.class);
                    Log.e("USERS ","Current User Name -> "+ c.getName());
                    // to avoid seeing myself on screen.
                    if(c.getEmail().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()))continue;
                    users.add(c);
                }
                Log.e("USERS SIZE-> ","size -> "+users.size());
                userAdapter = new UserAdapter(FriendsActivity.this,users,onUserClickListener);
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
                binding.recyclerView.setAdapter(userAdapter);
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_profile){
            startActivity(new Intent(FriendsActivity.this, Profile.class));
        }
        return super.onOptionsItemSelected(item);
    }
}