package com.example.booklink.model

class Comment() {
    var userId: String = ""
    var username: String = ""
    var text: String = ""
    var timestamp: Long = 0L

    constructor(userId: String, username: String, text: String, timestamp: Long) : this() {
        this.userId = userId
        this.username = username
        this.text = text
        this.timestamp = timestamp
    }
}
