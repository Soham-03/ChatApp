package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class PhoneSignUpActivity : AppCompatActivity() {
    lateinit var entPhoneNumber:EditText
    lateinit var btnVerify:ImageView
    lateinit var entCode:EditText
    lateinit var btnLogin:ImageView
    lateinit var btnLoginLayout: RelativeLayout
    lateinit var entName:EditText
    lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var verificationId: String
    lateinit var auth:FirebaseAuth

    lateinit var loading:LottieAnimationView
    lateinit var db:FirebaseFirestore
    lateinit var txtVerify: TextView

    var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_login_activity)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        entPhoneNumber = findViewById(R.id.entPhone)
        btnVerify = findViewById(R.id.btnVerify)
        entCode = findViewById(R.id.entCode)
        btnLogin = findViewById(R.id.btnlogin)
        loading = findViewById(R.id.loading)
        btnLoginLayout = findViewById(R.id.btnLoginLayout)
        entName = findViewById(R.id.entName)
        txtVerify = findViewById(R.id.txtVerify)

        entCode.visibility = View.GONE
        btnLoginLayout.visibility = View.GONE

        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                signIn(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(this@PhoneSignUpActivity,"Verification Failed",Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                verificationId = p0
                forceResendingToken = p1
                loading.visibility = View.GONE
                loading.cancelAnimation()
                entCode.visibility = View.VISIBLE
                btnLoginLayout.visibility = View.VISIBLE
            }

        }
        btnVerify.setOnClickListener {
            val phone = entPhoneNumber.text.toString().trim()
            startVerification(phone)
            loading.playAnimation()
        }

        btnLogin.setOnClickListener {
            val code = entCode.text.toString().trim()
            verifyNumberWithCode(verificationId, code)
        }

    }
    private fun startVerification(phone:String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

//    private fun resendVerificationCode(phone:String,token:PhoneAuthProvider.ForceResendingToken?){
//        val options = token?.let {
//            PhoneAuthOptions.newBuilder(auth)
//                .setPhoneNumber(phone)
//                .setTimeout(60L,TimeUnit.SECONDS)
//                .setActivity(this)
//                .setCallbacks(callback)
//                .setForceResendingToken(it)
//                .build()
//        }
//        if (options != null) {
//            PhoneAuthProvider.verifyPhoneNumber(options)
//        }
//    }

    private fun verifyNumberWithCode(verificationId:String,code:String){
        val credential = PhoneAuthProvider.getCredential(verificationId,code)
        signIn(credential)
    }

    private fun signIn(credential: PhoneAuthCredential) {
        val name = entName.text.toString().trim()
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val currentUser: FirebaseUser? = auth.currentUser
                val uid: String = currentUser!!.uid
                val dataUser:HashMap<String, Any> = object: HashMap<String, Any>(){}
                dataUser["name"] = name
                db.collection("user").document(uid).set(dataUser)
                    .addOnCompleteListener {
                        Toast.makeText(this,"Logged In",Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PhoneSignUpActivity,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Registration Failed",Toast.LENGTH_SHORT).show()
                    }
            }
    }
}