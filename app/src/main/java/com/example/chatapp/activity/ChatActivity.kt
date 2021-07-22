package com.example.chatapp.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.applic
import com.example.chatapp.model.Chat
import com.example.chatapp.notification.FcmNotificationSender
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.scottyab.aescrypt.AESCrypt
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private lateinit var uid:String
private lateinit var encryptedMessage: String
private lateinit var decryptedMessage:String
private lateinit var uidFriend:String
private lateinit var snackbarLayout:RelativeLayout
class ChatActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSendMsg: ImageView
    private lateinit var friendName: TextView
    private lateinit var friendPfp: ImageView
    private lateinit var imgOnlineState: ImageView
    private lateinit var btnBack:ImageView
    private lateinit var txtTyping: TextView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: FirestoreRecyclerAdapter<Chat,RecyclerView.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser
        uid = firebaseAuth.uid!!
        applic.online("false")

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        edtMessage = findViewById(R.id.edtMessage)
        btnSendMsg = findViewById(R.id.btnSendMsg)
        friendName = findViewById(R.id.friendName)
        friendPfp = findViewById(R.id.imgFriendsPfp)
        imgOnlineState = findViewById(R.id.imgOnlineChatAct)
        btnBack = findViewById(R.id.btnBackChat)
        txtTyping = findViewById(R.id.txtTyping)
        snackbarLayout = findViewById<RelativeLayout>(R.id.snackBar)

        val idChatRoom = intent.extras?.getString("idChatRoom")
        uidFriend = intent.extras?.getString("uidFriend")!!
        val color1 = resources.getColor(R.color.navyBlue,null)
        val color2 = resources.getColor(R.color.white,null)

        layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = false
        layoutManager.stackFromEnd = true

        chatRecyclerView.setHasFixedSize(true)
        chatRecyclerView.layoutManager = layoutManager

        val arrayListOfDates: ArrayList<String> = ArrayList()

        db.collection("chatRoom").document(idChatRoom!!).collection("chat").get().addOnSuccessListener {
            for(document in it){
                val date = document.get("date").toString()
                val dateShown = date.dropLast(9)
                arrayListOfDates.add(dateShown)
            }

            val options: FirestoreRecyclerOptions<Chat> = FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(db.collection("chatRoom").document(idChatRoom).collection("chat").orderBy("date"),Chat::class.java)
                .build()

            adapter = object: FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>(options) {
                override fun getItemViewType(position: Int): Int {
                    if(position != 0){
                        val date = adapter.snapshots[position].date?.dropLast(9)
                        val datePrevious = adapter.snapshots[position-1].date?.dropLast(9)
                        if(date != datePrevious){
                            return 1
                        }
                        return 0
                    }
                    return 1
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    if(viewType == 0){
                        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_single_row,parent,false)
                        return ChatViewHolder(view)
                    }
                    else{
                        val view = LayoutInflater.from(parent.context).inflate(R.layout.chatlist_single_row,parent,false)
                        return viewHolder1(view)
                    }
                }

                override fun onBindViewHolder(
                    holder: RecyclerView.ViewHolder,
                    position: Int,
                    model: Chat
                ) {
                    if(holder.itemViewType == 0){
                        if(model.uid == uid){
                            decryptedMessage = decrypt(model.message!!, uid)
                        }
                        else{
                            decryptedMessage = decrypt(model.message!!, uidFriend!!)
                        }
                        val chatViewHolder = holder as ChatViewHolder
                        chatViewHolder.setList(model.uid!!, model.message!!,model.date,applicationContext,color1,color2)
                    }
                    else{
                        val chatViewHolder = holder as viewHolder1
                        val date = model.date?.dropLast(9)
                        val singleDate = date?.drop(8)
                        val year = date?.dropLast(6)
                        val time = Calendar.getInstance().time
                        val timeInString = SimpleDateFormat("yyyy/MM/dd",Locale.getDefault()).format(time)

                        val myDate = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
                        val dateFormat: DateFormat = SimpleDateFormat("dd")
                        val yestr = dateFormat.format(myDate)
                        if(date == timeInString){
                            chatViewHolder.textView.text = "Today"
                        }
                        else if(date?.drop(8) == yestr){
                            chatViewHolder.textView.text = "Yesterday"
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
                            chatViewHolder.textView.text = ("$singleDate $month $year")
                        }
                        if(model.uid == uid){
                            decryptedMessage = decrypt(model.message!!, uid)
                        }
                        else{
                            decryptedMessage = decrypt(model.message!!, uidFriend!!)
                        }
                        chatViewHolder.setList(model.uid!!, model.message!!,model.date,applicationContext,color1,color2)
                    }
                }
            }
            adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    layoutManager.smoothScrollToPosition(chatRecyclerView,null,adapter.itemCount)
                }
            })

            val swipeGesture = object : SwipeGesture(){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if(adapter.snapshots[viewHolder.adapterPosition].uid == uid){
                        if(direction == ItemTouchHelper.LEFT){
                            val pos = viewHolder.adapterPosition
                            val currentMsgId = adapter.snapshots[pos].messageId
                            val message = adapter.snapshots[pos].message
                            val date = adapter.snapshots[pos].date
                            val uid = adapter.snapshots[pos].uid
                            val decryptedMsg = decrypt(message!!,uid!!)
                            val encryptedMsg = encrypt(decryptedMsg,uid)
                            db.collection("chatRoom").document(idChatRoom).collection("chat")
                                .document(currentMsgId!!)
                                .delete()
                                .addOnSuccessListener {
                                    println("UID: $currentMsgId")
                                    adapter.notifyDataSetChanged()
                                }
                            Snackbar.make(this@ChatActivity,snackbarLayout,"Message Deleted",Snackbar.LENGTH_SHORT)
                                .setAction("UNDO"
                                ) { val dataMessage = HashMap<String,String>()
                                    dataMessage["date"] = date.toString()
                                    dataMessage["message"] = encryptedMsg
                                    dataMessage["uid"] = uid
                                    db.collection("chatRoom").document(idChatRoom).collection("chat").add(dataMessage)
                                        .addOnSuccessListener {
                                            db.collection("chatRoom").document(idChatRoom)
                                                .collection("chat").document(it.id)
                                                .update("messageId", it.id)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                .show()
                        }
                    }
                    else{
                        Toast.makeText(this@ChatActivity,"Cant delete",Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            val touchHelper = ItemTouchHelper(swipeGesture)
            touchHelper.attachToRecyclerView(chatRecyclerView)

            chatRecyclerView.adapter = adapter
            adapter.startListening()


        }
        db.collection("user").document(uidFriend!!).get().addOnSuccessListener {
            val name = it.get("name").toString()
            val pfp = it.get("profilepicture").toString()
            val isOnline = it.get("online",String::class.java)
            if(isOnline == null){
                imgOnlineState.setImageResource(R.drawable.offline)
            }
            if(isOnline == "true"){
                imgOnlineState.setImageResource(R.drawable.online)
            }
            else{
                imgOnlineState.setImageResource(R.drawable.offline)
            }
            friendName.text = name
            if(pfp != null){
                Glide.with(this)
                    .load(pfp)
                    .centerCrop()
                    .placeholder(R.drawable.friendspfp)
                    .into(friendPfp)
            }
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnSendMsg.setOnClickListener {
            if(!applic.isInternetAvailable(this)){
                applic.noInternetDialog(this,"No Internet","Can't send Message")
            }
            else{
                val message = edtMessage.text.toString().trim()
                if(TextUtils.isEmpty(message)){
                    edtMessage.error = "Message Empty"
                }
                else{
                    val timeLstSeen = Calendar.getInstance().time
                    val timeInString = SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(timeLstSeen)
                    applic.lastMsgDate(timeInString)
                    encryptedMessage = encrypt(message, uid)
                    val time = Calendar.getInstance().time
                    val timeInStr = SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(time)
                    val dataMessage = HashMap<String,String>()
                    dataMessage["date"] = timeInStr
                    dataMessage["message"] = encryptedMessage
                    dataMessage["uid"] = uid

                    db.collection("chatRoom").document(idChatRoom).collection("chat").add(dataMessage)
                    .addOnSuccessListener {
                        db.collection("chatRoom").document(idChatRoom).collection("chat").document(it.id).update("messageId",it.id)
                        edtMessage.text.clear()
                        db.collection("user").document(uidFriend!!).get().addOnSuccessListener {
                            val token = it.get("token").toString()
                            val name = it.get("name").toString()
                            val notificationSender = FcmNotificationSender(token,name,"You have a new Message",applicationContext,this)
                            notificationSender.sendNotifications()
                        }
                    }
                    .addOnFailureListener {
                        edtMessage.error = "Message Cannot be Sent"
                    }
                }
            }
        }

        edtMessage.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            var isTyping = false
            private var timer = Timer()
            private val DELAY: Long = 2000
            override fun afterTextChanged(s: Editable) {
                if (!isTyping) {
                    db.collection("user").document(uid).update("isTyping",true)
                    isTyping = true
                }
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            isTyping = false
                            db.collection("user").document(uid).update("isTyping",false)
                        }
                    },
                    DELAY
                )
            }
        })

        db.collection("user").document(uidFriend).addSnapshotListener { value, error ->
            val isTyping: Any? = value!!.get("isTyping")
            if(isTyping == null){
                txtTyping.visibility = View.GONE
            }
            if(isTyping == true){
                txtTyping.visibility = View.VISIBLE
            }
            else{
                txtTyping.visibility = View.GONE
            }
        }

    }

    override fun onPause() {
        super.onPause()
        println("paused")
        applic.inBackground = true
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Destroy")
        applic.online("false")
        applic.inBackground = true
    }

    override fun onStop() {
        super.onStop()
        println("Stop")
        applic.inBackground = true
    }

    override fun onResume() {
        super.onResume()
        println("Resume")
        applic.online("true")
        applic.inBackground = false
    }

    override fun onBackPressed() {
        applic.online("true")
        super.onBackPressed()
    }

    class viewHolder1(itemView:View): RecyclerView.ViewHolder(itemView) {
        val textView:TextView = itemView.findViewById(R.id.txtDay)
        val view = itemView
        private val txtMessage: TextView = view.findViewById(R.id.txtMessage)
        private val chatLayout:ConstraintLayout= view.findViewById(R.id.chatLayout)
        private val txtDateAndTime = view.findViewById<TextView>(R.id.txtTime)

        fun setList(
            uidMessage: String,
            msg: String,
            dateAndTime: String?,
            applicationContext: Context,
            color1: Int,
            color2: Int
        ){
            val dropDate = dateAndTime.toString().drop(11)
            var displayDate = dropDate.dropLast(3)
            val apOrPm = displayDate[0].toString()+ displayDate[1].toString()
            val gg = apOrPm.toInt()
            if(gg<12){
                displayDate = "$displayDate am"
            }
            else{
                displayDate = "$displayDate pm"
            }

            if(uidMessage == uid){
                val con = ConstraintSet()
                con.clone(chatLayout)
                con.setHorizontalBias(R.id.txtMessage,1.0f)
                con.setHorizontalBias(R.id.txtTime,1.0f)
                con.applyTo(chatLayout)
                txtMessage.setBackgroundResource(R.drawable.sender_bg)
                chatLayout.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                txtMessage.setTextColor(color1)
                txtMessage.text = decryptedMessage
                txtDateAndTime.text = displayDate

            }
            else{
                val con = ConstraintSet()
                con.clone(chatLayout)
                con.setHorizontalBias(R.id.txtMessage,0.0f)
                con.setHorizontalBias(R.id.txtTime,0.0f)
                con.applyTo(chatLayout)
                txtMessage.setBackgroundResource(R.drawable.receiver_bg)
                txtMessage.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                txtMessage.setTextColor(color2)
                txtMessage.text = decryptedMessage
                txtDateAndTime.text = displayDate
            }
        }
    }

    class ChatViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        val view = itemView
        private val txtMessage: TextView = view.findViewById(R.id.txtMessage)
        private val chatLayout:ConstraintLayout= view.findViewById(R.id.chatLayout)
        private val txtDateAndTime = view.findViewById<TextView>(R.id.txtTime)

        fun setList(
            uidMessage: String,
            msg: String,
            dateAndTime: String?,
            applicationContext: Context,
            color1: Int,
            color2: Int
        ){
            val dropDate = dateAndTime.toString().drop(11)
            val date1 = dateAndTime.toString().drop(5)
            var date2 = date1.dropLast(9)
            var displayDate = dropDate.dropLast(3)
            val apOrPm = displayDate[0].toString()+ displayDate[1].toString()
            val gg = apOrPm.toInt()
            if(gg<12){
                displayDate = "$displayDate am"
            }
            else{
                displayDate = "$displayDate pm"
            }

            if(uidMessage == uid){
                val con = ConstraintSet()
                con.clone(chatLayout)
                con.setHorizontalBias(R.id.txtMessage,1.0f)
                con.setHorizontalBias(R.id.txtTime,1.0f)
                con.applyTo(chatLayout)
                txtMessage.setBackgroundResource(R.drawable.sender_bg)
                chatLayout.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                txtMessage.setTextColor(color1)
                txtMessage.text = decryptedMessage
                txtDateAndTime.text = displayDate
            }
            else{
                val con = ConstraintSet()
                con.clone(chatLayout)
                con.setHorizontalBias(R.id.txtMessage,0.0f)
                con.setHorizontalBias(R.id.txtTime,0.0f)
                con.applyTo(chatLayout)
                txtMessage.setBackgroundResource(R.drawable.receiver_bg)
                txtMessage.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                txtMessage.setTextColor(color2)
                txtMessage.text = decryptedMessage
                txtDateAndTime.text = displayDate
            }
        }

    }


    fun encrypt(messgae: String, password: String): String {
        return AESCrypt.encrypt(password, messgae)
    }

    fun decrypt(messgae: String, password: String): String {
        return AESCrypt.decrypt(password, messgae)
    }



}