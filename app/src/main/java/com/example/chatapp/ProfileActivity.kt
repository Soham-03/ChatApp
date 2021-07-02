package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    lateinit var txtUserName: TextView
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        txtUserName = findViewById(R.id.txtUserName)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = firebaseAuth.currentUser
        val uid: String = currentUser!!.uid
        db.collection("user").document(uid).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val documentSnapshot = it.result
                    if(documentSnapshot!!.exists()){
                        val name = documentSnapshot.get("name", String::class.java)
                        txtUserName.text = name
                    }
                }
            }
    }
}