package com.example.booklink

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.booklink.adapters.CommentAdapter
import com.example.booklink.databinding.ActivityBookDetailsBinding
import com.example.booklink.model.Book
import com.example.booklink.model.Comment
import com.example.booklink.model.CommentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailsBinding

    private lateinit var commentAdapter: CommentAdapter

    private lateinit var bookId: String

    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val book = intent.getSerializableExtra("book") as? Book
        book?.let {
            displayBookData(it)
        }

        binding.bookDetailsBTNBack.setOnClickListener {
            finish()
        }

        binding.bookDetailsBTNSendComment.setOnClickListener {
            book?.let { submitComment(it) }
        }

        commentAdapter = CommentAdapter(comments)
        binding.bookDetailsRVComments.layoutManager = LinearLayoutManager(this)
        binding.bookDetailsRVComments.adapter = commentAdapter
    }

    private fun displayBookData(book: Book) {
        binding.bookDetailsLBLName.text = getString(R.string.book_name_label, book.name)
        binding.bookDetailsLBLAuthor.text = getString(R.string.author_label, book.writer)
        binding.bookDetailsLBLPages.text = getString(R.string.pages_label, book.length)
        binding.bookDetailsLBLGenre.text = getString(R.string.genres_label, book.genre.joinToString(", "))
        binding.bookDetailsLBLRelease.text = getString(R.string.release_label, book.releaseDate)
        binding.bookDetailsLBLRating.text = getString(R.string.rating_label, book.rating)
        binding.bookDetailsLBLSummary.text = getString(R.string.summary_label, book.summary)
        binding.bookDetailsLBLUserReview.text = getString(R.string.review_label, book.userReview)

        Glide.with(this)
            .load(book.poster)
            .into(binding.bookDetailsIMGPoster)

        bookId = book.name.replace(" ", "_")
        fetchComments(bookId)
    }

    private fun submitComment(book: Book) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val commentText = binding.bookDetailsEDTComment.text.toString().trim()
        if (commentText.isBlank()) return

        val username = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown"

        val timestamp = System.currentTimeMillis()
        val comment = Comment(userId, username, commentText, timestamp)

        FirebaseDatabase.getInstance().reference
            .child("comments")
            .child(bookId)
            .push()
            .setValue(comment)
            .addOnSuccessListener {
                binding.bookDetailsEDTComment.text.clear()
            }
    }

    private fun fetchComments(bookId: String) {
        Log.d("COMMENTS", "Fetching comments for $bookId")

        CommentManager.fetchComments(bookId) { commentList ->
            Log.d("COMMENTS", "Got ${commentList.size} comments from Firebase")

            comments.clear()
            comments.addAll(commentList)
            commentAdapter.notifyDataSetChanged()
        }
    }

}