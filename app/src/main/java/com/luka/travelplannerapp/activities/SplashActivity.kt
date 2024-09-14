package com.luka.travelplannerapp.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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

        val textView01 = findViewById<TextView>(R.id.textView01)
        val typeFace: Typeface = Typeface.createFromAsset(assets, "Sophiecomic-Regular.ttf")
        textView01.typeface = typeFace

        Handler(Looper.getMainLooper()).postDelayed({
        // ako smo ulogovani direktno onda nas posalji na MainActivity, a ako nismo onda na IntroActivity
            var currentUserID = FirestoreClass().getCurrentUserId()
            if(currentUserID.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }

            finish()
        }, 2500)

    }

}
