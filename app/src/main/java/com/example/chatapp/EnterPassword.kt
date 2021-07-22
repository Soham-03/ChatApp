package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import com.example.chatapp.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt

class EnterPassword : Fragment() {
    lateinit var edtEnterPassword:EditText
    lateinit var btnLogin: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_password, container, false)
        btnLogin = view.findViewById(R.id.btnLoginWithPassword)
        edtEnterPassword = view.findViewById(R.id.edtEnterPassword)
        btnLogin.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            val uid = currentUser!!.uid
            val db = FirebaseFirestore.getInstance()
            db.collection("user").document(uid).get().addOnSuccessListener {
                val password = it.get("password")
                val decryptedPass = AESCrypt.decrypt(uid, password.toString())
                if(edtEnterPassword.text.toString().trim() == decryptedPass){
                    println("pass match")
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else{
                    edtEnterPassword.error = "Invalid Password"
                }
            }
        }
        return view
    }
}