package com.example.booklink.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklink.adapters.BookAdapter
import com.example.booklink.databinding.FragmentHomeBinding
import com.example.booklink.interfaces.BookCallback
import com.example.booklink.model.Book
import com.example.booklink.model.DataManager
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DataManager.listenToAverageRatings()

        setupRecyclerView()
        setupAdapterCallbacks()
        setupSearchBar()
        setupWelcomeMessage()

        val userId = getCurrentUserId()
        DataManager.fetchUserFavorites(userId)

        DataManager.fetchAllBooks(userId)
        DataManager.allBooksData.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter()
        binding.mainRVList.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    private fun setupAdapterCallbacks() {
        bookAdapter.bookCallback = object : BookCallback {
            override fun favoriteButtonClicked(book: Book, position: Int) {
                val userId = getCurrentUserId()
                book.isFavorite = !book.isFavorite
                bookAdapter.notifyItemChanged(position)
                DataManager.toggleFavorite(userId, book)
            }

            override fun ratingChanged(book: Book, newRating: Float) {
                val userId = getCurrentUserId()
                DataManager.saveUserRating(userId, book.name, newRating)
                DataManager.updateAverageRating(book.name)
            }

            override fun editButtonClicked(book: Book) {
                // Not used in ProfileFragment
            }
        }
    }

    private fun setupSearchBar() {
        binding.homeEDTSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                bookAdapter.filter(query)
            }
        })
    }

    private fun setupWelcomeMessage() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName ?: "User"
        binding.homeLBLWelcome.text = "Welcome, $name"
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
