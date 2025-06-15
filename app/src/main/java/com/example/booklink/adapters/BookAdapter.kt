package com.example.booklink.adapters

import android.animation.ObjectAnimator
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.booklink.BookDetailsActivity
import com.example.booklink.databinding.BookItemBinding
import com.example.booklink.interfaces.BookCallback
import com.example.booklink.model.DataManager
import com.example.booklink.R
import com.example.booklink.model.Book
import com.example.booklink.utilities.Constants
import com.example.booklink.utilities.ImageLoader
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import kotlin.math.max

// Adapter for displaying a list of books in a RecyclerView
class BookAdapter : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var books: MutableList<Book> = mutableListOf()

    private var fullBooks: List<Book> = listOf()  // full list for filtering

    var bookCallback: BookCallback? = null        // callback for favorite button

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun getItemCount(): Int = books.size

    private fun getItem(position: Int) = books[position]

    // Submit a new list of books to the adapter
    fun submitList(newBooks: List<Book>) {
        fullBooks = newBooks
        books = newBooks.toMutableList()
        notifyDataSetChanged()
    }

    // Filter books by title or author
    fun filter(query: String) {
        books = if (query.isBlank()) {
            fullBooks.toMutableList()
        } else {
            fullBooks.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book)
    }

    // ViewHolder that binds each book item to the layout
    inner class BookViewHolder(val binding: BookItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.bookIGMFavorite.setOnClickListener {
                bookCallback?.favoriteButtonClicked(getItem(adapterPosition), adapterPosition)
            }
        }

        fun bind(book: Book) {
            with(binding) {
                bookLBLName.text = book.name
                authorLblName.text = book.author
                bookLBLReleaseDate.text = book.releaseDate
                bookLBLPages.text = root.context.getString(R.string.pages_format, book.length)
                bookLBLGenres.text = book.genre.joinToString(", ")
                bookLBLSummary.text = book.summary

                ImageLoader.getInstance().loadImage(book.poster, bookIMGPoster)

                bookIGMFavorite.setImageResource(
                    if (book.isFavorite) R.drawable.heart else R.drawable.empty_heart
                )
            }

            setupRating(book)
            setupExpansion(book)
            setupPosterClick(book)
        }

        // Setup rating bar interactions and display average rating
        private fun setupRating(book: Book) {
            with(binding) {
                movieRBRating.setOnRatingBarChangeListener { _, rating, fromUser ->
                    if (fromUser) {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnRatingBarChangeListener
                        DataManager.saveUserRating(userId, book.name, rating)
                        DataManager.updateAverageRating(book.name)

                        DataManager.fetchAverageRating(book.name) { average ->
                            movieRBRating.rating = average
                            bookLBLRatingValue.text = String.format(Locale.getDefault(), "%.1f", average)
                        }
                    }
                }

                DataManager.fetchAverageRating(book.name) { average ->
                    movieRBRating.rating = average
                    bookLBLRatingValue.text = String.format(Locale.getDefault(), "%.1f", average)
                }
            }
        }
        // Toggle description expansion on card click
        private fun setupExpansion(book: Book) {
            with(binding) {
                bookCVData.setOnClickListener {
                    val maxLines = if (book.isCollapsed) bookLBLSummary.lineCount
                    else Constants.DataCard.OVERVIEW_MIN_LINES

                    val animator = ObjectAnimator.ofInt(bookLBLSummary, "maxLines", maxLines)
                    animator.duration = max(
                        (bookLBLSummary.lineCount - Constants.DataCard.OVERVIEW_MIN_LINES).toDouble(),
                        0.0
                    ).times(50L).toLong()

                    book.toggleCollapse()
                    animator.start()
                }
            }
        }

        private fun setupPosterClick(book: Book) {
            with(binding) {
                bookIMGPoster.setOnClickListener {
                    val context = it.context
                    val intent = Intent(context, BookDetailsActivity::class.java)
                    intent.putExtra("book", book)
                    context.startActivity(intent)
                }
            }
        }
    }
}