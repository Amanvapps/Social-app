package e.aman.socialapp;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar ChatToolbar;
    private ImageButton SendMessageButton , SendImageFileButton;
    private EditText USerMessageInput;
    private RecyclerView UserMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private String MessageReceiverID , MessageReceiverName , MessageSenderID ,saveCurrentDate,saveCurrentTime;

    private TextView ReceiverName , userLastSeen;
    private CircleImageView ReceiverProfileImage;

    private DatabaseReference RootRef , UserssRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        MessageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserssRef = FirebaseDatabase.getInstance().getReference().child("Users");


       MessageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        MessageReceiverName = getIntent().getExtras().get("username").toString();


    initializeFields();

    DisplayReceiverInfo();

    SendMessageButton.setOnClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            sendMessage();
        }
    });

    fetchMessages();

    }

    private void fetchMessages()
    {

        RootRef.child("Messages").child(MessageSenderID).child(MessageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists())
                {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage()
    {
        updateUserStatus("online");

        String messageText = USerMessageInput.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(getApplicationContext(), "Please Type Something.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String message_sender_ref = "Messages/" + MessageSenderID + "/" + MessageReceiverID;
            String message_receiver_ref = "Messages/" + MessageReceiverID + "/" + MessageSenderID;
            DatabaseReference users_message_key = RootRef.child("Messages").child(MessageSenderID).child(MessageReceiverID).push();
            String message_push_id=users_message_key.getKey();

            Calendar callforDate=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate=currentDate.format(callforDate.getTime());

            Calendar callforTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm ss");
            saveCurrentTime=currentTime.format(callforDate.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",MessageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id , messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Message send successfully",Toast.LENGTH_SHORT).show();
                        USerMessageInput.setText("");
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(), "Error Ocurred : "+message, Toast.LENGTH_SHORT).show();
                        USerMessageInput.setText("");
                    }


                }
            });

        }
    }

    public void updateUserStatus(String state)
    {
        String saveCurrentDate , saveCurrentTime;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UserssRef.child(MessageSenderID).child("userState").updateChildren(currentStateMap);


    }


    private void DisplayReceiverInfo()
    {
        ReceiverName.setText(MessageReceiverName);
        RootRef.child("Users").child(MessageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {

                    final String profileImage=dataSnapshot.child("profileimage").getValue().toString();


                    final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                    final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(type.equals("online"))
                    {
                        userLastSeen.setText("online");
                    }
                    else
                    {
                        userLastSeen.setText("last seen: " + lastTime + "  " + lastDate);
                    }

                    Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(ReceiverProfileImage);

                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields()
    {
        ChatToolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();                           //provide a actionbar in  app
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);     //for adding chat custom bar in actionbar
        actionBar.setCustomView(action_bar_view);


      ReceiverName = (TextView) findViewById(R.id.custom_profile_name);
      ReceiverProfileImage = (CircleImageView)findViewById(R.id.custom_profile_image);
       SendMessageButton = (ImageButton)findViewById(R.id.send_message_button);
       SendImageFileButton = (ImageButton)findViewById(R.id.send_image_file_button);
       USerMessageInput = (EditText) findViewById(R.id.input_messages);
        userLastSeen  = (TextView) findViewById(R.id.custom_user_last_seen);

       messagesAdapter = new MessagesAdapter(messagesList);
       UserMessagesList = (RecyclerView)findViewById(R.id.messages_list_users);
       linearLayoutManager = new LinearLayoutManager(this);
       UserMessagesList.setHasFixedSize(true);
       UserMessagesList.setLayoutManager(linearLayoutManager);
       UserMessagesList.setAdapter(messagesAdapter);

    }


}
