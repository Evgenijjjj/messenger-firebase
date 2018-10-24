package com.example.admin.messenger.views

import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import android.util.Log
import com.example.admin.messenger.R
import com.example.admin.messenger.models.ChatMessage
import com.example.admin.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.collection.LLRBNode
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import java.util.*


class LatestMessageRow(val chatMessage: ChatMessage, val context: Context): Item<ViewHolder>() {
    var chatPartnerUser: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        }
        else {
            chatPartnerId = chatMessage.fromId
        }
        val usersRef = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)

                if (user != null) {
                    chatPartnerUser = user
                    val targetImageView = viewHolder.itemView.circleImageView_latest_row
                    Picasso.get().load(user.profileImageURL).into(targetImageView)

                    viewHolder.itemView.username_latest_massage_row.text = user.username
                    viewHolder.itemView.latestmassage_row.text = chatMessage.text.toString()
                    Log.d("latest_mes","${chatMessage.text} "+  chatMessage.timestamp.toString())

                    if (Math.abs(Calendar.getInstance().timeInMillis / 1000 - chatMessage.timestamp) < 86400/2) {
                        viewHolder.itemView.time_latest_message_row.text = DateUtils.formatDateTime(
                                context, chatMessage.timestamp * 1000, DateUtils.FORMAT_SHOW_TIME)
                    }
                    else {
                        viewHolder.itemView.time_latest_message_row.text = DateUtils.formatDateTime(
                                context, chatMessage.timestamp * 1000, DateUtils.FORMAT_SHOW_DATE)
                    }

                    viewHolder.itemView.background.alpha = 150
                }
            }
        })
    }
}