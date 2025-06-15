package com.example.booklink.model

import com.google.firebase.database.*

// Manager class for fetching comments from Firebase Realtime Database
object CommentManager {

    private val database = FirebaseDatabase.getInstance().reference

    // Fetches all comments for a specific book by its ID
    fun fetchComments(bookId: String, callback: (List<Comment>) -> Unit) {
        val commentsRef = database.child("comments").child(bookId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()

                // Parse each comment node into a Comment object
                for (commentSnap in snapshot.children) {
                    val comment = commentSnap.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                }

                // Sort comments by newest first
                comments.sortByDescending { it.timestamp }

                // Return result via callback
                callback(comments)
            }

            // Handle database read error
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())  // Optionally log the error here
            }
        })
    }
}