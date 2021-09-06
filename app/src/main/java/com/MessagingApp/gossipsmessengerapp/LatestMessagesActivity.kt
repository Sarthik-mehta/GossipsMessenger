package com.MessagingApp.gossipsmessengerapp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import com.MessagingApp.gossipsmessengerapp.NewMessageActivity.Companion.USER_KEY
import com.MessagingApp.models.ChatMessage
import com.MessagingApp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: User?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        supportActionBar?.title="Gossips"

        recyclerview_latest_messages.adapter=adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        //set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->


            val intent=Intent(this,ChatLogActivity::class.java)

            val row= item as LatestMessageRow
            val myRef= FirebaseDatabase.getInstance().getReference("/latest-messages/${currentUser!!.uid}")

            myRef.child("${row.chatPartnerUser!!.uid}").child("messageStatus").setValue("seen")

            intent.putExtra(USER_KEY,row.chatPartnerUser)
            startActivity(intent)

        }

        newmessage_actionbutton.setOnClickListener {
            val intent= Intent(this,NewMessageActivity::class.java)
            startActivity(intent)
        }

        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()

    }
    val adapter= GroupAdapter<GroupieViewHolder>()
    val latestMessagesMap= HashMap<String,ChatMessage>()
    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId= FirebaseAuth.getInstance().uid
        val ref= FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage= snapshot.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!]= chatMessage
                refreshRecyclerViewMessages()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage= snapshot.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!]= chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){

        var chatPartnerUser: User?=null

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.message_textview_latest_message.text=chatMessage.text
            val chatPartnerId: String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid)
            {
                chatPartnerId=chatMessage.toId
            }
            else{
                chatPartnerId=chatMessage.fromId
            }
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid)
            {
                viewHolder.itemView.messageStatus.text="sent"
                viewHolder.itemView.messageStatus.setTextColor(Color.parseColor("#C1C1C1"))
            }
            else
            {
                if (chatMessage.messageStatus=="seen")
                {
                    viewHolder.itemView.messageStatus.text="seen"
                    viewHolder.itemView.messageStatus.setTextColor(Color.parseColor("#C1C1C1"))
                }
            }


            val ref= FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser= snapshot.getValue(User::class.java)
                    viewHolder.itemView.username_textview_latest_message.text=chatPartnerUser?.username
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.imageview_latest_message)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

    }

    private fun fetchCurrentUser() {
        val uid= FirebaseAuth.getInstance().uid
        val ref= FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser= snapshot.getValue(User::class.java)
                Log.d("LatestMessageActivity","current user: ${currentUser?.username}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("LatestMessageActivity","failed to get current user")
            }

        })
    }

    // to perform check whether the user is signed in or not
    private fun verifyUserIsLoggedIn() {
        val uid= FirebaseAuth.getInstance().uid
        if(uid==null)
        {
            val intent= Intent(this,LoginActivity::class.java)
            intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //when you press back button, this will not take you to register activity page, it will take you out of the app instead
            startActivity(intent)
        }
    }

    //to make the top bar menu buttons work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // switch case between menu options
        when(item?.itemId)
        {

            R.id.menu_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent= Intent(this,LoginActivity::class.java)
                intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //when you press back button, this will not take you to register activity page, it will take you out of the app instead
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchFilteredUsers(s: String){
        adapter.clear()
        latestMessagesMap.values.forEach{
            if(it.toName.lowercase().contains(s.lowercase())|| it.fromName.lowercase().contains(s.lowercase()))
            {
                adapter.add(LatestMessageRow(it))
            }
            else
            {
                Log.d("lmcu","fromName: ${it.fromName} and toName: ${it.toName}")

            }
        }
    }

    //to set the top bar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)

        if(menu!=null) {
            val SearchItem: MenuItem = menu.findItem(R.id.action_search)
            val searchView: SearchView = SearchItem.actionView as SearchView
            val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

            searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    if(p0!=null)
                        fetchFilteredUsers(p0)
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    if(p0!=null)
                        fetchFilteredUsers(p0)
                    return true
                }

            })
        }

        return super.onCreateOptionsMenu(menu)
    }

}