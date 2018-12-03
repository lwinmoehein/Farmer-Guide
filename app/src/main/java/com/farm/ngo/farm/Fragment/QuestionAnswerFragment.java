package com.farm.ngo.farm.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.farm.ngo.farm.Adapter.MessageAdapter;
import com.farm.ngo.farm.Holder.UsingSQLiteHelper;
import com.farm.ngo.farm.Model.Image;
import com.farm.ngo.farm.Model.ImageUpload;
import com.farm.ngo.farm.Model.Message;
import com.farm.ngo.farm.Model.MessageType;
import com.farm.ngo.farm.Model.Type;
import com.farm.ngo.farm.Model.User;
import com.farm.ngo.farm.R;
import com.farm.ngo.farm.Service.ChatHelper;
import com.farm.ngo.farm.Utility.FileUtil;
import com.farm.ngo.farm.Utility.ImageUtil;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import static android.app.Activity.RESULT_OK;

public class QuestionAnswerFragment extends Fragment {

    //Check Declaration
    private RecyclerView recy;
    LinearLayoutManager ly;
    EditText editText;
    private static final int showItem=20;
    int itempos=0;
    SwipeRefreshLayout swipeRefreshLayout;
    private static String lastitem="";
    private MessageAdapter mAdapter;
    ImageButton choose_iamge_or_camera, openCamera,addImage;
    LinearLayoutCompat show;
    boolean moreItem=true;
    User user;
    private static final String TAG = "Storage#MainActivity";
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final int RC_TAKE_PICTURE = 101;
    private static final int CAMERA_REQUEST = 893;
    private static final int CAMERA_PER_REQUEST=342;
    private static final int STORAGE_PER_REQUEST=3478;
    private LinkedList<Message> messages = new LinkedList<>();
    private LinkedList<String> messagesId = new LinkedList<>();
    //End

    private final String API_KEY = "AIzaSyAlc7IIpWJIvdTQbqUOQBSsyAbZ8pzeFUA ";
    private Context mContext;
    private Firebase mFirebase;


    public QuestionAnswerFragment(Context context) {
        // Required empty public constructor
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_question_answer, container, false);
        FirebaseApp.initializeApp(mContext);
        Firebase.setAndroidContext(mContext);

