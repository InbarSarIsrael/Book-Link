package com.example.booklink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.booklink.databinding.ActivityMainBinding
import com.example.booklink.model.DataManager
import com.example.booklink.ui.HomeFragment
import com.example.booklink.ui.FavoritesFragment
import com.example.booklink.ui.AddFragment
import com.example.booklink.ui.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uploadInitialBooksIfEmpty()

        // Show HomeFragment by default
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, HomeFragment())
            .commit()

        // Handle bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, FavoritesFragment())
                        .commit()
                    true
                }
                R.id.nav_add -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, AddFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


    private fun uploadInitialBooksIfEmpty() {
        val dbRef = FirebaseDatabase.getInstance().reference.child("books")

        dbRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val initialBooks = DataManager.generateInitialBooks()
                initialBooks.forEach { book ->
                    dbRef.child(book.name).setValue(book)
                }
            }
        }
    }
}
