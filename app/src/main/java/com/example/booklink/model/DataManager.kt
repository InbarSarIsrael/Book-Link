package com.example.booklink.model

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object DataManager {

    private val database = FirebaseDatabase.getInstance().reference

    // 1. All books (all posts from all users)
    val allBooksData = MutableLiveData<List<Book>>()

    // 2. Books added by the current user (initially empty)
    val userFavoritesData = MutableLiveData<List<Book>>()

    // 3. User's favorite books (using a Set to avoid duplicates)
    val userAddedBooksData = MutableLiveData<List<Book>>()

    fun fetchAllBooks(userId: String) {
        val booksRef = database.child("books")
        val favsRef = database.child("users").child(userId).child("favorites")

        favsRef.get().addOnSuccessListener { favsSnapshot ->
            val favoriteNames = favsSnapshot.children.mapNotNull { it.key }.toSet()

            booksRef.get().addOnSuccessListener { snapshot ->
                val books = mutableListOf<Book>()
                snapshot.children.forEach {
                    val book = it.getValue(Book::class.java)
                    book?.let { b ->
                        b.isFavorite = b.name in favoriteNames
                        books.add(b)
                    }
                }
                books.sortByDescending { it.timestamp }
                allBooksData.postValue(books)
            }
        }
    }

    fun fetchUserFavorites(userId: String) {
        database.child("users").child(userId).child("favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favs = mutableListOf<Book>()
                    snapshot.children.forEach {
                        val book = it.getValue(Book::class.java)
                        book?.let { b ->
                            b.isFavorite = true
                            favs.add(b)
                        }
                    }
                    userFavoritesData.postValue(favs)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun fetchUserAddedBooks(userId: String) {
        val addedRef = database.child("users").child(userId).child("addedBooks")
        val favsRef = database.child("users").child(userId).child("favorites")

        favsRef.get().addOnSuccessListener { favsSnapshot ->
            val favoriteNames = favsSnapshot.children.mapNotNull { it.key }.toSet()

            addedRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val added = mutableListOf<Book>()
                    snapshot.children.forEach {
                        val book = it.getValue(Book::class.java)
                        book?.let { b ->
                            b.isFavorite = b.name in favoriteNames
                            added.add(b)
                        }
                    }
                    added.sortByDescending { it.timestamp }
                    userAddedBooksData.postValue(added)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }


    fun toggleFavorite(userId: String, book: Book) {
        val favRef = database.child("users").child(userId).child("favorites").child(book.name)
        favRef.get().addOnSuccessListener {
            if (it.exists()) {
                favRef.removeValue().addOnSuccessListener {
                    fetchUserFavorites(userId)
                    fetchAllBooks(userId)
                    fetchUserAddedBooks(userId)
                }
                book.isFavorite = false
            } else {
                favRef.setValue(book).addOnSuccessListener {
                    fetchUserFavorites(userId)
                    fetchAllBooks(userId)
                    fetchUserAddedBooks(userId)
                }
                book.isFavorite = true
            }
        }
    }

    fun saveUserRating(userId: String, bookId: String, rating: Float) {
        val dbRef = FirebaseDatabase.getInstance().getReference("ratings")
        dbRef.child(bookId).child(userId).setValue(rating)
    }

    fun updateAverageRating(bookId: String) {
        val ratingsRef = FirebaseDatabase.getInstance().getReference("ratings").child(bookId)
        ratingsRef.get().addOnSuccessListener { snapshot ->
            var total = 0f
            var count = 0
            for (child in snapshot.children) {
                val rating = child.getValue(Float::class.java)
                if (rating != null) {
                    total += rating
                    count++
                }
            }
            if (count > 0) {
                val average = total / count

                FirebaseDatabase.getInstance().getReference("books").child(bookId)
                    .child("rating").setValue(average)

                FirebaseDatabase.getInstance().getReference("users")
                    .get().addOnSuccessListener { usersSnapshot ->
                        for (userSnapshot in usersSnapshot.children) {
                            val addedBook = userSnapshot.child("addedBooks").child(bookId)
                            if (addedBook.exists()) {
                                FirebaseDatabase.getInstance().getReference("users")
                                    .child(userSnapshot.key!!)
                                    .child("addedBooks")
                                    .child(bookId)
                                    .child("rating")
                                    .setValue(average)
                            }
                        }
                    }
            }
        }
    }

    fun listenToAverageRatings() {
        val dbRef = FirebaseDatabase.getInstance().getReference("ratings")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedBooks = allBooksData.value?.toMutableList() ?: return
                var updated = false

                for (bookSnapshot in snapshot.children) {
                    val bookId = bookSnapshot.key ?: continue
                    val ratings = bookSnapshot.children.mapNotNull { it.getValue(Float::class.java) }
                    val average = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f

                    val book = updatedBooks.find { it.name == bookId }
                    if (book != null) {
                        book.rating = average
                        updated = true
                    }
                }

                if (updated) {
                    allBooksData.postValue(updatedBooks)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun fetchAverageRating(bookId: String, callback: (Float) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("ratings").child(bookId)
        dbRef.get().addOnSuccessListener { snapshot ->
            var sum = 0f
            var count = 0
            for (child in snapshot.children) {
                val rating = child.getValue(Float::class.java)
                if (rating != null) {
                    sum += rating
                    count++
                }
            }
            val average = if (count > 0) sum / count else 0f
            callback(average)
        }
    }

    fun addUserBook(userId: String, book: Book) {
        book.timestamp = System.currentTimeMillis()

        database.child("users").child(userId).child("addedBooks").child(book.name).setValue(book)
        database.child("books").child(book.name).setValue(book)
    }


    fun generateInitialBooks(): List<Book> {
        val books = mutableListOf<Book>()
        books.add(
            Book(
                poster = "https://covers.shakespeareandcompany.com/97818402/9781840221930.jpg",
                name = "Pride and Prejudice",
                writer = "Jane Austen",
                genre = listOf("Romance", "Historical Fiction", "Satire"),
                length = 279,
                summary = "Pride and Prejudice by Jane Austen is a classic novel about love, social class, and personal growth. The story follows Elizabeth Bennet, a smart and independent young woman, as she navigates family pressures and societal expectations in 19th-century England. She meets the proud and wealthy Mr. Darcy, and their initial misunderstandings slowly turn into mutual respect and love. Through humor and sharp observations, the novel explores themes of pride, prejudice, and the importance of looking beyond first impressions.",
                userReview = "",
                releaseDate = "28/01/1913",
                rating = 4f,
                isFavorite = false,
                timestamp = 1746818400000

            )
        )
        books.add(
            Book(
                poster = "https://m.media-amazon.com/images/I/712zD1rKTUL._UF1000,1000_QL80_.jpg",
                name = "It Ends With Us",
                writer = "Colleen Hoover",
                genre = listOf("Romance", "Drama", "Psychological"),
                length = 376,
                summary = "Lily falls in love with Ryle, a successful doctor, but their relationship turns dark. When her first love, Atlas, reappears, she must make a difficult choice about her future and break a cycle of abuse.",
                userReview = "",
                releaseDate = "02/06/2016",
                rating = 2f,
                isFavorite = false,
                timestamp = 1746904800000
            )
        )
        books.add(
            Book(
                poster = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1348257965i/104979.jpg",
                name = "Journey of Souls",
                writer = "Michael Newton",
                genre = listOf("Spirituality", "Non-fiction", "Psychology"),
                length = 278,
                summary = "A groundbreaking exploration of what happens to souls between lives, based on case studies from hypnotherapy sessions with patients who recalled their life between lives.",
                userReview = "",
                releaseDate = "01/09/1994",
                rating = 2.5f,
                isFavorite = false,
                timestamp = 1746991200000
            )
        )
        books.add(
            Book(
                poster = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1674743453i/86767939.jpg",
                name = "Terms and Conditions",
                writer = "Lauren Asher",
                genre = listOf("Romance", "Contemporary", "New Adult"),
                length = 448,
                summary = "Declan needs a wife to secure his role as CEO. Iris, his assistant, becomes the unexpected choice. Their fake marriage starts to feel real as emotions and attraction grow.",
                userReview = "",
                releaseDate = "24/02/2022",
                rating = 5f,
                isFavorite = false,
                timestamp = 1747077600000
            )
        )
        books.add(
            Book(
                poster = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1671865608i/74045390.jpg",
                name = "The Fine Print",
                writer = "Lauren Asher",
                genre = listOf("Romance", "New Adult", "Contemporary"),
                length = 400,
                summary = "Rowan, a theme park mogul, must work with Zara, a spirited employee, to create new magic — but personal secrets and emotional walls make the journey complicated.",
                userReview = "",
                releaseDate = "08/07/2021",
                rating = 1f,
                isFavorite = false,
                timestamp = 1747164000000
            )
        )
        return books
    }
}