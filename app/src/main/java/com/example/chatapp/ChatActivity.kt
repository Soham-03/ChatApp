package com.example.chatapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.model.Chat
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

private lateinit var uid:String
class ChatActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSendMsg: ImageView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: FirestoreRecyclerAdapter<Chat,ChatViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser
        uid = firebaseAuth.uid!!

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        edtMessage = findViewById(R.id.edtMessage)
        btnSendMsg = findViewById(R.id.btnSendMsg)

        val idChatRoom = intent.extras?.getString("idChatRoom")
        val uidFriend = intent.extras?.getString("uidFriend")
        val color1 = resources.getColor(R.color.navyBlue,null)
        val color2 = resources.getColor(R.color.white,null)

        layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = false
        layoutManager.stackFromEnd = true

        chatRecyclerView.setHasFixedSize(true)
        chatRecyclerView.layoutManager = layoutManager

        val options: FirestoreRecyclerOptions<Chat> = FirestoreRecyclerOptions.Builder<Chat>()
            .setQuery(db.collection("chatRoom").document(idChatRoom!!).collection("chat").orderBy("date"),Chat::class.java)
            .build()

        adapter = object: FirestoreRecyclerAdapter<Chat, ChatViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_single_row,parent,false)
                return ChatViewHolder(view)
            }

            override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Chat) {
                holder.setList(model.uid!!, model.message!!,model.date,applicationContext,color1,color2)
                println("hello")
            }
        }
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                layoutManager.smoothScrollToPosition(chatRecyclerView,null,adapter.itemCount)
            }
        })

        chatRecyclerView.adapter = adapter
        adapter.startListening()

        btnSendMsg.setOnClickListener {
            val message = edtMessage.text.toString().trim()
            if(TextUtils.isEmpty(message)){
                edtMessage.error = "Message Empty"
            }
            else{
                val time = Calendar.getInstance().time
                val timeInString = SimpleDateFormat("HH:mm:ss MM/dd/yyyy",Locale.getDefault()).format(time)
                val dataMessage = HashMap<String,String>()
                dataMessage.put("date",timeInString)
                dataMessage.put("message",message)
                dataMessage.put("uid", uid)
                db.collection("chatRoom").document(idChatRoom).collection("chat").document().set(dataMessage)
                    .addOnSuccessListener {
                        edtMessage.text.clear()
                    }
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
            if(uidMessage == uid){
                val con = ConstraintSet()
                con.clone(chatLayout)
                con.setHorizontalBias(R.id.txtMessage,1.0f)
                con.setHorizontalBias(R.id.txtTime,1.0f)
                con.applyTo(chatLayout)
                txtMessage.setBackgroundResource(R.drawable.sender_bg)
                chatLayout.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                txtMessage.setTextColor(color1)
                txtMessage.text = msg
                txtDateAndTime.text = dateAndTime.toString()
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
                txtMessage.text = msg
                txtDateAndTime.text = dateAndTime.toString()
            }
        }
    }
}