package com.example.hoctest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.ChatMessage;


public class ChatRoomActivity extends Activity {

    private Button btn_send_msg, btn_Back;
    private EditText input_msg;
    private TextView roomInfo_Name;
    private FirebaseUser user;
    private String room_name, user_name;


    // Database reference
    private DatabaseReference root;
    // Firebase Authentication
    FirebaseAuth mAuth;

    //show in list, Later used for getting databaseref to different places in the firebase database
    DatabaseReference messagesInChat;
    DatabaseReference usersInChat;

    // Used in recycleview to check if the used scrolled and where the user currently is at in the list
    boolean userScrolled = false;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    // read the 50 last posts in the chatroom
    private Integer msgNum = 50;

    //
    LinearLayoutManager mLayoutManager;
    private RecyclerView recyclerView;

    // adapter used later to populate the recycleview
    private FirebaseRecyclerAdapter<ChatMessage, BlogPostHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        btn_Back = findViewById(R.id.btn_Back);
        btn_send_msg = findViewById(R.id.btn_send);
        input_msg = findViewById(R.id.msg_Input);
        roomInfo_Name = findViewById(R.id.room_Info_text);

        //get the room name which the user is currently in
        room_name = getIntent().getExtras().get("room_name").toString();

        //uses the room name to ref the desired database child
        root = FirebaseDatabase.getInstance().getReference().child(room_name);
        mAuth = FirebaseAuth.getInstance();

        /*
        ref for the two childs in the database, messages, which holds userid, user image, time, and message text
        users if for knowing if the user has been in this room before and wishes to get notifications
         */
        messagesInChat = FirebaseDatabase.getInstance().getReference().child(room_name).child("Messages");
        usersInChat = FirebaseDatabase.getInstance().getReference().child(room_name).child("Users");

        /*
        This sets up the recyclerview with the desired layouts and ref to the view in the xml file
         */
        mLayoutManager = new LinearLayoutManager(ChatRoomActivity.this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /*
        this sets up the firebase recycleoptions, used for later to populate the view with
        the correct information by using the ChatMessage class
         */
        Query query = messagesInChat.limitToLast( msgNum );
        FirebaseRecyclerOptions<ChatMessage> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        /*
            builds up the chat messages with the Blogpostholder class located at the bottom and uses it to get the correct
            values from the database by using the model chatmessage
         */
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatMessage, BlogPostHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull BlogPostHolder blogPostHolder, int i, @NonNull ChatMessage chatMessage) {
                blogPostHolder.setBlogPost(chatMessage);
            }

            @Override
            public BlogPostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

                return new BlogPostHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);

        implementScrollListener();

        /*
            Adds a eventlistener to all the children in the message child to know if there has been made a change or
            a new post has been added
         */
        messagesInChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*
        back button to be able to leave the chat room
         */
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goBack();
            }
        });

        /*
        sets the user_name to the username of the person logged in, used for checking if the person has been here before

         */
        if (mAuth.getCurrentUser() != null) {
            user = mAuth.getCurrentUser();
            user_name = user.getDisplayName();
            updateUI();
        }

        //Add information to userdatabase
        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                String message = input_msg.getText().toString();

                Date date = new Date();
                Date newDate = new Date(date.getTime() + (604800000L * 2) + (24 * 60 * 60));
                SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String stringdate = dt.format(newDate);
                Log.d("TAG", stringdate);

                FirebaseUser user = mAuth.getCurrentUser();
                String photo = String.valueOf(user.getPhotoUrl());

                ChatMessage chatMessage = new ChatMessage(user_name, message, stringdate, photo);

                messagesInChat.push().setValue(chatMessage);


                input_msg.setText(null);

                /*
                Adds users to user child and checks if the user has been here before, then asks if they want to be notified
                 */
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(room_name);
                ref.child("users").child(user_name).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            return;
                        } else {
                            AlertDialogNotification();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

            }
        });

    } //onCreate ends here
    /*
        alert dialog used to ask the user if they want to be notified if there are any new posts
    */
    public void AlertDialogNotification(){
        AlertDialog alertDialog = new AlertDialog.Builder(ChatRoomActivity.this).create();
        alertDialog.setTitle("Chat :" + room_name);
        alertDialog.setMessage("Do you wish to get notifications from this chat?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String,Object> map = new HashMap<String, Object>();
                        map.put(user_name,true);
                        root.child("users").updateChildren(map);
                        input_msg.setText("");
                        Log.d("TAG", String.valueOf(map));
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String,Object> map = new HashMap<String, Object>();
                        map.put(user_name,false);
                        root.child("users").updateChildren(map);
                        input_msg.setText("");
                        Log.d("TAG", String.valueOf(map));
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void implementScrollListener() {

        recyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView,
                                                     int newState) {

                        super.onScrollStateChanged(recyclerView, newState);

                        // If scroll state is touch scroll then set userScrolled

                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            userScrolled = true;

                        }

                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx,
                                           int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager
                                .findFirstVisibleItemPosition();

                        Toast.makeText(ChatRoomActivity.this, "Scrolling..", Toast.LENGTH_SHORT).show();

                        if (userScrolled
                                && (visibleItemCount + pastVisiblesItems) == totalItemCount) {
                            userScrolled = false;

                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseRecyclerAdapter!= null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }


    /*
        holder for the firebaseAdapter to build the messages into the recycleview
     */

    private class BlogPostHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView,messageTimeView, userTextView;
        private ImageView messageImageView;

        BlogPostHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_user_name);
            messageTimeView = itemView.findViewById(R.id.message_time);
            userTextView = itemView.findViewById(R.id.message_text);
            messageImageView = itemView.findViewById(R.id.user_icon);

        }

        void setBlogPost(ChatMessage chatMessage) {
            String imageThumb = chatMessage.getMessageUser();
            messageTextView.setText(imageThumb);
            String timeThumb = chatMessage.getMessageTime();
            messageTimeView.setText(timeThumb);
            String userId = chatMessage.getMessageText();
            userTextView.setText(userId);
            String photo = String.valueOf(Uri.parse(chatMessage.getMessageIconUrl()));
            Picasso.get().load(photo).into(messageImageView);
        }
    }

    private void updateUI(){

        if (room_name != null) {

            roomInfo_Name.append(room_name);

        } else {

            Toast.makeText(ChatRoomActivity.this, "Unable to locate room", Toast.LENGTH_SHORT).show();
            goBack();
        }
    }

    private void goBack(){

        startActivity(new Intent(this, LoggedActivity.class));
        finish();
    }

}