package com.MessagingApp.gossipsmessengerapp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import com.MessagingApp.gossipsmessengerapp.LatestMessagesActivity.Companion.currentUser
import com.MessagingApp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    val adapter= GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title="Select User"
        //val adapter= GroupAdapter<GroupieViewHolder>()
        //recyclerview_newmessage.adapter= adapter
        recyclerview_newmessage.adapter= adapter
        fetchUsers()

    }

    companion object{
        val USER_KEY ="USER_KEY"
    }


    private fun fetchFilteredUsers(s: String) {
        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{

            //snapshot consists data of each user
            override fun onDataChange(snapshot: DataSnapshot) {

                val nameSet= mutableSetOf<User>()
                snapshot.children.forEach{
                    Log.d("NewMessage",it.toString())
                    val user= it.getValue(User::class.java)
                    if(user!=null && user.username.lowercase().contains(s.lowercase()) && user.username!= currentUser!!.username )
                    {
                        nameSet.add(user)
                    }

                }

                adapter.clear()
                nameSet.forEach{
                    adapter.add(UserItem(it))
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem= item as UserItem
                    val intent= Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                }

                recyclerview_newmessage.addItemDecoration(
                    DividerItemDecoration(this@NewMessageActivity,
                        DividerItemDecoration.VERTICAL)
                )
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun fetchUsers() {
        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{

            //snapshot consists data of each user
            override fun onDataChange(snapshot: DataSnapshot) {

            snapshot.children.forEach{
                Log.d("NewMessage",it.toString())
                val user= it.getValue(User::class.java)
                if(user!=null && user.username!= currentUser!!.username)
                {
                    adapter.add(UserItem(user))
                }

            }

                adapter.setOnItemClickListener { item, view ->
                    val userItem= item as UserItem
                    val intent= Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                }

                recyclerview_newmessage.addItemDecoration(
                    DividerItemDecoration(this@NewMessageActivity,
                        DividerItemDecoration.VERTICAL)
                )
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.newmessages_menu,menu)

        if(menu!=null) {
            val SearchItem: MenuItem = menu.findItem(R.id.action_search)
            val searchView: SearchView = SearchItem.actionView as SearchView
            val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

            searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    adapter.clear()
                    if(p0!=null)
                    fetchFilteredUsers(p0)
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    adapter.clear()
                    if(p0!=null)
                        fetchFilteredUsers(p0)
                    return true
                }

            })
        }
        return super.onCreateOptionsMenu(menu)
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_textview_new_message.text=user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_new_message)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }


}
