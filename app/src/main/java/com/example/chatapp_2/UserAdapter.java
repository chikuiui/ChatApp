package com.example.chatapp_2;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.chatapp_2.databinding.UserHolderBinding;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    Context context;
    ArrayList<User> users;
    // when we click on particular user we want its position then perform some task to do that we create interface
    interface OnUserClickListener {
        void onUserClicked(int position);
    }
    // create instance of that interface
    OnUserClickListener onUserClickListener;


    public UserAdapter(Context context, ArrayList<User> users,OnUserClickListener onUserClickListener){
        this.context = context;
        this.users = users;
        this.onUserClickListener = onUserClickListener;
    }


    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_holder,parent,false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        String userName = users.get(position).getName();
        holder.user_name.setText(userName);

        String image = users.get(position).getProfilePhoto();
        Glide.with(context)
                .load(image)
                .error(R.drawable.account_img)
                .placeholder(R.drawable.account_img)
                .into(holder.user_image);

    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserHolder extends RecyclerView.ViewHolder{
        ImageView user_image;
        TextView user_name;


        public UserHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v ->
                    onUserClickListener.onUserClicked(getAdapterPosition()));

            user_image = itemView.findViewById(R.id.img_profile);
            user_name = itemView.findViewById(R.id.user_name);

        }
    }
}
