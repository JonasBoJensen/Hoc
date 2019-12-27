package com.example.hoctest;

        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.auth.api.signin.GoogleSignIn;
        import com.google.android.gms.auth.api.signin.GoogleSignInClient;
        import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;
        import com.squareup.picasso.Picasso;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.HashSet;
        import java.util.Iterator;
        import java.util.Map;
        import java.util.Set;

public class LoggedActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    TextView userInfo_Name;
    ImageView user_Image;

    Button btn_Logout;

    GoogleSignInClient mGoogleSignInClient;

    //stuff for chatrooms
    private Button  add_room;
    private EditText room_name;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_rooms = new ArrayList<>();
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);

        mAuth = FirebaseAuth.getInstance();

        userInfo_Name = findViewById(R.id.username_Info);
        user_Image = findViewById(R.id.user_Image);

        btn_Logout = findViewById(R.id.btn_Logout);

        btn_Logout.setOnClickListener(v -> Logout());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            Log.d("Tag", user.getDisplayName());
            updateUI(user);
        }

        /*
            chatRoom stuff
         */
        add_room = findViewById(R.id.btn_add_room);
        room_name = findViewById(R.id.room_name_edittext);
        listView = findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list_of_rooms);

        listView.setAdapter(arrayAdapter);

        add_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String,Object> map = new HashMap<String, Object>();
                map.put(room_name.getText().toString(),"");
                root.updateChildren(map);

            }
        });

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<String>();

                Iterator i = dataSnapshot.getChildren().iterator();

                while (i.hasNext()){
                    set.add(((DataSnapshot)i.next()).getKey());
                }

                list_of_rooms.clear();
                list_of_rooms.addAll(set);

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(),ChatRoomActivity.class);
                intent.putExtra("room_name",((TextView)view).getText().toString() );
//                intent.putExtra("user_name",name);

                startActivity(intent);
                finish();
            }
        });

    }

    private void updateUI(FirebaseUser user) {

        if (user != null) {
            String name = user.getDisplayName();
            String photo = String.valueOf(user.getPhotoUrl());

            userInfo_Name.append(" Hey " + name);

            Picasso.get().load(photo).into(user_Image);

        } else {
            Toast.makeText(LoggedActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            LoggingOutUI();
        }
    }

    private void Logout(){
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> LoggingOutUI());
    }

    private void LoggingOutUI(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
