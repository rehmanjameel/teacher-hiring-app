package org.ed.track;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.ed.track.adapter.ChatAdapter;
import org.ed.track.databinding.ActivityChatBinding;
import org.ed.track.model.ChatMessage;
import org.ed.track.utils.App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private FirebaseFirestore db;
    private String senderId, receiverId, chatId;

// it hink we should getthe CHATS ON THE BASE OF CURRENT USERS as they are
//    linked wiht chat id student or teacher. i want to show the list of chats
//    in chats activity when user open the chat it will redirect the main
//    chat activity according to id of student or teacher if it contains in
//    that chat..so for that i will need the spearate adapter and layout where
//    we can show the image of other persoen student or teacher on chatsactivity
//    where chats list will be available

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        senderId = App.getString("user_id");
        receiverId = getIntent().getStringExtra("receiverId");
        if (receiverId == null) {
            finish();
            return;
        }
        chatId = getChatId(senderId, receiverId);

        db = FirebaseFirestore.getInstance();

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, senderId);
        binding.recyclerChat.setAdapter(chatAdapter);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(this));

        binding.btnSend.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private String getChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    private void sendMessage() {
        String msg = binding.editMessage.getText().toString().trim();
        if (msg.isEmpty()) return;

        ChatMessage message = new ChatMessage(senderId, receiverId, msg, System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("chats")
                .add(message);

        binding.back.setOnClickListener(view -> onBackPressed());

        binding.editMessage.setText("");
    }

    private void listenForMessages() {
        FirebaseFirestore.getInstance()
                .collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;

                    chatMessages.clear();

                    for (DocumentSnapshot doc : snapshots) {
//                        if (doc.getString("receiverId").equals(receiverId) && doc.getString("senderId").equals(senderId) ){
//
//                        }
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        chatMessages.add(msg);
                    }
                    chatAdapter.notifyDataSetChanged();
                    binding.recyclerChat.scrollToPosition(chatMessages.size() - 1);
                });
    }

//    private void listenForMessages() {
//        chatMessages.clear();
//
//        // Messages sent by current user
//        db.collection("chats")
//                .whereEqualTo("senderId", senderId)
//                .whereEqualTo("receiverId", receiverId)
//                .orderBy("timestamp", Query.Direction.ASCENDING)
//                .get()
//                .addOnSuccessListener(snapshot1 -> {
//
//                    for (DocumentSnapshot doc : snapshot1) {
//                        ChatMessage msg = doc.toObject(ChatMessage.class);
//                        chatMessages.add(msg);
//                    }
//
//                    // Messages received by current user
//                    db.collection("chats")
//                            .whereEqualTo("senderId", receiverId)
//                            .whereEqualTo("receiverId", senderId)
//                            .orderBy("timestamp", Query.Direction.ASCENDING)
//                            .get()
//                            .addOnSuccessListener(snapshot2 -> {
//
//                                for (DocumentSnapshot doc : snapshot2) {
//                                    ChatMessage msg = doc.toObject(ChatMessage.class);
//                                    chatMessages.add(msg);
//                                }
//
//                                // Sort all messages by timestamp after merging
//                                Collections.sort(chatMessages, Comparator.comparingLong(ChatMessage::getTimestamp));
//                                chatAdapter.notifyDataSetChanged();
//                                binding.recyclerChat.scrollToPosition(chatMessages.size() - 1);
//                            });
//                });
//    }


    @Override
    protected void onResume() {
        super.onResume();
        listenForMessages();
    }
}