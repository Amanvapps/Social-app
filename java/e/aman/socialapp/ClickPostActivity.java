package e.aman.socialapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import javax.xml.transform.sax.SAXResult;

public class ClickPostActivity extends AppCompatActivity {
private TextView PostDescription;
private ImageView PostImage;
private Button DeletePostButton,EditPostButton;
private String PostKey,currentUserID,databaseUserID,image,description;
private FirebaseAuth mAuth;
private DatabaseReference ClickPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth=FirebaseAuth.getInstance();

        currentUserID=mAuth.getCurrentUser().getUid();
        PostKey=getIntent().getExtras().get("PostKey").toString();
        ClickPostRef=FirebaseDatabase.getInstance().getReference().child("posts").child(PostKey);
        PostImage=(ImageView)findViewById(R.id.click_post_image);
        PostDescription=(TextView)findViewById(R.id.click_post_description);
        DeletePostButton=(Button)findViewById(R.id.delete_post_button);
        EditPostButton=(Button)findViewById(R.id.edit_post_button);
       DeletePostButton.setVisibility(View.INVISIBLE);
       EditPostButton.setVisibility(View.INVISIBLE);


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              if(dataSnapshot.exists())
              {
                  description=dataSnapshot.child("description").getValue().toString();
                  image=dataSnapshot.child("postimage").getValue().toString();
                  databaseUserID=dataSnapshot.child("uid").getValue().toString();

                  if(currentUserID.equals(databaseUserID))
                  {
                      DeletePostButton.setVisibility(View.VISIBLE);
                      EditPostButton.setVisibility(View.VISIBLE);
                  }

                  EditPostButton.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view)
                      {
                          editCurrentPost(description);
                      }
                  });

                  PostDescription.setText(description);
                  Picasso.with(ClickPostActivity.this).load(image).into(PostImage);

              }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentPost();
            }
        });

    }

    private void editCurrentPost(String description)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputField=new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(getApplicationContext(), "Post updated succcessfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);

    }

    private void deleteCurrentPost()
    {
        ClickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(getApplicationContext(),"Post has been deleted",Toast.LENGTH_SHORT).show();
    }
    private void sendUserToMainActivity() {
        Intent MainIntent=new Intent(ClickPostActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
