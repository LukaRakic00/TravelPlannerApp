package com.luka.travelplannerapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.firebase.FirestoreClass
import com.luka.travelplannerapp.models.Board
import com.luka.travelplannerapp.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var mSelectedImageFileUri: Uri? = null

    private lateinit var mUserName: String

    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        // poziv fcije
        setupActionBar()

        if(intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        // inicijalizacija potrebne slike iz actitity_create_board
        val ivBoardImage = findViewById<CircleImageView>(R.id.iv_board_image)

        // Inicijalizacija ActivityResultLauncher-a, cela ova prica sa galleryLauncher je u tome sto je
        // metoda onActitityResult postala depracted, tako da je ovo danas praksa ovako da se radi
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { selectedImageUri ->
                    // Sačuvaj URI slike u mSelectedImageFileUri
                    mSelectedImageFileUri = selectedImageUri

                    // Prikazivanje odabrane slike u ivUserImage koristeći Glide
                    try {
                        Glide
                            .with(this@CreateBoardActivity)
                            .load(mSelectedImageFileUri) // Učitavanje URI-ja slike
                            .centerCrop() // Podešavanje da slika bude centrirana i isečena
                            .placeholder(R.drawable.ic_board_place_holder) // Placeholder slika
                            .into(ivBoardImage) // Postavljanje slike u ImageView
                    } catch (e: IOException) {
                        e.printStackTrace() // Ispis greške ako dođe do problema sa učitavanjem slike
                    }
                }
            }
        }

        ivBoardImage.setOnClickListener {

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

        val buttonCreate = findViewById<Button>(R.id.btn_create)

        buttonCreate.setOnClickListener {
            if(mSelectedImageFileUri != null) {
                uploadBoardImage()
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    // kreiramo u DataBaseConnection znaci u FireStore klasi
    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK) // pokretanje aktivnosti ako je rezultat ispravan
        finish()
    }

    /**
     * Funkcija za podešavanje action bar-a
     */
    private fun setupActionBar() {

        val toolbarCreateBoardActivity = findViewById<Toolbar>(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        toolbarCreateBoardActivity.setNavigationOnClickListener { onBackPressed() }
    }

    // fcija je ista kao u MyProfileActivity
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

    // fcija koja zapravo kreira board, tj putovanje
    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        // pripremamo informacije o putovanju
        val etBoardName = findViewById<AppCompatEditText>(R.id.et_board_name)

        var board = Board(
            etBoardName.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        // sada mozemo da prosledimo u bazu sve
        FirestoreClass().createBoard(this@CreateBoardActivity, board)
        // sada name treba za sliku update i za to cu napraviti novu fciju uploadBoardImage
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        // treba nam referenca ka firebase Storage
        if (mSelectedImageFileUri != null) {

            // dobijanje storage reference
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this@CreateBoardActivity, mSelectedImageFileUri
                )
            )

            // dodavanje datoteke u referencu
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                        "Firebase Journey Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            // dodeljivanje URL slike promenljivoj
                            mBoardImageURL = uri.toString()

                            // poziv funkcije za kreiranje putovanja
                            createBoard()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@CreateBoardActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }
}