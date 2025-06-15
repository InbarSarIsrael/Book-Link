package com.example.booklink.model

import com.google.firebase.database.*

object CommentManager {

    private val database = FirebaseDatabase.getInstance().reference

    fun fetchComments(bookId: String, callback: (List<Comment>) -> Unit) {
        val commentsRef = database.child("comments").child(bookId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()
                for (commentSnap in snapshot.children) {
                    val comment = commentSnap.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                }

                comments.sortByDescending { it.timestamp }
                callback(comments)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}