package com.example.chatapp_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp_2.databinding.MessageHolderBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private final ArrayList<Message> messages;
    private final Context context;

    public MessageAdapter(ArrayList<Message> messages,Context context){
        this.messages = messages;
        this.context = context;
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageHolderBinding binding = MessageHolderBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MessageHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
         Message message = messages.get(position);
         holder.txtMessage.setText(message.getContent());

        ConstraintLayout constraintLayout = holder.binding.ccLayout;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);


        if(!messages.get(position).getSender().equals(Objects
                .requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())){
            constraintSet.clear(R.id.message_content,ConstraintSet.RIGHT);
            constraintSet.connect(R.id.message_content,ConstraintSet.LEFT,
                    R.id.cc_layout,ConstraintSet.LEFT);
            holder.txtMessage.setBackgroundResource(R.drawable.shape2);
        }else{
            constraintSet.clear(R.id.message_content,ConstraintSet.LEFT);
            constraintSet.connect(R.id.message_content,ConstraintSet.RIGHT,
                    R.id.cc_layout,ConstraintSet.RIGHT);
            holder.txtMessage.setBackgroundResource(R.drawable.shape1);
        }

        constraintSet.applyTo(constraintLayout);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageHolder extends RecyclerView.ViewHolder{
        private final MessageHolderBinding binding;
        private final TextView txtMessage;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            binding = MessageHolderBinding.bind(itemView);
            txtMessage = binding.messageContent;
        }
    }
}
