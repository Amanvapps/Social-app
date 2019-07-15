package e.aman.socialapp;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView MyPostsList;
    private String currentUserID;

    Boolean LikeChecker=false;

    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef , user_ref, LikesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mToolbar = (Toolbar)findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        MyPostsList = (RecyclerView)findViewById(R.id.my_all_post_list);
        MyPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        MyPostsList.setLayoutManager(linearLayoutManager);

        user_ref= FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        displayMyAllPosts();
    }

    private void displayMyAllPosts()
    {
        Query myPostQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerAdapter<Posts,MyPostsViewHolder > firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>
                (
                        Posts.class,
                        R.layout.all_posts_layout,
                        MyPostsViewHolder.class,
                        myPostQuery
                )
        {
            @Override
            protected void populateViewHolder(MyPostsViewHolder viewHolder, Posts model, int position)
            {
                final String PostKey=getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(),model.getPostimage());


                viewHolder.setLikeButtonStatus(PostKey);


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Intent ClickPostIntent=new Intent(MyPostsActivity.this,ClickPostActivity.class);
                        ClickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(ClickPostIntent);
                    }
                });

                viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Intent CommentsIntent=new Intent(MyPostsActivity.this,CommentsActivity.class);
                        CommentsIntent.putExtra("PostKey",PostKey);
                        startActivity(CommentsIntent);
                    }
                });



                viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        LikeChecker = true;

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if(LikeChecker.equals(true))
                                {
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }
                                    else
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });






            }
        };

        MyPostsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder
    {

        ImageButton LikePostButton,CommentPostButton;
        TextView DisplayNoOfLikes;
        int count_likes;
        String currentUserId;
        DatabaseReference LikesRef;



        View mView;
        public MyPostsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;


            LikePostButton=(ImageButton) mView.findViewById(R.id.like_button);
            DisplayNoOfLikes=(TextView) mView.findViewById(R.id.display_no_of_likes);
            CommentPostButton=(ImageButton) mView.findViewById(R.id.comment_button);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        }

        public void setLikeButtonStatus(final String Postkey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(Postkey).hasChild(currentUserId)) {
                        count_likes = (int) dataSnapshot.child(Postkey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText(Integer.toString(count_likes) + (" Likes"));
                    } else {
                        count_likes = (int) dataSnapshot.child(Postkey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText(Integer.toString(count_likes) + (" Likes"));
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        public void setFullname(String fullname)
        {
            TextView username=(TextView) mView.findViewById(R.id.post_username);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx,String profileimage)
        {
            CircleImageView image=(CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setTime(String time)
        {
            TextView PostTime=(TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("   " + time);
        }
        public void setDate(String date)
        {
            TextView PostDate=(TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("   " + date);
        }
        public void setDescription(String description)
        {
            TextView PostDescription=(TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }
        public void setPostimage(Context ctx1,String postimage)
        {
            ImageView PostImage=(ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }

    }


}
