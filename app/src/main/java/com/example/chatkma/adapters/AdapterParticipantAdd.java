package com.example.chatkma.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.R;
import com.example.chatkma.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd  extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {

    private Context context;
    private ArrayList<ModelUsers> userList;
    private String groupID, myGroupRole;//admin/giangVien/sinhVien

    public AdapterParticipantAdd(Context context, ArrayList<ModelUsers> userList, String groupID, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupID = groupID;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add,parent,false);

        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        //get data
        ModelUsers modelUsers = userList.get(position);
        String name= modelUsers.getHoten();
        String email=modelUsers.getEmail();
        String maSv=modelUsers.getMaSv();
        String image=modelUsers.getImage();
        String uid=modelUsers.getUid();

        //set data
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        holder.maSvTv.setText(maSv);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_user).into(holder.avatarIv);
        }
        catch (Exception e){
            holder.avatarIv.setImageResource(R.drawable.ic_default_user);
        }
        checkIfAlreadyExits(modelUsers,holder);
        //xu ly click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check user da dc add vao nhom hay chua
                //neu da dc add, show remove-participant/make admin/remove admin option
                // (admin(Giangvien) k thay doi dc role vs creator(PhongDaoTao))
                // neu chua dc add, hien thi option add participant
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupID).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    //user exists// da la participant
                                    String hisPreviousRole =""+dataSnapshot.child("role").getValue();
                                    //option to display in dialog
                                    String[] options;
                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("Tùy chọn");
                                    if (myGroupRole.equals("creator")){
                                        if (hisPreviousRole.equals("admin")){
                                            //im creator, he is admin
                                            options=new String[]{"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                 //xu ly item click
                                                 if (which==0){
                                                     //Remove Admin clicked
                                                     removeAdmin(modelUsers);
                                                 }
                                                 else {
                                                     //remove user click
                                                     removeParticipant(modelUsers);
                                                 }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            //im creator, he is participant
                                            options=new String[]{"Make Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //xu ly item click
                                                    if (which==0){
                                                        //Remove Admin clicked
                                                        makeAdmin(modelUsers);
                                                    }
                                                    else {
                                                        //remove user click
                                                        removeParticipant(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if (myGroupRole.equals("admin")){
                                        if (hisPreviousRole.equals("creator")){
                                            //im admin, he is creator
                                            Toast.makeText(context, "Người tạo nhóm", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (hisPreviousRole.equals("admin")){
                                            //im admin, he is admin too
                                            options=new String[]{"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //xu ly item click
                                                    if (which==0){
                                                        //Remove Admin clicked
                                                        removeAdmin(modelUsers);
                                                    }
                                                    else {
                                                        //remove user click
                                                        removeParticipant(modelUsers);
                                                    }
                                                }
                                            }).show();

                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            //im admin, he is participant
                                            options=new String[]{"Make Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //xu ly item click
                                                    if (which==0){
                                                        //Remove Admin clicked
                                                        makeAdmin(modelUsers);
                                                    }
                                                    else {
                                                        //remove user click
                                                        removeParticipant(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                else {
                                    //user khong phai la participant cua nhom: add
                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("Thêm thành viên")
                                            .setMessage("Thêm người này vào nhóm?")
                                            .setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    addParticipant(modelUsers);
                                                }
                                            })
                                            .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });

    }
    private void addParticipant(ModelUsers modelUsers){
        //setup data
        String timestamp =""+System.currentTimeMillis();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid", modelUsers.getUid());
        hashMap.put("role","participant");
        hashMap.put("timestamp",""+timestamp);
        //add that user into group>groupID>Participant
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants").child(modelUsers.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                   // add thanh cong
                        Toast.makeText(context, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //add that bai
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void makeAdmin(ModelUsers modelUsers) {
        //setup data
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","admin");
        //update database
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants").child(modelUsers.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "Người dùng giờ là Admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //loi
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }


    private void removeParticipant(ModelUsers modelUsers){
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants").child(modelUsers.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //xoa thanh cong
                        Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //xoa that bai
                        Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void removeAdmin(ModelUsers modelUsers) {
        //setup data
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","participant");
        //update database
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants").child(modelUsers.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "Người dùng không còn là Admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //loi
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private void checkIfAlreadyExits(ModelUsers modelUsers, HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants").child(modelUsers.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //already exits
                            String hisRole=""+dataSnapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }
                        else {
                            // doesn't exits
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{

        private ImageView avatarIv;
        private TextView nameTv, emailTv, maSvTv, statusTv;

        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);

            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            emailTv=itemView.findViewById(R.id.emailTv);
            maSvTv=itemView.findViewById(R.id.maSvTv);
            statusTv=itemView.findViewById(R.id.statusTv);
        }
    }
}
