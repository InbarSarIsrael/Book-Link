package com.example.booklink.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.booklink.BookDetailsActivity
import com.example.booklink.R
import com.example.booklink.databinding.BookItemProfileBinding
import com.example.booklink.interfaces.BookCallback
import com.example.booklink.model.Book
import com.example.booklink.utilities.ImageLoader
import java.util.Locale

// Adapter for displaying a list of books in the user's profile
class BookProfileAdapter : RecyclerView.Adapter<BookProfileAdapter.BookViewHolder>() {

    private var books: MutableList<Book> = mutableListOf()
    var bookCallback: BookCallback? = null  // Callback for favorite/edit actions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun getItemCount() = books.size

    // Updates the list of books in the adapter
    fun submitList(newBooks: List<Book>) {
        books = newBooks.toMutableList()
        notifyDataSetChanged()
    }

    // ViewHolder for a single book item in the profile
    inner class BookViewHolder(val binding: BookItemProfileBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            with(binding) {
                // Favorite button click
                bookIGMFavorite.setOnClickListener {
                    val book = books[adapterPosition]
                    bookCallback?.favoriteButtonClicked(book, adapterPosition)
                }

                // Edit button click
                bookIGMEdit.setOnClickListener {
                    val book = books[adapterPosition]
                    bookCallback?.editButtonClicked(book)
                }

                // Navigate to details screen on poster click
                bookIMGPoster.setOnClickListener {
                    val context = it.context
                    val book = books[adapterPosition]
                    val intent = Intent(context, BookDetailsActivity::class.java)
                    intent.putExtra("book", book)
                    context.startActivity(intent)
                }
            }
        }

        fun bind(book: Book) {
            with(binding) {
                bookLBLName.text = book.name
                authorLblName.text = book.author
                bookLBLPages.text = root.context.getString(R.string.pages_format, book.length)
                bookLBLReleaseDate.text = book.releaseDate
                bookLBLGenres.text = book.genre.joinToString(", ")
                bookLBLSummary.text = book.summary
                bookLBLUserReview.text = book.userReview

                bookLBLRatingValue.text = String.format(Locale.getDefault(), "%.1f", book.rating)

                bookIGMFavorite.setImageResource(
                    if (book.isFavorite) R.drawable.heart else R.drawable.empty_heart
                )

                ImageLoader.getInstance().loadImage(book.poster, bookIMGPoster)
            }
        }
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }
}