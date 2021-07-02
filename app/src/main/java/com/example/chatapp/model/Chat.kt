package com.example.chatapp.model

import java.util.*

class Chat {
    var message: String? = null
    var uid: String? = null
    var date: String? = null

    constructor() {}
    constructor(message: String?, uid: String?, date: String?) {
        this.message = message
        this.uid = uid
        this.date = date
    }
}



