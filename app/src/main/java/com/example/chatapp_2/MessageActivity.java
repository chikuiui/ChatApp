package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp_2.databinding.ActivityMessageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;

    String userNameofRoommate,emailOfRoommate,imageOfRoommate,chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userNameofRoommate = getIntent().getStringExtra("roommate_name");
        emailOfRoommate = getIntent().getStringExtra("roommate_email");
        imageOfRoommate = getIntent().getStringExtra("roommate_image");

        // name of person i am chatting with
        binding.chattingWith.setText(userNameofRoommate);
        messages = new ArrayList<>();
        Glide.with(this).load(imageOfRoommate).placeholder(R.drawable.account_img)
                .error(R.drawable.account_img).into(binding.imgToolbar);

        binding.send.setOnClickListener((view) ->{
            if(binding.editText.getText().toString().isEmpty()){
                Toast.makeText(this, "No message Found!", Toast.LENGTH_SHORT).show();
                return;
            }
            Message currMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                    emailOfRoommate,binding.editText.getText().toString());

            FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId).push().setValue(currMessage);
            binding.editText.setText("");
        });

        messageAdapter = new MessageAdapter(messages,this);
        binding.recyclerMessage.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMessage.setAdapter(messageAdapter);

        setUpChatRoom();

    }

    private void setUpChatRoom() {
        FirebaseDatabase.getInstance().getReference("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String myUserName = snapshot.getValue(User.class).getName();

                        if(userNameofRoommate.compareTo(myUserName) >= 0) chatRoomId = myUserName + userNameofRoommate;
                        else chatRoomId = userNameofRoommate + myUserName;

                        attachMessageListener(chatRoomId);

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void attachMessageListener(String chatRoomId){
        // addValueEventListener -> changes to the dta in real time and updates your Ui Accordingly
        FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            messages.add(dataSnapshot.getValue(Message.class));
                        }
                        messageAdapter.notifyDataSetChanged();
                        binding.recyclerMessage.scrollToPosition(messages.size()-1);
                        binding.recyclerMessage.setVisibility(View.VISIBLE);
                        binding.progressMessage.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}