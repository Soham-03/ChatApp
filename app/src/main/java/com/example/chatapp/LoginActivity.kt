package com.example.chatapp

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    lateinit var txtSignIn: TextView
    lateinit var btnSignUp: ImageView
    lateinit var executor: Executor
    lateinit var biometricPrompt: BiometricPrompt
    lateinit var biometricManager: BiometricManager
    lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo
    lateinit var authenticationCallback: BiometricPrompt.AuthenticationCallback
    lateinit var cancellationSignal: CancellationSignal
    lateinit var fingerprintAnim:LottieAnimationView
    lateinit var auth:FirebaseAuth
    lateinit var authStateListener: FirebaseAuth.AuthStateListener
    var isLoggegIn:Boolean = false

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtSignIn = findViewById(R.id.txtSignIn)
        btnSignUp = findViewById(R.id.btnSignUp)
        fingerprintAnim = findViewById(R.id.fingerprintAnim)
        auth = FirebaseAuth.getInstance()
//        txtSignIn.visibility = View.INVISIBLE
//        if(!fingerprintAnim.isAnimating){
//            txtSignIn.visibility = View.VISIBLE
//            fingerprintAnim.playAnimation()
//        }
//        else{
//            fingerprintAnim.pauseAnimation()
//        }
        checkBiometricSupport()
        authStateListener = FirebaseAuth.AuthStateListener { p0 ->
            if(p0.currentUser == null){
                val intent = Intent(this@LoginActivity,PhoneSignUpActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        auth.addAuthStateListener(authStateListener)

        authenticationCallback = object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                val intent = Intent(this@LoginActivity,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@LoginActivity,"Authentication Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@LoginActivity,"Error BioMetric",Toast.LENGTH_SHORT).show()
            }
        }
        txtSignIn.setOnClickListener {
            biometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Authentication")
                .setSubtitle("Login using FingerPrint")
                .setNegativeButton("Cancel",this.mainExecutor, {
                        dialog, which ->
                    Toast.makeText(this@LoginActivity, "Authentication Cancelled", Toast.LENGTH_SHORT).show()
                })
                .build()
            biometricPrompt.authenticate(getCancelSignal(),mainExecutor,authenticationCallback)
        }
        btnSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity,PhoneSignUpActivity::class.java)
            startActivity(intent)
        }

    }

    fun checkBiometricSupport(): Boolean {
        val keyGuardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(!keyGuardManager.isDeviceSecure){
            Toast.makeText(this@LoginActivity,"Fingerprint is not enabled in settings",Toast.LENGTH_SHORT).show()
            return false
        }
        if(ActivityCompat.checkSelfPermission(this@LoginActivity,android.Manifest.permission.USE_BIOMETRIC)!=PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this@LoginActivity,"Please allow the app to use fingerprint authentication",Toast.LENGTH_SHORT).show()
            return false
        }
        return if(packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        }else true
    }
    fun getCancelSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal.setOnCancelListener {
            Toast.makeText(this@LoginActivity,"User cancelled Authentication",Toast.LENGTH_SHORT).show()
        }
        return cancellationSignal
    }
}