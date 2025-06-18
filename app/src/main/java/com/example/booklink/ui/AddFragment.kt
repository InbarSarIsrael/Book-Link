package com.example.booklink.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booklink.R
import com.example.booklink.databinding.FragmentAddBinding
import com.example.booklink.model.Book
import com.example.booklink.model.DataManager
import com.google.firebase.auth.FirebaseAuth
import android.app.DatePickerDialog
import androidx.core.net.toUri
import java.util.Calendar
import java.util.Locale
import androidx.activity.result.contract.ActivityResultContracts

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null

    private val binding get() = _binding!!

    private var imageUri: Uri? = null // Selected image URI

    private var bookToEdit: Book? = null // Book passed for editing (null if creating a new book)

    // Launcher for image picking using the modern Activity Result API
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.addBookImage)
            binding.addBookUploadText.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve book if passed as argument for editing
        @Suppress("DEPRECATION") // For backward compatibility with minSdk < 33
        bookToEdit = arguments?.getParcelable(ARG_BOOK)

        bookToEdit?.let { populateFieldsForEditing(it) }

        // Image picker trigger
        binding.addBookImageContainer.setOnClickListener {
            openImageChooser()
        }

        // Submit button
        binding.addBookBtn.setOnClickListener {
            addBook()
        }

        // Show date picker on release date field click
        binding.addBookReleaseDateEdt.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(requireContext(),
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val dateStr = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear
                        )
                        binding.addBookReleaseDateEdt.setText(dateStr)
                    }, year, month, day
                )
                datePickerDialog.show()
            }
        }
    }

     // Fill UI fields with data of the book being edited
    private fun populateFieldsForEditing(book: Book) {
        binding.addBookTitleEdt.setText(book.name)
        binding.addBookAuthorEdt.setText(book.author)
        binding.addBookGenreEdt.setText(book.genre.joinToString(", "))
        binding.addBookPagesEdt.setText(book.length.toString())
        binding.addBookSummaryEdt.setText(book.summary)
        binding.addBookReviewEdt.setText(book.userReview)
        binding.addBookReleaseDateEdt.setText(book.releaseDate)

        if (book.poster.isNotBlank()) {
            imageUri = book.poster.toUri()
            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.addBookImage)
            binding.addBookUploadText.visibility = View.GONE
        }

        binding.addBookBtn.text = getString(R.string.update_book)
    }

    // Opens image picker from gallery
    private fun openImageChooser() {
        pickImageLauncher.launch("image/*")
    }

     //Validates input and adds or updates a book in the database
    private fun addBook() {
        val name = binding.addBookTitleEdt.text.toString().trim()
        val author = binding.addBookAuthorEdt.text.toString().trim()
        val genreText = binding.addBookGenreEdt.text.toString().trim()
        val pagesText = binding.addBookPagesEdt.text.toString().trim()
        val summary = binding.addBookSummaryEdt.text.toString().trim()
        val userReview = binding.addBookReviewEdt.text.toString().trim()
        val releaseDateText = binding.addBookReleaseDateEdt.text.toString().trim()

        // Input validation
        if (name.isEmpty() || author.isEmpty() || genreText.isEmpty() || pagesText.isEmpty()
            || summary.isEmpty() || releaseDateText.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val pages = try {
            pagesText.toInt()
        } catch (_: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid page count", Toast.LENGTH_SHORT).show()
            return
        }

        val genres = genreText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val userId = getCurrentUserId()

        // Upload image to Firebase before saving the book
            uploadImageToFirebase { uploadedImageUrl ->
                val posterUrl = uploadedImageUrl ?: bookToEdit?.poster ?: ""

                if (bookToEdit != null) {
                // Update existing book
                val book = bookToEdit!!
                book.name = name
                book.author = author
                book.genre = genres
                book.length = pages
                book.summary = summary
                book.userReview = userReview
                book.releaseDate = releaseDateText
                book.poster = posterUrl

                DataManager.addUserBook(userId, book)
                Toast.makeText(requireContext(), "Book updated!", Toast.LENGTH_SHORT).show()
            } else {
                // Create new book
                val newBook = Book(
                    poster = posterUrl,
                    name = name,
                    author = author,
                    genre = genres,
                    length = pages,
                    summary = summary,
                    userReview = userReview,
                    releaseDate = releaseDateText,
                    rating = 0.0f,
                    isFavorite = false,
                    timestamp = System.currentTimeMillis()
                )
                DataManager.addUserBook(userId, newBook)
                Toast.makeText(requireContext(), "Book added!", Toast.LENGTH_SHORT).show()
            }

            clearFields()

            // Return to profile screen
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

     //Returns current Firebase user ID
    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

     // Clears all input fields
    private fun clearFields() {
        binding.addBookImage.setImageResource(android.R.color.transparent)
        binding.addBookUploadText.visibility = View.VISIBLE
        binding.addBookTitleEdt.text?.clear()
        binding.addBookAuthorEdt.text?.clear()
        binding.addBookGenreEdt.text?.clear()
        binding.addBookPagesEdt.text?.clear()
        binding.addBookSummaryEdt.text?.clear()
        binding.addBookReviewEdt.text?.clear()
        binding.addBookReleaseDateEdt.text?.clear()
        imageUri = null
    }

    companion object {
        private const val ARG_BOOK = "book_to_edit"

        // Creates a new instance of AddFragment with a Book (for editing)
        fun newInstance(book: Book): AddFragment {
            val fragment = AddFragment()
            val args = Bundle()
            args.putParcelable(ARG_BOOK, book)
            fragment.arguments = args
            return fragment
        }
    }

    // Uploads selected image to Firebase Storage
    private fun uploadImageToFirebase(onComplete: (String?) -> Unit) {
        val image = imageUri ?: return onComplete(null)

        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("book_images/${System.currentTimeMillis()}.jpg")

        imageRef.putFile(image)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show()
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                onComplete(null)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}