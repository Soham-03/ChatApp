package com.example.chatapp
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Gravity
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

object applic {
    var inBackground = false
    var hasUnreadMsg = false
    fun online(trueOrFalse:String){
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser!!.uid
        db.collection("user").document(uid).update("online",trueOrFalse)
    }
    fun noInternetDialog(context:Context,Title:String,Msg:String){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.no_internet_dialog)
        val title:TextView = dialog.findViewById(R.id.Title)
        val msg:TextView = dialog.findViewById(R.id.Message)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.TOP)
        title.text = Title
        msg.text = Msg
        dialog.show()
    }
    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }
    fun lastMsgDate(date:String){
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser!!.uid
        db.collection("user").document(uid).update("lastSeen",date)
    }
}