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
import kotlin.math.max

class BookAdapter : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var books: MutableList<Book> = mutableListOf()

    private var fullBooks: List<Book> = listOf()

    var bookCallback: BookCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun getItemCount(): Int = books.size

    private fun getItem(position: Int) = books[position]

    val currentList: List<Book>
        get() = books

    fun submitList(newBooks: List<Book>) {
        fullBooks = newBooks
        books = newBooks.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        books = if (query.isBlank()) {
            fullBooks.toMutableList()
        } else {
            fullBooks.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.writer.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book)
    }

    inner class BookViewHolder(val binding: BookItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.bookIGMFavorite.setOnClickListener {
                bookCallback?.favoriteButtonClicked(getItem(adapterPosition), adapterPosition)
            }
        }

        fun bind(book: Book) {
            binding.bookLBLName.text = book.name
            binding.writerLBLName.text = book.writer
            binding.bookLBLReleaseDate.text = book.releaseDate
            binding.bookLBLPages.text = "${book.length} pages"
            binding.bookLBLGenres.text = book.genre.joinToString(", ")
            binding.bookLBLSummary.text = book.summary

            ImageLoader.getInstance().loadImage(book.poster, binding.bookIMGPoster)

            binding.bookIGMFavorite.setImageResource(
                if (book.isFavorite) R.drawable.heart else R.drawable.empty_heart
            )

            setupRating(book)
            setupExpansion(book)
            setupPosterClick(book)
        }

        private fun setupRating(book: Book) {
            binding.movieRBRating.setOnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnRatingBarChangeListener
                    DataManager.saveUserRating(userId, book.name, rating)
                    DataManager.updateAverageRating(book.name)

                    DataManager.fetchAverageRating(book.name) { average ->
                        binding.movieRBRating.rating = average
                        binding.bookLBLRatingValue.text = String.format("%.1f", average)
                    }
                }
            }

            DataManager.fetchAverageRating(book.name) { average ->
                binding.movieRBRating.rating = average
                binding.bookLBLRatingValue.text = String.format("%.1f", average)
            }
        }

        private fun setupExpansion(book: Book) {
            binding.bookCVData.setOnClickListener {
                val maxLines = if (book.isCollapsed) binding.bookLBLSummary.lineCount else Constants.DataCard.OVERVIEW_MIN_LINES

                val animator = ObjectAnimator.ofInt(binding.bookLBLSummary, "maxLines", maxLines)
                animator.duration = max(
                    (binding.bookLBLSummary.lineCount - Constants.DataCard.OVERVIEW_MIN_LINES).toDouble(),
                    0.0
                ).times(50L).toLong()

                book.toggleCollapse()
                animator.start()
            }
        }

        private fun setupPosterClick(book: Book) {
            binding.bookIMGPoster.setOnClickListener {
                val context = it.context
                val intent = Intent(context, BookDetailsActivity::class.java)
                intent.putExtra("book", book)
                context.startActivity(intent)
            }
        }
    }
}
