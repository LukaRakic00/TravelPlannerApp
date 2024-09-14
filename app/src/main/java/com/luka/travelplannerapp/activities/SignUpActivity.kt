package com.luka.travelplannerapp.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.firebase.FirestoreClass
import com.luka.travelplannerapp.models.User

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        }
        else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

        }

        setupActionBar()
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this@SignUpActivity,
            "you have succesfully registered",
            Toast.LENGTH_SHORT
        ).show()
        hideProgressDialog()
        /**
         * Ovde se novi registrovani korisnik automatski prijavljuje
         * tako da samo odjavljujemo korisnika sa Firebase-a
         * i pošaljite ga na Intro Screen za prijavu
         * */
        FirebaseAuth.getInstance().signOut()
        // kraj finish Sign-Up Screen
        finish()
    }

    private fun setupActionBar() {
        val toolbarSignUp = findViewById<Toolbar>(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbarSignUp)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbarSignUp.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val btn_sign_up: Button = findViewById(R.id.btn_sign_up)
        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val etName: EditText = findViewById(R.id.et_name)
        val etEmail: EditText = findViewById(R.id.et_email)
        val etPassword: EditText = findViewById(R.id.et_password)

        val name: String = etName.text.toString().trim { it <= ' '} // trim - da ukloni na kraju imena sve space-e
        val email: String = etEmail.text.toString().trim { it <= ' '}
        val password: String = etPassword.text.toString().trim { it <= ' '}

        // ovo radi zapravo registraciju na firebase-u
        if(validateForm(email, password)) {
            // prikazivanja progres dialog-a
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        // ako je registracija dobra
                        if (task.isSuccessful) {

                            // Firebase registrovani user
                            val firebaseUser: FirebaseUser = task.result!!.user!!

                            // Registrovani email
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid, name, registeredEmail)

                            FirestoreClass().registerUser(this, user)

                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                "Registration failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
        }
    }

    // Funkcija za proveru unosa novog korisnika.
    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            } else -> true
        }
    }


}