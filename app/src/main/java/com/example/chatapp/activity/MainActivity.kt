

package com.example.chatapp.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.applic
import com.example.chatapp.model.Friend
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import org.w3c.dom.Text
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

lateinit var db: FirebaseFirestore
private lateinit var uid: String
lateinit var loadingLayout:RelativeLayout
lateinit var loadingLottie:LottieAnimationView
class MainActivity() : AppCompatActivity() {
    lateinit var btnAdd: ImageView
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var hamburger:ImageView
    lateinit var userNameFriend: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: FirestoreRecyclerAdapter<Friend, FriendViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnAdd = findViewById(R.id.btnAdd)
        hamburger = findViewById(R.id.imgHamburger)
        loadingLayout = findViewById(R.id.loadingLayout)
        loadingLottie = findViewById(R.id.loadingLottie)
        loadingLottie.visibility = View.GONE
        loadingLayout.visibility = View.GONE

        if(!applic.isInternetAvailable(this)){
            applic.noInternetDialog(this,"No Internet","You are not Connected to the Internet")
        }

        val currentUser = firebaseAuth.currentUser
        uid = currentUser!!.uid
        applic.online("true")
        val view = findViewById<RelativeLayout>(R.id.viewForMenu)
        val token = getSharedPreferences("chat_app", MODE_PRIVATE)
            .getString("fcm_token","").toString()
        db.collection("user").document(uid).update("token",token)
        db.collection("user").addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                adapter.notifyDataSetChanged()
            }
        })

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

    override fun onDestroy() {
        applic.online("false")
        println("Destroyed")
        super.onDestroy()
    }

    override fun onPause() {
        applic.online("false")
        println("Paused")
        super.onPause()
    }

    override fun onResume() {
        applic.online("true")
        println("Resumed")
        super.onResume()
    }
    private fun goMain() {

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
                userNameFriend = snapshots.getSnapshot(position).id
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
                    db.collection("user").whereEqualTo("username", name).get()
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
        val imgProfile :ImageView = view.findViewById(R.id.imgFriends)
        var txtName: TextView = view.findViewById(R.id.txtFriendsName)
        var imgOnline: ImageView = view.findViewById(R.id.imgOnline)
        val txtLastSeen:TextView = view.findViewById(R.id.txtLastSeen)
        fun setList(userNameFriend: String) {
            db.collection("user").document(userNameFriend).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val docSnapShot = it.result
                    val name = docSnapShot!!.get("name")
                    txtName.text = name.toString()
                    val profilePicture = docSnapShot.get("profilepicture",String::class.java)
                    val lastSeenDate = docSnapShot.get("lastSeen",String::class.java)
                    if(profilePicture != null){
                        Glide.with(view)
                            .load(profilePicture)
                            .centerCrop()
                            .into(imgProfile)
                    }
                    val isOnline = docSnapShot.get("online",String::class.java)
                    if(isOnline == null){
                        imgOnline.setImageResource(R.drawable.offline)
                    }
                    if(isOnline == "true"){
                        imgOnline.setImageResource(R.drawable.online)
                        txtLastSeen.text = ""
                    }
                    else{
                        imgOnline.setImageResource(R.drawable.offline)
                        val date = lastSeenDate?.dropLast(9)
                        val singleDate = date?.drop(8)
                        val year = date?.dropLast(6)
                        val time = Calendar.getInstance().time
                        val timeInString = SimpleDateFormat("yyyy/MM/dd",Locale.getDefault()).format(time)

                        val myDate = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
                        val dateFormat: DateFormat = SimpleDateFormat("dd")
                        val yestr = dateFormat.format(myDate)
                        val dropDate = lastSeenDate.toString().drop(11)
                        var displayDate = dropDate.dropLast(3)
                        val apOrPm = displayDate[0].toString()+ displayDate[1].toString()
                        val gg = apOrPm.toInt()
                        if(gg<12){
                            displayDate = "$displayDate am"
                        }
                        else{
                            displayDate = "$displayDate pm"
                        }
                        if(date == timeInString){
                            txtLastSeen.text = "Last Seen Today at $displayDate"
                        }
                        else if(date?.drop(8) == yestr){
                            txtLastSeen.text = "Last Seen Yesterday at $displayDate"
                        }
                        else{
                            var month = "gg"
                            when("${date!![5]}${date[6]}"){
                                "01"->{
                                    month = "January"
                                }
                                "02"->{
                                    month = "February"
                                }
                                "03"->{
                                    month = "March"
                                }
                                "04"->{
                                    month = "April"
                                }
                                "05"->{
                                    month = "June"
                                }
                                "06"->{
                                    month = "July"
                                }
                                "07"->{
                                    month = "October"
                                }
                                "08"->{
                                    month = "November"
                                }
                                "09"->{
                                    month = "December"
                                }
                                "10"->{
                                    month = "February"
                                }
                                "11"->{
                                    month = "February"
                                }
                                "12"->{
                                    month = "February"
                                }
                            }
                            txtLastSeen.text = ("Last Seen $singleDate $month $year")
                        }
                    }
                }
            }
            loadingLayout.visibility = View.GONE
            loadingLottie.cancelAnimation()
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
                                db.collection("user").document(userNameFriend)
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