        //Reading User From SQLite database
        UsingSQLiteHelper helper = new UsingSQLiteHelper(mContext);
        user = helper.getUser();
        Log.i("user id",user.getId());
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);
        show = (LinearLayoutCompat) view.findViewById(R.id.chooe_image_oe_camera);
        choose_iamge_or_camera = (ImageButton) view.findViewById(R.id.add_btn);
        openCamera = (ImageButton) view.findViewById(R.id.openCamera);
        addImage = (ImageButton) view.findViewById(R.id.addImage);
        choose_iamge_or_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (show.getVisibility() == View.GONE) {
                    show.setVisibility(View.VISIBLE);
                    choose_iamge_or_camera.setImageResource(R.drawable.ic_clear);

                } else if (show.getVisibility() == View.VISIBLE) {
                    show.setVisibility(View.GONE);
                    choose_iamge_or_camera.setImageResource(R.drawable.add_image);
                }
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraFunction(v);
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddImage(v);
            }
        });
        recy = (RecyclerView) view.findViewById(R.id.messages_view);
        editText = (EditText) view.findViewById(R.id.editText);
        ly = new LinearLayoutManager(mContext);
        mAdapter = new MessageAdapter(getActivity(), messages);
        recy.setAdapter(mAdapter);
        recy.setLayoutManager(ly);
        recy.setHasFixedSize(true);
        recy.scrollToPosition(messages.size() - 1);
        loadData();
        view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText() != null && !editText.getText().toString().trim().equals("")) {
                    SendMessage(v);

                }

            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itempos=0;
                loadMoreData();

            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {

                try {
                    final File actualImage = FileUtil.from(mContext, data.getData());
                    final Context context= getActivity();
                    ImageUtil imgu = new ImageUtil(actualImage, context, user,false);
                    imgu.uploadImage();
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                    builder.setMessage("Are you sure want to send this photo");
//                    builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User clicked OK button
//
//                        }
//
//                    });
//                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User cancelled the dialog
//                        }
//                    });
//                    AlertDialog dialog = builder.create();
//                    dialog.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Image image=new Image((Bitmap)data.getExtras().get("data"),user);
                final Context context = getActivity();
                new ImageUpload(context,false).execute(image);
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setMessage("Are you sure want to send this photo");
//                builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User clicked OK button
//
//                    }
//
//                });
//                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
//                AlertDialog dialog = builder.create();
//                dialog.show();
//

            } else {
                Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PER_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case CAMERA_PER_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED &&  grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission is granted
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //For launch Camera to take camera from UserChat to send photo
    private void launchCamera() {
        Log.d(TAG, "launchCamera");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(intent, RC_TAKE_PICTURE);
    }


    //To load more chat data From cloud firebase
    private void loadMoreData() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("messages").child(user.getId());
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(messagesId.indexOf(dataSnapshot.getKey())==-1) {
                    if (lastitem.equals(dataSnapshot.getKey())) {
                        moreItem=false;
                        lastitem="";
                        mAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    else {
                        Message mg = dataSnapshot.getValue(Message.class);
                        messagesId.add(itempos,dataSnapshot.getKey());
                        messages.add(itempos++, mg);
                        moreItem=false;
                        if (itempos == 1) {
                            lastitem = dataSnapshot.getKey();
                        }
                        mAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
                if(!moreItem){
                    swipeRefreshLayout.setRefreshing(false);
                }


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
        };

        Query query=database.orderByKey().endAt(lastitem).limitToLast(showItem);
        query.addChildEventListener(childEventListener);
        if(moreItem){
            Handler handler=new Handler();
            Runnable r=new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext,"OK",Toast.LENGTH_SHORT).show();
                }
            };
            handler.postDelayed(r,2000);
        }
    }

    //To load data
    private void loadData(){
        //mFirebase = new Firebase("https://farm-application-7d2d2.firebaseio.com/chat-table");
        DatabaseReference database=FirebaseDatabase.getInstance().getReference().child("messages").child(user.getId());
        ChildEventListener childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message mg=dataSnapshot.getValue(Message.class);
                messages.add(mg);
                messagesId.add(dataSnapshot.getKey());
                itempos++;
                if(itempos==1){
                    lastitem=dataSnapshot.getKey();
                }

                mAdapter.notifyItemInserted(messages.size()-1);
                swipeRefreshLayout.setRefreshing(false);
                recy.scrollToPosition(messages.size()-1);
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
        };
        Query query=database.orderByKey().limitToLast(showItem);
        query.addChildEventListener(childEventListener);
    }

    //To Send Message to Cloud
    public void SendMessage(View view) {
        if (show.getVisibility() == View.VISIBLE) {
            show.setVisibility(View.GONE);
            choose_iamge_or_camera.setImageResource(R.drawable.add_image);
        }
        DatabaseReference database=FirebaseDatabase.getInstance().getReference().child("messages").child(user.getId());

        Message mg = new Message( user.getId(), Type.user, editText.getText().toString(),ServerValue.TIMESTAMP,"",MessageType.text, false);
        String key = database.push().getKey();
        database.child(key).setValue(mg);
        ChatHelper.sendChat(user.getTownship(),user.getId(),mg,user.getName(),false);
        editText.setText("");
        recy.scrollToPosition(messages.size() - 1);
        Toast.makeText(mContext, "Send success", Toast.LENGTH_SHORT);

    }

    //To add Image
    public void btnAddImage(View view) {
        if (ContextCompat.checkSelfPermission(mContext,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){

            // Permission is granted
            launchCamera();
        }
        else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                Toast.makeText(mContext,"Storage permension is needed to post image",Toast.LENGTH_SHORT).show();

            }

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PER_REQUEST);



        }


    }

    //To open Camera
    public void openCameraFunction(View view){
        if (ContextCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){

            // Permission is granted
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
        else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(mContext,"Storage permension amd Camera permension is needed to post image",Toast.LENGTH_SHORT).show();

            }

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CAMERA_PER_REQUEST);



        }
        Log.d(TAG, "launchCamera");
    }

}
