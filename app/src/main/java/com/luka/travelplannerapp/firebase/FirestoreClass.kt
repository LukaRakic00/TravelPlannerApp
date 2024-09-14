package com.luka.travelplannerapp.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.luka.travelplannerapp.activities.CreateBoardActivity
import com.luka.travelplannerapp.activities.MainActivity
import com.luka.travelplannerapp.activities.MyProfileActivity
import com.luka.travelplannerapp.activities.SignInActivity
import com.luka.travelplannerapp.activities.SignUpActivity
import com.luka.travelplannerapp.activities.TaskListActivity
import com.luka.travelplannerapp.models.Board
import com.luka.travelplannerapp.models.User
import com.luka.travelplannerapp.utils.Constants

// ova klasa je odgovorna za sve stvari u Bazi
class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    // hocemo da imamo jos informacija o User-u odnosno korisniku aplikacije

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document", e
                )
            }
    }

    //  Funkcija za prijavljivanje koristeći Firebase i dobijanje korisničkih detalja iz Firestore baze podataka.
    fun loadUserData(
        activity: Activity,
        readBoardsList: Boolean = false
    ) { // default ce biti false citanje

        // Ovde prenosimo ime kolekcije iz koje želimo podatke.
        mFireStore.collection(Constants.USERS)
            // ID dokumenta da bi smo dobili polja korisnika.
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }

                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }

                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

                // Ovde pozivamo funkciju osnovne aktivnosti (Base activity) za prenos rezultata na nju.
            }.addOnFailureListener { e ->
                // START
                // Ovde se pozova funkcija osnovne aktivnosti za prenošenje rezultata na nju.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    "SignInUser",
                    "Error while getting loggedIn user details", e
                )
            }
    }

    fun getCurrentUserId(): String {

        // da nam vrati u IntroActivity ako je null currentUser
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    /**
     * Funkcija za ažuriranje podataka korisničkog profila u bazu podataka.
     * */

    fun updateUserProfileData(
        activity: MyProfileActivity,
        userHashMap: HashMap<String, Any>
    ) {

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                // ako je dobro prosao update Profile data
                Log.i(
                    activity.javaClass.simpleName, "Profile Data updated successfully!"
                )
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Obavestavanje rezultata uspeha
                activity.profileUpdateSuccess()
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating the profile!",
                    e
                )
                Toast.makeText(activity, "E!", Toast.LENGTH_SHORT).show()
            }

    }

    // zelimo fciju koja kreira Board i unosi u bazu podataka
    fun createBoard(activity: CreateBoardActivity, board: Board) {

        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Journey created successfully.")

                Toast.makeText(activity, "Journey created successfully.", Toast.LENGTH_SHORT).show()

                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a trip.",
                    e
                )
            }
    }

    // moramo da napravimo da se otpremi iz baze Board, to znaci da moram get iz baze da uradimo
    fun getBoardsList(activity: MainActivity) {
        // cekiramo Constants.BOARDS trenutni boards sa trenutnim id-em
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document -> // ovaj document je zapravo snapshot
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardsList: ArrayList<Board> = ArrayList()
                for (i in document.documents) { // idem kroz ceo document
                    val board = i.toObject(Board::class.java)!! // moramo !! jer je nullable
                    board.documentId = i.id // dobijamo i id documenta
                    boardsList.add(board)
                }

                // ovde prosledjujemo rezultat base aktivitiju
                activity.populateBoardsListTOUI(boardsList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)

            }

    }

    // kreiranje detalja o Board-u
    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        // slican kod kao kod getBoardlist
        // cekiramo Constants.BOARDS trenutni boards sa trenutnim id-em
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document -> // ovaj document je zapravo snapshot
                Log.i(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                // TODO ovde se otpremljuju detalji board-a
                activity.boardDetails(board)

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }

    }

    // ovde kreiramo listu, koja moze da bude hash mapa, hashMap ili hashList, updateTask liste
    fun addUpdateTaskList(activity: TaskListActivity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }

    }


    /**
     * Funkcija za dobijanje korisničkog ID-a trenutnog registrovanog korisnika.
     */
    fun getCurrentUserID(): String {
        // Instanca currentUser koristeći FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Promenljiva za dodeljivanje currentUserId ako nije null ili će u suprotnom biti prazan.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }
}

