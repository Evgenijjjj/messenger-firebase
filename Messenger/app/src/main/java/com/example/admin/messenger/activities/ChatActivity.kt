package com.example.admin.messenger.activities

import android.app.Notification
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.admin.messenger.R
import com.example.admin.messenger.models.ChatMessage
import com.example.admin.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.util.*

class ChatActivity : AppCompatActivity() {
    private val adapter = GroupAdapter<ViewHolder>()
    private var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toUser = intent.getParcelableExtra<User>("USER")

        send_button_chat.background.alpha = 100

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        listenForMessages()

        recyclerview_chat.adapter = adapter

        send_button_chat.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val toId = toUser?.uid
        val fromId = LatestMessagesActivity.currentUser?.uid
        if (toId == null || fromId == null) return
        val fromRef = FirebaseDatabase.getInstance().getReference("/user-to-user-messages/$fromId/$toId/")

        fromRef.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage, LatestMessagesActivity.currentUser!!, this@ChatActivity))
                    }
                    else {
                        adapter.add(ChatToItem(chatMessage, toUser!!, this@ChatActivity))
                    }
                }
                else { }

                recyclerview_chat.scrollToPosition(adapter.itemCount -1)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun performSendMessage() {
        val text = editText_chat.text.toString()
        editText_chat.text.clear()

        if (text.isEmpty()) {
            Toast.makeText(this, "Filed is Empty", Toast.LENGTH_SHORT).show()
            return
        }

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>("USER")
        val toId = user.uid

        val fromRef = FirebaseDatabase.getInstance().getReference("/user-to-user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-to-user-messages/$toId/$fromId").push()

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId").push()
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId").push()

        val notificationRef = FirebaseDatabase.getInstance().getReference("/Notifications/$toId").push()

        //val notification = Notification("text", "jbjbj")

        val chatMessage = ChatMessage(fromRef.key!!, text, fromId!!, toId, System.currentTimeMillis() / 1000)
        fromRef.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d("ChatActivity", "Successful sending message to Firebase database, key: ${fromRef.key}")
                    recyclerview_chat.scrollToPosition(adapter.itemCount -1)
                }
            toRef.setValue(chatMessage)

        latestMessageRef.setValue(chatMessage)
                .addOnFailureListener {
                    Log.d("ChatActivity", "Error sending message to latest database, key: ${it.message}")
                }
        latestMessageToRef.setValue(chatMessage)

        notificationRef.setValue(chatMessage)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top)
    }

    override fun onStart() {
        super.onStart()
        stars_chat_activity.onStart()
    }

    override fun onStop() {
        super.onStop()
        stars_chat_activity.onStop()
    }
}

class ChatFromItem(private val chatMessage: ChatMessage, private val user: User, private val ctx: Context): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = chatMessage.text
        viewHolder.itemView.frame_from_row.background.alpha = 125

        if (Math.abs(Calendar.getInstance().timeInMillis / 1000 - chatMessage.timestamp) < 86400/2) {
            viewHolder.itemView.time_from_row.text = DateUtils.formatDateTime(
                    ctx, chatMessage.timestamp * 1000, DateUtils.FORMAT_SHOW_TIME)
        }
        else {
            viewHolder.itemView.time_from_row.text = DateUtils.formatDateTime(
                    ctx, chatMessage.timestamp * 1000, DateUtils.FORMAT_SHOW_DATE)
        }

        val uri = user.profileImageURL
        val targetImageView = viewHolder.itemView.image_from_row

        Picasso.get().load(uri).into(targetImageView)
    }
}

class ChatToItem(private val chatMessage: ChatMessage, private val user: User, private val ctx: Context): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text = chatMessage.text
        viewHolder.itemView.frame_to_row.background.alpha = 125

        if (Math.abs(Calendar.getInstance().timeInMillis / 1000 - chatMessage.timestamp) < 86400/2) {
            viewHolder.itemView.time_to_row.text = DateUtils.formatDateTime(
                    ctx, chatMessage.timestamp * 1000, DateUtils.FORMAT_SHOW_TIME)
        }
        else {
            viewHolder.itemView.time_to_row.text = DateUtils.formatDateTime(
                    ctx, chatMessage.timestamp * 1000, DateUtils.FORMAT_SHOW_DATE)
        }

        val uri = user.profileImageURL
        val targetImageView = viewHolder.itemView.image_to_row

        Picasso.get().load(uri).into(targetImageView)
    }
}
