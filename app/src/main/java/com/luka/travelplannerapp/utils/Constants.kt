package com.luka.travelplannerapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher

object Constants {

    // Ovo se koristi za naziv kolekcije za USERS
    const val USERS: String = "users"

    // Ovo se koristi za naziv kolekcije za BOARDS
    const val BOARDS: String = "boards" // za kreiranje Kolekcije, uradicu to direktno u kodu u CreateBoardActivity

    // Imena polja Firebase baze podataka
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO : String = "assignedTo"
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val DOCUMENT_ID : String = "documentId" // kada jednom kliknemo na Board, tj na adapter zelimo da pokrenemo drugi Activity i u novom tom Activiti-ju zelim da prosledim nove informacije, recimo detalje o putovanju


    // dodavanje novog polja za TaskList
    const val TASK_LIST: String = "taskList"

    /**
     * Funkcija za izbor slike korisničkog profila iz skladišta telefona.
     */
    fun showImageChooser(galleryLauncher: ActivityResultLauncher<Intent>) {
        try {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageChooserError", "Failed to open image chooser", e)
        }
    }

    // omogućava da pravilno identifikujem i rukujem  vrstama fajlova u aplikaciji
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Dvosmerna mapa koja preslikava MIME tipove u ekstenzije datoteka i obrnuto.
         * getSingleton(): Nabavite singleton instancu MimeTipeMap-a.
         * getEktensionFromMimeTipe: Vrati registrovanu ekstenziju za dati MIME tip.
         * contentResolver.getTipe: vraća MIME tip datog URL-a sadržaja.
         */

        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }


}