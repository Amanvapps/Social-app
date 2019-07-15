package e.aman.socialapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
private Toolbar mToolbar;
private ImageButton SearchButton;
private EditText SearchInputText;

private RecyclerView SearchResultList;

private DatabaseReference AllUsersDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mToolbar=(Toolbar) findViewById(R.id.find_friends_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        AllUsersDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Users");

        SearchResultList=(RecyclerView)findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));


        SearchButton=(ImageButton)findViewById(R.id.search_people_friends_button);
        SearchInputText=(EditText)findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String SearchBoxInput=SearchInputText.getText().toString();
                searchPeopleAndFriends(SearchBoxInput);
            }
        });

    }

    private void searchPeopleAndFriends(String searchBoxInput)
    {

        Toast.makeText(getApplicationContext(),"Searching....",Toast.LENGTH_LONG).show();
        Query searchPeopleAndFriendsQuery = AllUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder > firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindFriendsViewHolder.class,
                        searchPeopleAndFriendsQuery

                ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, FindFriends model, final int position)
            {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                      String visit_user_id=getRef(position).getKey();

                        Intent ProfileIntent=new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                        ProfileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(ProfileIntent);
                    }
                });
            }
        };
        SearchResultList.setAdapter(firebaseRecyclerAdapter);

    }



public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
{
    View mView;

    public FindFriendsViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setProfileimage(Context ctx,String profileimage)
    {
        CircleImageView myImage=(CircleImageView) mView.findViewById(R.id.all_users_profile_image);
        Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
    }

    public void setFullname(String fullname)
    {
        TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_name);
        myName.setText(fullname);
    }

    public void setStatus(String status)
    {
        TextView myStatus = (TextView) mView.findViewById(R.id.all_users_status);
        myStatus.setText(status);
    }



}


}
