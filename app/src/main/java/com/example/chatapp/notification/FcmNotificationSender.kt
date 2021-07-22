package com.example.chatapp.notification

import android.app.Activity
import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.Response
import org.json.JSONException
import org.json.JSONObject


class FcmNotificationSender(
    var userFcmToken: String?,
    var title: String?,
    var body: String?,
    var mContext: Context?,
    var mActivity: Activity?
) {


    private val postUrl = "https://fcm.googleapis.com/fcm/send"
    private val fcmServerKey = "AAAAtIel-CM:APA91bHEgFjUjOmFhyJDz91xmSjNpT0b3a3LgeSRTC9iQZKFVdrrWWCa509VHd5T12Z2P4tAPUHDMg0eWKUTZoslH24Y_ApsceS4tPLGjnXutPODva3ZSx8bPGjEqIlNc5xuSePb-R3O"

    fun sendNotifications() {
        val requestQueue = Volley.newRequestQueue(mActivity)
        val mainObj = JSONObject()
        try {
            mainObj.put("to", userFcmToken)
            val notiObject = JSONObject()
            notiObject.put("title", title)
            notiObject.put("body", body)
            notiObject.put("icon", "logo")
            mainObj.put("notification", notiObject)

            val listener =object: com.android.volley.Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {
                    println("Message sent: ${response?.toString()}")
                }
            }
            val errorListener = object : com.android.volley.Response.ErrorListener{
                override fun onErrorResponse(error: VolleyError?) {
                    println(error?.printStackTrace())
                }
            }

            val request: JsonObjectRequest = object : JsonObjectRequest(Method.POST, postUrl, mainObj,listener,errorListener){

                override fun getHeaders(): MutableMap<String, String> {

                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=$fcmServerKey"
                    return header

                }
            }
            requestQueue.add(request)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}