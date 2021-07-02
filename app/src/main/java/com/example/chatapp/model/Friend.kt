package com.example.chatapp.model

class Friend(idChatRoom:String){
    constructor() : this("")
    var idChatRoom = idChatRoom
    fun getChatRoom(): String {
        return idChatRoom
    }
    fun setChatRoom(id:String){
        this.idChatRoom = id
    }
}