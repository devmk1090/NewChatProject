package com.devkproject.newchatproject.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devkproject.newchatproject.ChatActivity;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.adapters.FriendsListAdapter;
import com.devkproject.newchatproject.customviews.RecyclerViewItemClickListener;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";
    private EditText search_friends_editText;
    private Button search_button;

    public static final int UNSELECTION_MODE = 1;
    public static final int SELECTION_MODE = 2;
    private int selectionMode = UNSELECTION_MODE;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference userRef;
    private DatabaseReference friendsRef;

    private RecyclerView recyclerView;
    private FriendsListAdapter friendsListAdapter;

    public FriendsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View friendsView = inflater.inflate(R.layout.fragment_friends, container,false);

        search_friends_editText = (EditText) friendsView.findViewById(R.id.friends_search_editText);
        search_button = (Button) friendsView.findViewById(R.id.friends_search_button);
        recyclerView = (RecyclerView) friendsView.findViewById(R.id.friends_recyclerView);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        friendsRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid()).child("friends");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, mUser.getDisplayName());
                Log.d(TAG, mUser.getEmail());
                Log.d(TAG, mUser.getUid());
                Log.d(TAG, mUser.getPhotoUrl().toString());
                searchFriends();
            }
        });

        addFriendListener();
        friendsListAdapter = new FriendsListAdapter();
        recyclerView.setAdapter(friendsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final User friend = friendsListAdapter.getItem(position);

                if(friendsListAdapter.getSelectionMode() == FriendsListAdapter.UNSELECTION_MODE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),3);
                    builder.setTitle(friend.getUserNickname() + "님과 대화를 하시겠습니까 ?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                            chatIntent.putExtra("uid", friend.getUid());
                            startActivity(chatIntent);
                        }
                    }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                } else {
                    // selection 되어있다면 false, 안되어있으면 true
                    friend.setSelection(friend.isSelection() ? false : true);
                    int selectionUserCount = friendsListAdapter.getSelectionUserCount();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),3);
                    builder.setTitle(selectionUserCount + "명과 대화를 하시겠습니까 ?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                            chatIntent.putExtra("uids", friendsListAdapter.getSelectedUids());
                            startActivity(chatIntent);
                        }
                    }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
            }
        }));

        return friendsView;
    }

    public void searchFriends() {

        final String inputEmail = search_friends_editText.getText().toString();

        if(inputEmail.isEmpty()) {
            Toast.makeText(getActivity(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if(inputEmail.equals(mUser.getEmail())) {
            Toast.makeText(getActivity(), "자기 자신은 친구로 입력할 수 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        //계속 읽을 필요가 없으므로 한번만 읽는 singlevalue 사용
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // friends db 에서 uid 만 얻어올거기 때문에 getChildren 사용
                Iterable<DataSnapshot> friendsIterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> friendsIterator = friendsIterable.iterator();

                while(friendsIterator.hasNext()) {
                    User user = friendsIterator.next().getValue(User.class);

                    if (user.getUserEmail().equals(inputEmail)) {
                        Toast.makeText(getActivity(), "이미 등록된 친구입니다", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> usersIterator = dataSnapshot.getChildren().iterator();
                        int userCount = (int) dataSnapshot.getChildrenCount();
                        int loopCount = 1;

                        while(usersIterator.hasNext()) {
                            // 현재 가리키고 있는 유저의 정보를 가져옴
                            final User currentUser = usersIterator.next().getValue(User.class);

                            if(inputEmail.equals(currentUser.getUserEmail())) {

                                // 친구 등록 로직
                                // 5. users/{myuid}/friends/{someone_uid}/firebasePush/상대 정보를 나의 친구에 등록
                                friendsRef.push().setValue(currentUser, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        // 6. users/{someone_uid}/friends/{myuid}/상대 정보에 내정보 등록
                                        // 나의 정보를 가져온다
                                        userRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);
                                                userRef.child(currentUser.getUid()).child("friends").push().setValue(user);
                                                Toast.makeText(getActivity(), "친구등록이 완료되었습니다", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });
                            } else {
                                if(loopCount++ >= userCount){
                                    Toast.makeText(getActivity(), "가입을 하지 않은 친구입니다", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            // 총 사용자의 명수 == loopCount
                            // 등록된 사용자가 없다는 메세지를 출력
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void addFriendListener() {

        friendsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // User 모델과 friend 는 구조가 동일하기 때문에 User 모델로 가져온다
                User friend = dataSnapshot.getValue(User.class);
                drawUI(friend);
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
    }
    private void drawUI(User friend) {
        friendsListAdapter.addItem(friend);
    }

    //현재의 모드가 SELECTION_MODE 라면 UNSEL 일때와 SEL 일때의 작동이 다르게 하기 위한 코드
    public void toggleSelectionMode() {
        friendsListAdapter.setSelectionMode(friendsListAdapter.getSelectionMode() == FriendsListAdapter.SELECTION_MODE ? friendsListAdapter.UNSELECTION_MODE :
                FriendsListAdapter.SELECTION_MODE);
    }

}
