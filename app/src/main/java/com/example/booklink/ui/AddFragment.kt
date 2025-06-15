package com.example.booklink.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import java.util.Calendar

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null

    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    private var bookToEdit: Book? = null

    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookToEdit = arguments?.getSerializable(ARG_BOOK) as? Book
        bookToEdit?.let { populateFieldsForEditing(it) }


        binding.addBookImageContainer.setOnClickListener {
            openImageChooser()
        }

        binding.addBookBtn.setOnClickListener {
            addBook()
        }

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
                        val dateStr = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                        binding.addBookReleaseDateEdt.setText(dateStr)
                    }, year, month, day
                )
                datePickerDialog.show()
            }
        }
    }

    private fun populateFieldsForEditing(book: Book) {
        binding.addBookTitleEdt.setText(book.name)
        binding.addBookAuthorEdt.setText(book.writer)
        binding.addBookGenreEdt.setText(book.genre.joinToString(", "))
        binding.addBookPagesEdt.setText(book.length.toString())
        binding.addBookSummaryEdt.setText(book.summary)
        binding.addBookReviewEdt.setText(book.userReview)
        binding.addBookReleaseDateEdt.setText(book.releaseDate)

        if (book.poster.isNotBlank()) {
            imageUri = Uri.parse(book.poster)

            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.addBookImage)

            binding.addBookUploadText.visibility = View.GONE
        }

        binding.addBookBtn.text = "Update Book"
    }


    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.addBookImage)
            binding.addBookUploadText.visibility = View.GONE
        }
    }

    private fun addBook() {
        val name = binding.addBookTitleEdt.text.toString().trim()
        val writer = binding.addBookAuthorEdt.text.toString().trim()
        val genreText = binding.addBookGenreEdt.text.toString().trim()
        val pagesText = binding.addBookPagesEdt.text.toString().trim()
        val summary = binding.addBookSummaryEdt.text.toString().trim()
        val userReview = binding.addBookReviewEdt.text.toString().trim()
        val releaseDateText = binding.addBookReleaseDateEdt.text.toString().trim()

        if (name.isEmpty() || writer.isEmpty() || genreText.isEmpty() || pagesText.isEmpty() || summary.isEmpty() || releaseDateText.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val pages = try {
            pagesText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid page count", Toast.LENGTH_SHORT).show()
            return
        }

        val genres = genreText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val userId = getCurrentUserId()

        uploadImageToFirebase { uploadedImageUrl ->
            val posterUrl = uploadedImageUrl ?: ""

            if (bookToEdit != null) {
                val book = bookToEdit!!
                book.name = name
                book.writer = writer
                book.genre = genres
                book.length = pages
                book.summary = summary
                book.userReview = userReview
                book.releaseDate = releaseDateText
                book.poster = posterUrl

                DataManager.addUserBook(userId, book)
                Toast.makeText(requireContext(), "Book updated!", Toast.LENGTH_SHORT).show()
            } else {
                val newBook = Book(
                    poster = posterUrl,
                    name = name,
                    writer = writer,
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

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    private fun clearFields() {
        binding.addBookImage.setImageResource(android.R.color.transparent)
        binding.addBookUploadText.visibility = View.VISIBLE
        binding.addBookTitleEdt.text?.clear()
        binding.addBookAuthorEdt.text?.clear()
        binding.addBookGenreEdt.text?.clear()
        binding.addBookPagesEdt.text?.clear()
        binding.addBookSummaryEdt.text?.clear()
        binding.addBookReviewEdt.text?.clear()
        imageUri = null
    }

    companion object {
        private const val ARG_BOOK = "book_to_edit"

        fun newInstance(book: Book): AddFragment {
            val fragment = AddFragment()
            val args = Bundle()
            args.putSerializable(ARG_BOOK, book)
            fragment.arguments = args
            return fragment
        }
    }

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
