package com.example.booklink.interfaces

import com.example.booklink.model.Book

interface BookCallback {
    fun favoriteButtonClicked(book: Book, position: Int)
    fun ratingChanged(book: Book, newRating: Float)
    fun editButtonClicked(book: Book)
}