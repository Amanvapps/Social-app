package e.aman.socialapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
private ImageButton PostCommentButton;
private EditText CommentInputText;
private RecyclerView CommentsList;

private String Post_Key;


private DatabaseReference UsersRef , postRef;
private FirebaseAuth mAuth;
private String Current_User_Id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key=getIntent().getExtras().get("PostKey").toString();

        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        postRef=FirebaseDatabase.getInstance().getReference().child("posts").child(Post_Key).child("comments");
        Current_User_Id=mAuth.getCurrentUser().getUid();

        CommentsList=(RecyclerView)findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);


        CommentInputText = (EditText) findViewById(R.id.comment_input);


        PostCommentButton = (ImageButton)findViewById(R.id.post_comment_button);

         PostCommentButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view)
             {

                 UsersRef.child(Current_User_Id).addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {
                         if (dataSnapshot.exists()) {
                             String Username = dataSnapshot.child("username").getValue().toString();

                             validateComment(Username);

                             CommentInputText.setText("");


                         }
                     }

                     @Override
                     public void onCancelled(DatabaseError databaseError) {

                     }
                 });
             }
         });

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        postRef

                )
        {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position)
            {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());

            }
        };
CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }

        public void setUsername(String username)
        {
            TextView myUserName=(TextView) mView.findViewById(R.id.comment_username);
            myUserName.setText("@" + username + "   ");
        }

        public void setComment(String comment)
        {
            TextView myComment=(TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date)
        {
            TextView myDate=(TextView) mView.findViewById(R.id.comment_date);
            myDate.setText("   Date: " + date);
        }

        public void setTime(String time)
        {
            TextView myTime=(TextView) mView.findViewById(R.id.comment_time);
            myTime.setText("  Time:  " + time);
        }


    }



    private void validateComment(String username)
    {
        String commentText=CommentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(getApplicationContext(), "Please write a comment.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar callforDate=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate=currentDate.format(callforDate.getTime());

            Calendar callforTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
            final String saveCurrentTime=currentTime.format(callforDate.getTime());

            final String RandomKey=Current_User_Id + saveCurrentDate + saveCurrentTime ;

            HashMap commentsMap=new HashMap();
            commentsMap.put("uid",Current_User_Id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",username);
            postRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(), "You have commented successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Error occured Try Again!!!",Toast.LENGTH_SHORT).show();
                    }
                }

            });


        }
    }
}
