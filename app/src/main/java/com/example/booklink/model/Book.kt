package com.example.booklink.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

 // Data model representing a book in the app
@Parcelize
data class Book(
    var poster: String = "",           // URL to the book's cover image (hosted on Firebase Storage)
    var name: String = "",             // Title of the book
    var author: String = "",           // Author's name
    var genre: List<String> = emptyList(),  // List of genres
    var length: Int = 0,               // Number of pages
    var summary: String = "",          // Summary/overview of the book
    var userReview: String = "",       // Personal review written by the user
    var releaseDate: String = "",      // Release date
    var rating: Float = 0.0f,          // Average user rating (0.0–5.0)
    var isFavorite: Boolean = false,   // True if the user marked it as favorite
    var isCollapsed: Boolean = false,  // Used for toggling the UI summary expansion
    var timestamp: Long = 0L           // Time of creation (used for sorting/display)
) : Parcelable {

     // Toggles the collapsed state of the summary section (for UI)
    fun toggleCollapse() {
        isCollapsed = !isCollapsed
    }
}
