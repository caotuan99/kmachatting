package com.example.chatkma.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.ChatActivity;
import com.example.chatkma.R;
import com.example.chatkma.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List <ModelUsers> usersList;

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_users
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        String hisUID = usersList.get(i).getUid();
        String userImage = usersList.get(i).getImage();
        String userName = usersList.get(i).getHoten();
        String userEmail = usersList.get(i).getEmail();
        String userMaSV = usersList.get(i).getMaSv();

        //set data
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        myHolder.mMaSvTv.setText(userMaSV);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_user)
                    .into(myHolder.mAvatarTv);

        } catch (Exception e){

        }
        // xu ly item click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUID);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    class  MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarTv;
        TextView mNameTv, mEmailTv, mMaSvTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarTv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
            mMaSvTv = itemView.findViewById(R.id.maSvTv);
        }
    }
}
