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

class BookProfileAdapter : RecyclerView.Adapter<BookProfileAdapter.BookViewHolder>() {

    private var books: MutableList<Book> = mutableListOf()

    var bookCallback: BookCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun getItemCount() = books.size

    fun submitList(newBooks: List<Book>) {
        books = newBooks.toMutableList()
        notifyDataSetChanged()
    }

    inner class BookViewHolder(val binding: BookItemProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.bookIGMFavorite.setOnClickListener {
                val book = books[adapterPosition]
                bookCallback?.favoriteButtonClicked(book, adapterPosition)
            }

            binding.bookIGMEdit.setOnClickListener {
                val book = books[adapterPosition]
                bookCallback?.editButtonClicked(book)
            }

            binding.bookIMGPoster.setOnClickListener {
                val context = it.context
                val book = books[adapterPosition]
                val intent = Intent(context, BookDetailsActivity::class.java)
                intent.putExtra("book", book)
                context.startActivity(intent)
            }
        }

        fun bind(book: Book) {
            with(binding) {
                bookLBLName.text = book.name
                writerLBLName.text = book.writer
                bookLBLPages.text = "${book.length} pages"
                bookLBLReleaseDate.text = book.releaseDate
                bookLBLGenres.text = book.genre.joinToString(", ")
                bookLBLSummary.text = book.summary
                bookLBLUserReview.text = book.userReview
                bookLBLRatingValue.text = String.format("%.1f", book.rating)

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
