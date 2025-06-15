package com.example.booklink.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.booklink.databinding.CommentItemBinding
import com.example.booklink.model.Comment
import java.text.SimpleDateFormat
import java.util.*

// Adapter for displaying a list of user comments in a RecyclerView
class CommentAdapter(private var comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    // ViewHolder that binds a single comment item to the layout
    inner class CommentViewHolder(private val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            with(binding) {
                commentLBLUsername.text = comment.username
                commentLBLText.text = comment.text

                // Format the timestamp to a readable date string
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDate = formatter.format(Date(comment.timestamp))
                commentLBLDate.text = formattedDate
            }
        }
    }
}
