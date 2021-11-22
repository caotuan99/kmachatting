package com.example.chatkma.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.CmtActivity;
import com.example.chatkma.ProfileActivity;
import com.example.chatkma.R;
import com.example.chatkma.models.ModelPost;
import com.example.chatkma.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;
    private FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        ModelPost post=postList.get(i);
        if(post==null) return;
        String uid= postList.get(i).getUid();
        String uEmail= postList.get(i).getuEmail();
        String uName= postList.get(i).getuName();
        String uDp= postList.get(i).getuDp();
        String pId= postList.get(i).getpId();
        String pTitle= postList.get(i).getpTitle();
        String pDescription= postList.get(i).getpDescr();
        String pImage= postList.get(i).getpImage();
        String pTimeStamp= postList.get(i).getpTime();

        // chuyen timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        } catch (Exception e){
            e.getStackTrace();
        }
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        myHolder.uNameTv.setText(uName);
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pDescriptionTv.setText(pDescription);
        setSlLike(myHolder,post);
        setSlCmt(myHolder,post);

        setLike(post,myHolder);




        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_post).into(myHolder.uPictureIv);
        }
        catch (Exception e){

        }

        //set post image
        // neu k co anh thi an ImageView di
        if (pImage.equals("noImage")){
            //hide imageView
            myHolder.pImageIV.setVisibility(View.GONE);

        }
        else {
            try {
                Picasso.get().load(pImage).into(myHolder.pImageIV);
            }
            catch (Exception e){

            }
        }
        // handle btn click
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // phát triển sau
                Toast.makeText(context, "Thêm", Toast.LENGTH_SHORT).show();
            }
        });
        // handle btn Like
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
              if(myHolder.tvlike.getText().equals("Thích")){
                  myHolder.tvlike.setText("Đã thích");
                  myHolder.tvlike.setTextColor(R.color.xanhsang);
                  myHolder.imglike.setImageResource(R.drawable.icon_like_blue);
                  DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike").child(user.getUid());
                  reference.setValue("Like");




              }else {
                  myHolder.tvlike.setText("Thích");
                  myHolder.imglike.setImageResource(R.drawable.icon_like);
                  myHolder.tvlike.setTextColor(R.color.colorBlack);
                  DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike").child(user.getUid());
                  reference.removeValue();

              }

            }
        });
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu=new PopupMenu(context,v);
                menu.getMenuInflater().inflate(R.menu.menu_pos,menu.getMenu());
                menu.show();
            }
        });
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // phát triển sau
                Intent intent= new Intent(context, CmtActivity.class);
                Bundle bundle= new Bundle();
                bundle.putSerializable("Post",post);
                intent.putExtras(bundle);
                context.startActivity(intent);

            }
        });
        // handle btn Share
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // phát triển sau
                Toast.makeText(context, "Chia sẻ", Toast.LENGTH_SHORT).show();
            }
        });
        myHolder.uPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gttToProfile(post);
            }
        });
        myHolder.uNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gttToProfile(post);
            }
        });


    }

    private void gttToProfile(ModelPost post) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(post.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers users= snapshot.getValue(ModelUsers.class);
                Intent intent= new Intent(context, ProfileActivity.class);
                Bundle bundle= new Bundle();
                bundle.putSerializable("User",users);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setSlCmt(MyHolder myHolder, ModelPost post) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("Comments");
        reference.addChildEventListener(new ChildEventListener() {
            int cmt=0;
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.getValue()!=null) {
                    cmt =cmt+1;
                    myHolder.slCmt.setText(String.valueOf(cmt)+" Bình luận");

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                cmt =cmt-1;
                myHolder.pLikeTv.setText(String.valueOf(cmt)+" Bình luận");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setSlLike(MyHolder myHolder,ModelPost post) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike");
        reference.addChildEventListener(new ChildEventListener() {
            int like=0;
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.getValue()!=null) {
                    like =like+1;
                    myHolder.pLikeTv.setText(String.valueOf(like)+" Lượt thích");

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                like =like-1;
                myHolder.pLikeTv.setText(String.valueOf(like)+" Lượt thích");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setLike(ModelPost post, MyHolder myHolder) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts").child(post.getpId()).child("ListLike").child(user.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null){
                    myHolder.tvlike.setText("Thích");
                    myHolder.imglike.setImageResource(R.drawable.icon_like);
                }
                else {
                    myHolder.tvlike.setText("Đã thích");
                    myHolder.imglike.setImageResource(R.drawable.icon_like_blue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //view from row_post.xml
        ImageView uPictureIv, pImageIV,imglike;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikeTv,tvlike,slCmt;
        ImageButton moreBtn;
        LinearLayout likeBtn, commentBtn, shareBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIV = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            tvlike = itemView.findViewById(R.id.tvlike);
            imglike = itemView.findViewById(R.id.imglike);
            slCmt = itemView.findViewById(R.id.slCmt);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikeTv = itemView.findViewById(R.id.pLikeTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likebtn);
            commentBtn = itemView.findViewById(R.id.commentbtn);
            shareBtn = itemView.findViewById(R.id.sharebtn);
        }
    }
}
