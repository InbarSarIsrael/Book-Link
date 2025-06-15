package com.example.booklink.model

import java.io.Serializable

class Book() : Serializable {
    var poster: String = ""
    var name: String = ""
    var writer: String = ""
    var genre: List<String> = emptyList()
    var length: Int = 0
    var summary: String = ""
    var userReview: String = ""
    var releaseDate: String = ""
    var rating: Float = 0.0f
    var isFavorite: Boolean = false
    var isCollapsed: Boolean = false
    var timestamp: Long = 0L

    constructor(
        poster: String,
        name: String,
        writer: String,
        genre: List<String>,
        length: Int,
        summary: String,
        userReview: String,
        releaseDate: String,
        rating: Float,
        isFavorite: Boolean,
        timestamp: Long
    ) : this() {
        this.poster = poster
        this.name = name
        this.writer = writer
        this.genre = genre
        this.length = length
        this.summary = summary
        this.userReview = userReview
        this.releaseDate = releaseDate
        this.rating = rating
        this.isFavorite = isFavorite
        this.timestamp = timestamp
    }

    fun toggleCollapse() {
        isCollapsed = !isCollapsed
    }
}
