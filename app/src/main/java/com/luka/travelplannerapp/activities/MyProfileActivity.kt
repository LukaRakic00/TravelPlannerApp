package com.luka.travelplannerapp.activities

import android.os.Bundle
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.firebase.FirestoreClass
import com.luka.travelplannerapp.models.User
import de.hdodenhof.circleimageview.CircleImageView
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.luka.travelplannerapp.utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var mSelectedImageFileUri: Uri? = null

    // da nam uri moze da se skine
    private var mProfileImageURL: String = ""

    // Globalna promenjiva za user details
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        // Inicijalizacija ActivityResultLauncher-a
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { selectedImageUri ->
                    // Sačuvaj URI slike u mSelectedImageFileUri
                    mSelectedImageFileUri = selectedImageUri

                    // Prikazivanje odabrane slike u ivUserImage koristeći Glide
                    try {
                        val ivUserImage = findViewById<CircleImageView>(R.id.iv_profile_user_image)
                        Glide
                            .with(this@MyProfileActivity)
                            .load(mSelectedImageFileUri) // Učitavanje URI-ja slike
                            .centerCrop() // Podešavanje da slika bude centrirana i isečena
                            .placeholder(R.drawable.ic_user_place_holder) // Placeholder slika
                            .into(ivUserImage) // Postavljanje slike u ImageView
                    } catch (e: IOException) {
                        e.printStackTrace() // Ispis greške ako dođe do problema sa učitavanjem slike
                    }
                }
            }
        }
        val ivUserImage = findViewById<CircleImageView>(R.id.iv_profile_user_image)
        ivUserImage.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // Ako je dozvola odobrena, pokrecemo izbor slike
                Constants.showImageChooser(galleryLauncher)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        val buttonUpdate = findViewById<Button>(R.id.btn_update)

        buttonUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))

                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Proveri da li je dobijena dozvola za čitanje spoljnog skladišta
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Ako je dozvola odobrena, pozovi funkciju za izbor slike
                Constants.showImageChooser(galleryLauncher)
            } else {
                // Ako je dozvola odbijena, prikaži poruku korisniku
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage. You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupActionBar() {
        val toolbarMyProfileActivity = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        // postavljanje Toolbar-a kao akcioni bar za aktivnosti(Activity)
        setSupportActionBar(toolbarMyProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        toolbarMyProfileActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {
        val ivUserImage = findViewById<CircleImageView>(R.id.iv_profile_user_image)

        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image) // URL slike, gde slika treba da bude u bazi
            .centerCrop() // Tip razmera slike
            .placeholder(R.drawable.ic_user_place_holder) // Default place holder
            .into(ivUserImage) // prikaz u koji će slika biti učitana


        val etName = findViewById<AppCompatEditText>(R.id.et_name_my_profile)
        val etEmail = findViewById<AppCompatEditText>(R.id.et_email_my_profile)
        val etMobile = findViewById<AppCompatEditText>(R.id.et_mobile)

        etName.setText(user.name)
        etEmail.setText(user.email)
        if (user.mobile != 0L) {
            etMobile.setText(user.mobile.toString())
        }
    }

    /**
     * Funkcija za otpremanje izabrane korisničke slike u Firebase cloud skladištu
     */
    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            // dobijanje storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this@MyProfileActivity, mSelectedImageFileUri
                )
            )

            // dodavanje datoteke u referencu
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            // dodeljivanje URL slike promenljivoj
                            mProfileImageURL = uri.toString()

                            // poziv funkcije za update user details u bazi
                            updateUserProfileData()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * Funkcija za ažuriranje detalja korisničkog profila u bazu podataka
     */
    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            // userHashMap["image"] // ako zelimo nesto iz hashMape, to je zapravo value za sliku image
            // ovo je update hash mape
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        val etNameMyProfile = findViewById<AppCompatEditText>(R.id.et_name_my_profile)
        if(etNameMyProfile.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = etNameMyProfile.text.toString()
        }

        val etMobileMyMobile = findViewById<AppCompatEditText>(R.id.et_mobile)
        if(etMobileMyMobile.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = etMobileMyMobile.text.toString().toLong() // jer user Hash mapa trazi Long
        }

        // update podataka u bazi
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}