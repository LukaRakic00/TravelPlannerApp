package com.luka.travelplannerapp.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.luka.travelplannerapp.R


open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Ova funkcija se koristi za prikaz dijaloga napretka (progress dialog) sa naslovom i porukom korisniku.
     */
    fun showProgressDialog(text: String) {
            mProgressDialog = Dialog(this)

            /* Podesite sadržaj ekrana iz resursa rasporeda.
             * Resurs će biti inflated, dodajući sve prikaze najvišeg nivoa(top-level views) na ekran.
             * */
            mProgressDialog.setContentView(R.layout.dialog_progress)

            val mProgressDialogTextView =
                mProgressDialog.findViewById<TextView>(R.id.tv_progress_text)


            mProgressDialogTextView.text = text

            //Pokreni dijalog i prikazi ga.

            mProgressDialog.show()
        }

        /**
         * Ova funkcija se koristi za odbacivanje dijaloga napretka ako je vidljiv korisniku.
         */
        fun hideProgressDialog() {
            mProgressDialog.dismiss()
        }

        // Ako currentUser slučajno bude null u ovom trenutku,
        // !! operator će izazvati KotlinNullPointerException i aplikacija će se srušiti
        fun getCurrentUserID(): String {
            return FirebaseAuth.getInstance().currentUser!!.uid // uid unique id
        }

        // za pritiskanje back dugmeta dva puta za izlazak iz aplikacije
        fun doubleBackToExit() {
            if (doubleBackToExitPressedOnce) {
                onBackPressedDispatcher.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(
                this,
                resources.getString(R.string.please_click_back_again_to_exit),
                Toast.LENGTH_SHORT
            ).show()

            // ako korisnik mnogo ceka zelimo da sve resetujemo
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }

        fun showErrorSnackBar(message: String) {
            val snackBar =
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    R.color.snackbar_error_color
                )
            )
            snackBar.show()
        }
}