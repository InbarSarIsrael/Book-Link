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
import com.example.booklink.model.DataManager
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailsBinding

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var bookId: String // Used to identify book-related comments
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve Book object from Intent (Parcelable)
        @Suppress("DEPRECATION") // For API < 33
        val book = intent.getParcelableExtra<Book>("book")

        // Show book details if available
        book?.let { displayBookData(it) }

        // Set back button action
        binding.bookDetailsBTNBack.setOnClickListener {
            finish()
        }

        // Handle comment submission
        binding.bookDetailsBTNSendComment.setOnClickListener {
            book?.let { submitComment() }
        }

        // Set up RecyclerView for displaying comments
        commentAdapter = CommentAdapter(comments)
        binding.bookDetailsRVComments.layoutManager = LinearLayoutManager(this)
        binding.bookDetailsRVComments.adapter = commentAdapter
    }

    // Display book info in UI and start loading comments
    private fun displayBookData(book: Book) {
        binding.bookDetailsLBLName.text = getString(R.string.book_name_label, book.name)
        binding.bookDetailsLBLAuthor.text = getString(R.string.author_label, book.author)
        binding.bookDetailsLBLPages.text = getString(R.string.pages_label, book.length)
        binding.bookDetailsLBLGenre.text = getString(R.string.genres_label, book.genre.joinToString(", "))
        binding.bookDetailsLBLRelease.text = getString(R.string.release_label, book.releaseDate)
        binding.bookDetailsLBLRatingValue.text = getString(R.string.loading)
        binding.bookDetailsLBLSummary.text = getString(R.string.summary_label, book.summary)
        binding.bookDetailsLBLUserReview.text = getString(R.string.review_label, book.userReview)

        setupRating(book)
        listenToRatingChanges(book.name)

        // Load book poster image using Glide
        Glide.with(this)
            .load(book.poster)
            .into(binding.bookDetailsIMGPoster)

        // Use sanitized book name as ID key for comments
        bookId = book.name.replace(" ", "_")
        fetchComments(bookId)
    }

    private fun setupRating(book: Book) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val ratingBar = binding.bookDetailsLBLRating
        val ratingValue = binding.bookDetailsLBLRatingValue

        // Fetch and display average rating
        DataManager.fetchAverageRating(book.name) { average ->
            ratingBar.rating = average
            ratingValue.text = String.format(Locale.getDefault(), "%.1f", average)
        }

        // Allow user to submit a new rating
        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser && userId != null) {
               DataManager.saveUserRating(userId, book.name, rating)
                DataManager.updateAverageRating(book.name)

               DataManager.fetchAverageRating(book.name) { average ->
                    ratingBar.rating = average
                    ratingValue.text = String.format(Locale.getDefault(), "%.1f", average)
                }
            }
        }
    }

    private fun listenToRatingChanges(bookName: String) {
        val ratingRef = FirebaseDatabase.getInstance()
            .getReference("ratings")
            .child(bookName)

        ratingRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                var total = 0f
                var count = 0
                for (child in snapshot.children) {
                    val rating = child.getValue(Float::class.java)
                    if (rating != null) {
                        total += rating
                        count++
                    }
                }
                val average = if (count > 0) total / count else 0f
                binding.bookDetailsLBLRating.rating = average
                binding.bookDetailsLBLRatingValue.text = String.format(Locale.getDefault(), "%.1f", average)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("RatingListener", "Rating fetch cancelled: ${error.message}")
            }
        })
    }

    // Handle user comment submission
    private fun submitComment() {
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

    // Load comments from Firebase and update the list
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