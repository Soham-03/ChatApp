package com.example.chatapp.activity

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.applic
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileActivity : AppCompatActivity() {
    lateinit var txtName: TextView
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var imgProfilePic: ImageView
    lateinit var storageReference:StorageReference
    lateinit var imageUri:Uri
    lateinit var uid:String
    lateinit var txtUserName: TextView
    lateinit var txtPhoneNumber: TextView
    lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        txtName = findViewById(R.id.txtName)
        txtUserName = findViewById(R.id.txtUserName)
        imgProfilePic = findViewById(R.id.imgMyProfilePic)
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber)
        btnBack = findViewById(R.id.btnBack)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser
        uid = currentUser!!.uid
        applic.online("true")
        storageReference = FirebaseStorage.getInstance().reference

        db.collection("user").document(uid).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val documentSnapshot = it.result
                    if (documentSnapshot!!.exists()) {
                        val name = documentSnapshot.get("name", String::class.java)
                        val profilePicture = documentSnapshot.get("profilepicture",String::class.java)
                        val userName = documentSnapshot.get("username",String::class.java)
                        val phoneNumber = documentSnapshot.get("phonenumber",String::class.java)
                        txtName.text = name
                        txtUserName.text = userName
                        txtPhoneNumber.text = phoneNumber
                        if(profilePicture != null){
                            Glide.with(this)
                                .load(profilePicture)
                                .centerCrop()
                                .placeholder(R.drawable.pfp)
                                .into(imgProfilePic)
                        }
                    }
                }
            }
        btnBack.setOnClickListener {
            onBackPressed()
        }
        imgProfilePic.setOnClickListener {
            ImagePicker.with(this)
                .compress(1024)//Final image size will be less than 1 MB(Optional)
                .crop(1f,1f)
                .maxResultSize(800, 800)  //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
    }

    override fun onDestroy() {
        applic.online("false")
        super.onDestroy()
    }

    override fun onPause() {
        applic.online("false")
        super.onPause()
    }

    override fun onResume() {
        applic.online("true")
        super.onResume()
    }
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data!!
                imageUri = fileUri
                uploadImage()
            }
            else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    private fun uploadImage(){
        val filePath = storageReference.child("profilepicture").child(uid)
        filePath.putFile(imageUri).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {
                db.collection("user").document(uid).update("profilepicture",it.toString())
                    .addOnSuccessListener {
                        Toast.makeText(this@ProfileActivity,"Profile Pic Uploaded",Toast.LENGTH_SHORT).show()
                        this.recreate()
                    }
            }
        }
    }
}