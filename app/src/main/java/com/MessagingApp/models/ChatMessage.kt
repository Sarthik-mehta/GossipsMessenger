package com.MessagingApp.models

class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long, val fromName: String, val toName: String,
                  var messageStatus: String)
{
    constructor(): this("","","","",-1,"unknown","unknown","unseen")
}