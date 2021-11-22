package com.example.chatkma.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.Interface.IClickCmt;
import com.example.chatkma.R;
import com.example.chatkma.models.Comment;
import com.example.chatkma.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;
    private Context context;
    private IClickCmt iClickCmt;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    public CommentAdapter(List<Comment> commentList, Context context,IClickCmt iClickCmt) {
        this.commentList = commentList;
        this.context = context;
        this.iClickCmt=iClickCmt;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_cmt,parent,false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        int i=position;
        Comment comment=commentList.get(position);
        if(comment==null) return;
        if(comment.getImage()==null) holder.image.setVisibility(View.GONE);
        else {
            Picasso.get().load(comment.getImage()).into(holder.image);
        }
        if(comment.getUid().equals(user.getUid())){
            holder.tvChinhSua.setVisibility(View.VISIBLE);
            holder.tvXoa.setVisibility(View.VISIBLE);
        }
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(comment.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers users= snapshot.getValue(ModelUsers.class);
                if(users==null) return;
                holder.tvName.setText(users.getHoten());
                if(users.getImage()!=null&& !users.getImage().equals("")) Picasso.get().load(users.getImage()).into(holder.imgAvatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.tvXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iClickCmt.onClickUser(comment,i);
            }
        });
        holder.tvNoiDung.setText(comment.getNoidung());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(comment.getTime()));
        } catch (Exception e){
            e.getStackTrace();
        }
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        holder.tvTime.setText(pTime);

    }


    @Override
    public int getItemCount() {
        return commentList.size();

    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgAvatar,image;
        private TextView tvName,tvNoiDung,tvTime,tvXoa,tvChinhSua;
        public CommentViewHolder(@NonNull View itemView) {

            super(itemView);
            imgAvatar=itemView.findViewById(R.id.imgAvatar);
            tvName=itemView.findViewById(R.id.tvName);
            image=itemView.findViewById(R.id.image);
            tvXoa=itemView.findViewById(R.id.tvXoa);
            tvChinhSua=itemView.findViewById(R.id.tvChinhSua);
            tvNoiDung=itemView.findViewById(R.id.tvNoiDung);
            tvTime=itemView.findViewById(R.id.tvTime);
        }
   }
   public void remove(int i){
        commentList.remove(i);
        notifyDataSetChanged();
   }
}
