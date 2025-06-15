package com.example.booklink.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklink.adapters.BookAdapter
import com.example.booklink.databinding.FragmentFavoritesBinding
import com.example.booklink.interfaces.BookCallback
import com.example.booklink.model.Book
import com.example.booklink.model.DataManager
import com.google.firebase.auth.FirebaseAuth

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null

    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start listening to average ratings in case they change
        DataManager.listenToAverageRatings()

        // Set up RecyclerView and adapter callbacks
        setupRecyclerView()
        setupAdapterCallbacks()

        // Fetch favorite books for the current user
        val userId = getCurrentUserId()
        DataManager.fetchUserFavorites(userId)

        // Observe favorite books and update the adapter when data changes
        DataManager.userFavoritesData.observe(viewLifecycleOwner) { favorites ->
            bookAdapter.submitList(favorites)
        }
    }

    // Sets up RecyclerView with the BookAdapter
    private fun setupRecyclerView() {
        bookAdapter = BookAdapter()
        binding.favRVList.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // Defines what happens when the user interacts with each book item
    private fun setupAdapterCallbacks() {
        bookAdapter.bookCallback = object : BookCallback {
            override fun favoriteButtonClicked(book: Book, position: Int) {
                val userId = getCurrentUserId()
                DataManager.toggleFavorite(userId, book)
            }

            override fun ratingChanged(book: Book, newRating: Float) {
                val userId = getCurrentUserId()
                DataManager.saveUserRating(userId, book.name, newRating)
                DataManager.updateAverageRating(book.name)
            }

            override fun editButtonClicked(book: Book) {
                // Not needed in FavoritesFragment
            }
        }
    }

    // Returns the current logged-in Firebase user ID (or empty if not logged in)
    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
