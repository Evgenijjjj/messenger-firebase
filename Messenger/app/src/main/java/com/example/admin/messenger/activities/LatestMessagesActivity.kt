package com.example.admin.messenger.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.admin.messenger.models.ChatMessage
import com.example.admin.messenger.models.User
import com.example.admin.messenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import com.example.admin.messenger.R
import com.google.firebase.iid.FirebaseInstanceId
import android.support.v4.os.HandlerCompat.postDelayed
import android.support.v7.widget.RecyclerView
import android.text.style.UpdateAppearance
import android.view.animation.AnimationUtils
import com.example.admin.messenger.services.UpdateTokenService
import com.yalantis.phoenix.PullToRefreshView




class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    private var adapter: GroupAdapter<ViewHolder>? = null
    private var listChatMessage: ArrayList<ChatMessage>? = null// cmp time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        val mActionBarToolbar = findViewById<View>(R.id.toolbar_actionbar) as Toolbar
        mActionBarToolbar.title = ""
        mActionBarToolbar.background.alpha = 100
        setSupportActionBar(mActionBarToolbar)

        adapter = GroupAdapter<ViewHolder>()

        fetchCurrentUser()
        verifyUserIsLoggedIn()

        recyclerview_latest_messages.adapter = adapter

        adapter!!.setOnItemClickListener { item, view ->
            val row = item as LatestMessageRow
            val i = Intent(this, ChatActivity::class.java)
            i.putExtra("USER", row.chatPartnerUser)
            startActivity(i)
            overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom)
        }

        pull_to_refresh.setOnRefreshListener {
            adapter?.clear()
            adapter?.notifyDataSetChanged()

            listenForLatestMessages()
            pull_to_refresh.postDelayed({
                pull_to_refresh.setRefreshing(false)
            }
                    ,2000)
        }
    }

    private fun refreshAdapter() {
        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
        listenForLatestMessages()
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        Log.d("LatestMessagesActivity", "fromId: $fromId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.children.last().getValue(ChatMessage::class.java) ?: return
                adapter?.add(LatestMessageRow(chatMessage,this@LatestMessagesActivity))
            }
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessagesActivity", "current username: ${currentUser?.username}")
                username_latest_messages.text = currentUser?.username
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            goToRegisterActivity()
        }
        else {
            val messagingToken = FirebaseInstanceId.getInstance().token
            if (messagingToken != null)
                refreshTokenInDB(messagingToken)
        }
    }

    private fun goToRegisterActivity() {
        val i = Intent(this, RegisterActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_new_message) {
            startActivity(Intent(this, NewMessageActivity::class.java))
        }

        if (item?.itemId == R.id.action_logout) {
            startActivity(Intent(this, RegisterActivity::class.java))
            this@LatestMessagesActivity.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_search)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    private fun refreshTokenInDB(token: String) {
        Log.d("TOKEN_update", token)
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("/users/")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("messagingToken")
                .setValue(token)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStart() {
        stars_latest_messages.onStart()
        refreshAdapter()
        runLayoutAnimation(recyclerview_latest_messages)
        super.onStart()
    }

    override fun onStop() {
        stars_latest_messages.onStop()
        super.onStop()
    }

    private fun runLayoutAnimation(recyclerView: RecyclerView) {
        val context: Context = recyclerView.context
        val controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)

        recyclerView.layoutAnimation = controller
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }
}
