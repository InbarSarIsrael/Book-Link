package com.example.booklink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Force light mode (disable dark mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Apply system bars padding for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // If no user is currently signed in, launch sign-in flow
        if (FirebaseAuth.getInstance().currentUser == null) {
            signIn()
        } else {
            transactToMainActivity()
        }
    }

    // Launcher for the Firebase AuthUI sign-in activity
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    // Starts the sign-in flow using Firebase AuthUI
    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers) // Set authentication methods
            .setLogo(R.drawable.app_logo) // Optional app logo in sign-in screen
            .setTheme(R.style.LoginTheme) // Optional theme for styling
            .build()

        signInLauncher.launch(signInIntent)
    }

    // Handles result from Firebase AuthUI sign-in flow
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == RESULT_OK) {
            // User successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            val name = user?.displayName ?: "User"
            Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()
            transactToMainActivity()
        } else {
            // Sign-in failed or was cancelled by the user
            Toast.makeText(this, "Error: Failed Logging in.", Toast.LENGTH_LONG).show()
            signIn() // Retry sign-in
        }
    }

    // Navigates to the MainActivity and finishes the login screen
    private fun transactToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}