package com.example.chatapp.model

import java.util.*

class Chat {

    var message: String? = null
    var uid: String? = null
    var date: String? = null
    var messageId:String? = null

    constructor() {}
    constructor(message: String?, uid: String?, date: String?,msgId:String?) {
        this.message = message
        this.uid = uid
        this.date = date
        this.messageId = msgId
    }
}



