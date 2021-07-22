package com.example.chatapp.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.chatapp.EnterPassword
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    lateinit var txtSignIn: TextView
    lateinit var btnSignUp: ImageView
    lateinit var biometricPrompt: BiometricPrompt
    lateinit var authenticationCallback: BiometricPrompt.AuthenticationCallback
    lateinit var cancellationSignal: CancellationSignal
    lateinit var auth:FirebaseAuth
    lateinit var authStateListener: FirebaseAuth.AuthStateListener
    lateinit var txtSignInWithPass : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtSignIn = findViewById(R.id.txtSignIn)
        btnSignUp = findViewById(R.id.btnSignUp)
        txtSignInWithPass = findViewById(R.id.txtSignInWithPass)
        auth = FirebaseAuth.getInstance()
        checkBiometricSupport()
        authStateListener = FirebaseAuth.AuthStateListener { p0 ->
            if(p0.currentUser == null){
                val intent = Intent(this@LoginActivity, PhoneSignUpActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        auth.addAuthStateListener(authStateListener)

        authenticationCallback = object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
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

        txtSignInWithPass.setOnClickListener {
            val fragment: Fragment = EnterPassword()
            supportFragmentManager.beginTransaction()
                .replace(R.id.loginActivityLayout, fragment,fragment::class.java.simpleName)
                .addToBackStack("EnterPassword")
                .commit()
        }

        btnSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, PhoneSignUpActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onBackPressed() {
        val mFragmentManager = this.supportFragmentManager
        if (mFragmentManager.backStackEntryCount > 0){
            mFragmentManager.popBackStackImmediate();
        }
        else
            super.onBackPressed();
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