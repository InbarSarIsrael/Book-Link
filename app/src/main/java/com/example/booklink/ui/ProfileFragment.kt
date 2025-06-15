package com.example.booklink.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklink.LoginActivity
import com.example.booklink.R
import com.example.booklink.adapters.BookProfileAdapter
import com.example.booklink.databinding.FragmentProfileBinding
import com.example.booklink.interfaces.BookCallback
import com.example.booklink.model.Book
import com.example.booklink.model.DataManager
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookProfileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAdapterCallbacks()

        val userId = getCurrentUserId()
        DataManager.fetchUserAddedBooks(userId)
        DataManager.userAddedBooksData.observe(viewLifecycleOwner) { addedBooks ->
            bookAdapter.submitList(addedBooks)
        }

        binding.profileBTNLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    private fun setupRecyclerView() {
        bookAdapter = BookProfileAdapter()
        binding.profileRVList.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupAdapterCallbacks() {
        bookAdapter.bookCallback = object : BookCallback {
            override fun favoriteButtonClicked(book: Book, position: Int) {
                book.isFavorite = !book.isFavorite
                bookAdapter.notifyItemChanged(position)
                DataManager.toggleFavorite(getCurrentUserId(), book)
            }

            // Required to implement the full BookCallback interface,
            // even if rating changes are not needed in this fragment.
            override fun ratingChanged(book: Book, newRating: Float) {
                // Not used in ProfileFragment
            }

            override fun editButtonClicked(book: Book) {
                val fragment = AddFragment.newInstance(book)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }
}

