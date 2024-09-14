package com.luka.travelplannerapp.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.adapters.TaskListItemsAdapter
import com.luka.travelplannerapp.firebase.FirestoreClass
import com.luka.travelplannerapp.models.Board
import com.luka.travelplannerapp.models.Card
import com.luka.travelplannerapp.models.Task
import com.luka.travelplannerapp.utils.Constants

class TaskListActivity : BaseActivity() {

    // Globalna promenljiva za Board Details
    private lateinit var mBoardDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        // proveravam da li je DOCUMENT_ID prenet kroz Intent, kako bi se preuzeli podaci o Board-u
        var boardDocumentId = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }


        showProgressDialog(resources.getString(R.string.please_wait))
        // pozivam metodu koja dohvata detalje o trenutnom Board-u koristeci boardDocumentId
        FirestoreClass().getBoardDetails(this@TaskListActivity, boardDocumentId)
    }

    // sve dobijamo iz Board-a tj ove globalne mBoardDetails promenljive, znaci sve dopremljujemo iz Board-a
    // pocev od title, name..
    // postavljanje Toolbar-a kao akcioni bar za aktivnosti(Activity)
    private fun setupActionBar() {
        val toolbarTaskListActivity = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        //Postavlja "Back" dugme i indikator u Toolbar-u, kao i naslov koji
        // se postavlja na ime "Board-a"
        setSupportActionBar(toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }
        toolbarTaskListActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }


    fun boardDetails(board: Board) {

        // Nakon što zamenim promenljivu parametra globalnom, tako da će se od
        // nadalje koristiti globalna promenljiva.
        mBoardDetails = board

        val rvTaskList = findViewById<RecyclerView>(R.id.rv_task_list)

        hideProgressDialog()
        // zelim da setujem progres bar
        setupActionBar()

        // Ovde dodajemo prikaz stavke za dodavanje liste zadataka za Board.
        val addTaskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        rvTaskList.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        rvTaskList.setHasFixedSize(true)

        // Pravim instancu TaskListItemsAdapter i proslecujem joj listu zadataka(task lists)
        val adapter = TaskListItemsAdapter(this@TaskListActivity, board.taskList)
        rvTaskList.adapter = adapter // Attach the adapter to the recyclerView.
    }

    /**
     * Funkcija za dobijanje rezultata dodavanja ili ažuriranja liste zadataka.
     */
    fun addUpdateTaskListSuccess() {
        // kada napravimo u listi novi ListItem zelim da se zatvori taj Dialog i da se otvori novi za kreiranje novom ListItem, to je logika samog otvaranja i zatvaranja
        hideProgressDialog()

        // prikazivanje progres dijaloga
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)

    }

    /**
     * Funkcija za dobijanje imena liste zadataka iz klase adaptera koju ću koristiti za kreiranje nove liste zadataka u bazi podataka.
     */
    fun createTaskList(taskListName: String) {

        Log.e("Task List Name", taskListName)

        // Kreiram i dodeljujem detalje zadatka
        val task = Task(taskListName, FirestoreClass().getCurrentUserID())
        // Dodajem task na prvu poziciju ArrayList-i
        mBoardDetails.taskList.add(0, task) // Dodajem task na prvu poziciju ArrayList-i
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1) // // Uklanjam poslednju poziciju pošto sam ručno dodao stavku za dodavanje liste zadataka.

        // prikazivanje progress dialog-a
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    /**
     * Funkcija za dobijanje rezultata dodavanja ili ažuriranja liste zadataka(task list).
     */
    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        // Uklanjanje poslednjeg item-a
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }


    /**
     * Funkcija za brisanje liste zadataka iz baze podataka.
     */
    fun deleteTaskList(position: Int){

        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        // Prikazivanje progress dialog-a.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {

        // uklanjamo last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())

        val card = Card(cardName, FirestoreClass().getCurrentUserId(), cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards // task list pozicija gde zelimo Card da imamo
        cardsList.add(card)

        // mozemo da kreiramo Task
        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        // moramo da zamenimo stari task sa novim, tj da ga update-ujemo
        mBoardDetails.taskList[position] = task

        // da prikazemo showProgress
        showProgressDialog(resources.getString(R.string.please_wait))

        // na kraju update-ujemo Board
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }
}