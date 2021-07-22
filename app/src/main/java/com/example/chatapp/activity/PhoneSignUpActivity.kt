package com.example.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.chatapp.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt
import java.util.*
import java.util.concurrent.TimeUnit

class PhoneSignUpActivity : AppCompatActivity() {
    lateinit var entPhoneNumber:EditText
    lateinit var btnVerify:ImageView
    lateinit var entCode1:EditText
    lateinit var entCode2:EditText
    lateinit var entCode3:EditText
    lateinit var entCode4:EditText
    lateinit var entCode5:EditText
    lateinit var entCode6:EditText
    lateinit var btnLogin:ImageView
    lateinit var btnLoginLayout: RelativeLayout
    lateinit var entName:EditText
    lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var verificationId: String
    lateinit var auth:FirebaseAuth
    lateinit var loading:LottieAnimationView
    lateinit var db:FirebaseFirestore
    lateinit var txtVerify: TextView
    lateinit var layout1:RelativeLayout
    lateinit var layout2: RelativeLayout
    lateinit var edtPassword: EditText

    var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_login_activity)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        entPhoneNumber = findViewById(R.id.entPhone)
        btnVerify = findViewById(R.id.btnVerify)
        entCode1 = findViewById(R.id.entCode1)
        entCode2 = findViewById(R.id.entCode2)
        entCode3 = findViewById(R.id.entCode3)
        entCode4 = findViewById(R.id.entCode4)
        entCode5 = findViewById(R.id.entCode5)
        entCode6 = findViewById(R.id.entCode6)
        btnLogin = findViewById(R.id.btnlogin)
        loading = findViewById(R.id.loading)
        btnLoginLayout = findViewById(R.id.btnLoginLayout)
        entName = findViewById(R.id.entName)
        txtVerify = findViewById(R.id.txtVerify)
        layout1 = findViewById(R.id.layout1)
        layout2 = findViewById(R.id.layout2)
        edtPassword = findViewById(R.id.edtSetUpPassword)

        layout2.visibility = View.GONE
        
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                signIn(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(this@PhoneSignUpActivity, "Verification Failed", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                verificationId = p0
                forceResendingToken = p1
                loading.visibility = View.GONE
                loading.cancelAnimation()
                layout2.visibility = View.VISIBLE
            }

        }
        btnVerify.setOnClickListener {
            db.collection("user").whereEqualTo("phonenumber", entPhoneNumber.text.toString().trim()).get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        val phone = "+91"+entPhoneNumber.text.toString().trim()
                        startVerification(phone)
                        layout1.visibility = View.GONE
                        loading.visibility = View.VISIBLE
                        loading.playAnimation()
                    } else {
                        Toast.makeText(this,"Account Already Exists",Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnLogin.setOnClickListener {
            val code = entCode1.text.toString().trim()+entCode2.text.toString().trim()+entCode3.text.toString().trim()+entCode4.text.toString().trim()+entCode5.text.toString().trim()+entCode6.text.toString().trim()
            verifyNumberWithCode(verificationId, code)
        }

        changeFocus()

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
        val phoneNumber = entPhoneNumber.text.toString().trim()
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val currentUser: FirebaseUser? = auth.currentUser
                val uid: String = currentUser!!.uid
                val dataUser:HashMap<String, Any> = object: HashMap<String, Any>(){}
                val userName = createUserName(name)
                val password = edtPassword.text.toString().trim()
                val pass = AESCrypt.encrypt(uid,password)
                dataUser["name"] = name
                dataUser["username"] = userName
                dataUser["phonenumber"] = phoneNumber
                dataUser["isTyping"] = false
                dataUser["password"] = pass
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
    private fun createUserName(name:String): String {
        val random = Random()
        var randomString = name
        while (randomString.length < 10) {
            val randomChar = random.nextInt(123)
            randomString += if (Character.isLetterOrDigit(randomChar)) randomChar else ""
        }
        return randomString
    }

    private fun changeFocus(){
        entCode1.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(entCode1.text.length == 1){
                    println("textChanged")
                    entCode2.requestFocus()
                }
            }

        })
        entCode2.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(entCode2.text.length == 1){
                    println("textChanged")
                    entCode3.requestFocus()
                }
            }

        })
        entCode3.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(entCode3.text.length == 1){
                    println("textChanged")
                    entCode4.requestFocus()
                }
            }

        })
        entCode4.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(entCode4.text.length == 1){
                    println("textChanged")
                    entCode5.requestFocus()
                }
            }

        })
        entCode5.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(entCode5.text.length == 1){
                    println("textChanged")
                    entCode6.requestFocus()
                }
            }

        })
    }
}