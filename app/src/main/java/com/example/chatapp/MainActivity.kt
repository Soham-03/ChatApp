package com.example.chatapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.model.Friend
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
private lateinit var db: FirebaseFirestore
private lateinit var uid: String
class MainActivity() : AppCompatActivity() {
    lateinit var btnAdd: ImageView
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var authListner: FirebaseAuth.AuthStateListener
    lateinit var hamburger:ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: FirestoreRecyclerAdapter<Friend, FriendViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        btnAdd = findViewById(R.id.btnAdd)
        hamburger = findViewById(R.id.imgHamburger)
        val view = findViewById<RelativeLayout>(R.id.viewForMenu)
        hamburger.setOnClickListener {
            val popup = PopupMenu(this,view,Gravity.END)
            val inflater:MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu,popup.menu)
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menuProfile->{
                        val intent = Intent(this@MainActivity,ProfileActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.menuLogout->{
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@MainActivity,PhoneSignUpActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
                true
            }
            popup.show()
        }
        goMain()
    }
    private fun goMain() {
        
        val currentUser = firebaseAuth.currentUser
        uid = currentUser!!.uid

        recyclerView = findViewById(R.id.mainActivityRecyclerView)
        layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        val options: FirestoreRecyclerOptions<Friend> = FirestoreRecyclerOptions.Builder<Friend>()
            .setQuery(db.collection("user").document(uid).collection("friend"), Friend::class.java)
            .build()
        adapter = object : FirestoreRecyclerAdapter<Friend, FriendViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.friend_single_row, parent, false)
                return FriendViewHolder(view)
            }

            override fun onBindViewHolder(holder: FriendViewHolder, position: Int, model: Friend) {
                val userNameFriend = snapshots.getSnapshot(position).id
                holder.setList(userNameFriend)
                holder.itemView.setOnClickListener {
                    println(holder.txtName.text)
                    goChatRoom(model.getChatRoom(), userNameFriend)
                }
            }
        }

        recyclerView.adapter = adapter
        adapter.startListening()


        btnAdd.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setTitle("Enter UserName")
            dialog.setContentView(R.layout.dialog)
            dialog.show()
            val userName: TextView = dialog.findViewById(R.id.txtName)
            val btnAddFriend: Button = dialog.findViewById(R.id.btnAddFriend)
            btnAddFriend.setOnClickListener {
                val name = userName.text.toString().trim()
                if (TextUtils.isEmpty(name)) {
                    userName.error = "Please Enter username"
                } else {
                    db.collection("user").whereEqualTo("name", name).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                userName.setError("UserName not found")
                            } else {
                                for (docSnapShots in it.documents) {
                                    val uidFriend = docSnapShots.getId()
                                    checkFriendExist(uidFriend)
                                }
                            }
                        }
                }
            }
        }
    }
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val view: View = itemView
        lateinit var imgProfile: ImageView
        var txtName: TextView = view.findViewById(R.id.txtFriendsName)
        fun setList(userNameFriend: String) {
            db.collection("user").document(userNameFriend).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val docSnapShot = it.result
                    val name = docSnapShot!!.get("name")
                    txtName.text = name.toString()
                }
            }
        }
    }

    fun checkFriendExist(userNameFriend: Any?) {
        db.collection("user").document(uid).collection("friend").document().get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val docSnapShot = it.result
                    if (docSnapShot!!.exists()) {
                        val idChatRoom = docSnapShot.get("idChatRoom")
                        goChatRoom(idChatRoom as String, userNameFriend as String)
                    } else {
                        createNewChatRoom(userNameFriend)
                    }
                }
            }
    }

    private fun createNewChatRoom(userNameFriend: Any?) {
        val dataChatRoom = HashMap<String, Any>()
        val time = Calendar.getInstance().time
        val timeInString = SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.getDefault()).format(time)
        dataChatRoom.put("dateAdded",timeInString)
        db.collection("chatRoom").document(uid + userNameFriend).set(dataChatRoom)
            .addOnSuccessListener {
                val dataFriend = HashMap<String, Any>()
                dataFriend["idChatRoom"] = uid + userNameFriend
                db.collection("user").document(uid).collection("friend")
                    .document(userNameFriend as String).set(dataFriend)
                    .addOnSuccessListener {
                        //write user data
                        val dataUserFriend = HashMap<String, Any>()
                        dataUserFriend["idChatRoom"] = uid + userNameFriend
                        db.collection("user").document(uid).collection("friend")
                            .document(userNameFriend).set(dataFriend)
                            .addOnSuccessListener {
                                //write on users friend data
                                val dataUserFriend = HashMap<String, Any>()
                                dataUserFriend.put("idChatRoom", uid + userNameFriend)
                                db.collection("user").document(userNameFriend as String)
                                    .collection("friend").document(uid).set(dataUserFriend)
                                    .addOnSuccessListener {
                                        goChatRoom(uid + userNameFriend, userNameFriend)
                                    }
                            }
                    }
            }
    }

    private fun goChatRoom(idChatRoom: String, uidFriend: String) {
        val intent = Intent(this@MainActivity,ChatActivity::class.java)
        intent.putExtra("idChatRoom",idChatRoom)
        intent.putExtra("uidFriend",uidFriend)
        startActivity(intent)
    }
}

