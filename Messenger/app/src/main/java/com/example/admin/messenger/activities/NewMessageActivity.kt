package com.example.admin.messenger.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import com.example.admin.messenger.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.admin.messenger.R


class NewMessageActivity : AppCompatActivity() {

    private var adapter: GroupAdapter<ViewHolder>? = null
    private var listOfUsers: ArrayList<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        val mActionBarToolbar = findViewById<View>(R.id.toolbar_actionbar) as Toolbar
        mActionBarToolbar.title = ""
        mActionBarToolbar.background?.alpha = 150
        setSupportActionBar(mActionBarToolbar)
        invalidateOptionsMenu()

        adapter = GroupAdapter<ViewHolder>()
        listOfUsers = ArrayList<User>()

        uploadUsersFromFirebaseDatabase()
    }

    private fun uploadUsersFromFirebaseDatabase() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null) {
                        listOfUsers?.add(user)
                        adapter?.add(UserItem(user))
                    }
                }

                adapter?.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val i = Intent(view.context, ChatActivity::class.java)
                    i.putExtra("USER", userItem.user)
                    startActivity(i)
                    finish()
                }

                recyclerview_newmessage.adapter = adapter
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_logout)?.isVisible = false
        menu?.findItem(R.id.action_new_message)?.isVisible = false

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        try {
            val searchMenuItem = menu?.findItem(R.id.action_search)
                    ?: return super.onCreateOptionsMenu(menu)
            val searchView = searchMenuItem.actionView as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    refreshRecycleView(newText)
                    return false
                }
            })

        } catch (e:Exception) {
            Log.d("New_MessageActivity", e.toString())
        }
        return super.onCreateOptionsMenu(menu)
    }

    private  fun refreshRecycleView(str: String?) {
        if (str == null) return

        if (str.isEmpty()) {
            adapter?.clear()
            listOfUsers?.forEachIndexed { index, user -> adapter?.add(UserItem(user)) }

            adapter?.notifyDataSetChanged()
            return
        }

        adapter?.clear()
        listOfUsers?.forEachIndexed { index, user ->
            if (user.username.contains(str))
                adapter?.add(UserItem(user))
        }

        adapter?.notifyDataSetChanged()
    }
}

class UserItem(val user: User): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_newmassage.text = user.username.toString()

        Picasso.get().load(user.profileImageURL).into(viewHolder.itemView.user_image_newmessage)
    }
}
