package com.luka.travelplannerapp.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.firebase.FirestoreClass
import com.luka.travelplannerapp.models.User



class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    /**
     * Ovu funkciju Android automatski kreira kada se kreira klasa aktivnosti.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // parent konstruktor
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = Firebase.auth

        // Ovo se koristi za skrivanje statusne trake i pretvaranje uvodnog ekrana kao aktivnosti na celom ekranu.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setupActionBar()

        val btn_sign_in: Button = findViewById(R.id.btn_sign_in)
        btn_sign_in.setOnClickListener {
            signInRegisterUser()
        }
    }

    fun signInSuccess (user: User) {
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun setupActionBar() {
        val toolbarSignIn = findViewById<Toolbar>(R.id.toolbar_sign_in_activity)
        setSupportActionBar(toolbarSignIn)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbarSignIn.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun signInRegisterUser() {
        val etEmailSignIn: EditText = findViewById(R.id.et_email_sign_in)
        val etPasswordSignIn: EditText = findViewById(R.id.et_password_sign_in)

        val email: String = etEmailSignIn.text.toString().trim { it <= ' ' }
        val password: String = etPasswordSignIn.text.toString().trim { it <= ' ' }

        // ovo radi zapravo registraciju na firebase-u
        if (validateForm(email, password)) {
            // prikazivanja progres dialog-a
            showProgressDialog(resources.getString(R.string.please_wait))

            // Sing-In koriscenjem FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Pozivanje funkcije FirestoreClass signInUser za dobijanje podataka o korisniku iz baze podataka.
                       FirestoreClass().loadUserData(this@SignInActivity)
                    } else {
                        // Ako sign in nije prosao dobro onda izbaci Toast
                        Toast.makeText(
                            this@SignInActivity,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }

    // Funkcija za proveru unosa novog korisnika.
    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }

            else -> true
        }
    }


}