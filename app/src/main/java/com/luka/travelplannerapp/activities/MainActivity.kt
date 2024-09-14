package com.luka.travelplannerapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.adapters.BoardItemsAdapter
import com.luka.travelplannerapp.databinding.ActivityMainBinding
import com.luka.travelplannerapp.firebase.FirestoreClass
import com.luka.travelplannerapp.models.Board
import com.luka.travelplannerapp.models.User
import com.luka.travelplannerapp.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Kreiramo companion object i konstantnu promenljivu za rezultat ekrana(Screen result) My profile )
    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12// za osvezavanje Board-a
    }

    // ovo je promenjiva koja se koristi umesto findViewById, odnosno findViewById je depracted nacin za dobijanje id iz xml layout-a
    private lateinit var binding: ActivityMainBinding


    // TODO: Definišemo ActivityResultLauncher za pokretanje aktivnosti za rezultat, jer nam je metoda startActivityForResult depracted
    private val myProfileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            // Obradi rezultat
            FirestoreClass().loadUserData(this@MainActivity)
        }
    }

    private val createBoardLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Dobijamo novu ažuriranu listu na board-u
            FirestoreClass().getBoardsList(this@MainActivity)
        }
    }

    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* omogućavaju da koristim View Binding
         * kako bi postavio ceo layout iz XML-a kao glavni sadržaj aktivnosti.
         * */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        // navView je zapravo side nav kao hamburger meni iz activity_main layout-a,
        // to je zapravo DrawerLayout

        binding.navView.setNavigationItemSelectedListener(this)

        // ucitavamo korisnika i Boards,
        // page ostaje na load ali detalji Boarda se ne azuriraju i to moram da popravim
        FirestoreClass().loadUserData(
            this@MainActivity,
            true
        )

        // ovo je zapravo plus u donjem desnom ulogu mog app_bar_main layout-a
        // koji je uvezen iz app_bar_main layout-a, zato ne mogu da mu pristupam preko binding nego na stari nacin, da ne biih pravio novi binding
        val fabCreateBoard = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            // ako pritisnemo plus dugme ono ce nam otovriti CreateBoardActivity
            val intent = Intent(
                this@MainActivity,
                CreateBoardActivity::class.java
            )
            intent.putExtra(Constants.NAME, mUserName)
            createBoardLauncher.launch(intent)
        }
    }

    // postavlja Toolbar kao akcioni bar za aktivnost
    // i dodaje funkcionalnost za dugme koje će otvoriti navigacioni meni (drawer).
    private fun setupActionBar() {
        val toolbarMainActivity = findViewById<Toolbar>(R.id.toolbar_main_activity)
        // postavljanje Toolbar-a kao akcioni bar za aktivnosti(Activity),
        // to znaci da ce toolbar postati primarni UI element na vrhu ekrana
        // gde se obično prikazuje naslov aktivnosti, dugmad, i meni.
        setSupportActionBar(toolbarMainActivity)

        toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawler()
        }
    }

    // ova fcija kontrolise otvaranje i zatvaranje akcionog menija
    private fun toggleDrawler() {
        // kontrolisanje navigacionog drawer-a unutar DrawerLayouta, koristeci GravityCompat.START,
        // GravityCompat da bi se iskontrolisala strana, obicno je sa leve strane akcioni meni
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // ovde podesavam da ako je drawerLayout otvoren da ga centrira da bude gracitacija na .START u suprotnom ako nije otvoren da pozove doubleBackToExit()

    /*
    *  koristi zato što unutar ove metode nije pozvana super metoda super.onBackPressed().
    * To je zato što želiš da prilagodiš ponašanje dugmeta "Back" bez pozivanja podrazumevanog ponašanja (koje bi moglo zatvoriti aktivnost).
    * Anotacija služi da se spreči upozorenje kompajlera o nedostatku poziva super metode.
    *
    * */
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    // TODO: Ažuriranje onNavigationItemSelected metode da koristi ActivityResultLauncher,
    //  tj ovim sam resio problem sa tim da mi bude azuriran DrawerLayout nakon izmene
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Pokrecemo aktivnost MyProfile za rezultat ako pritisnem myProfile
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                myProfileLauncher.launch(intent) // Koristi ActivityResultLauncher umesto startActivityForResult
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                // Zatvaram trenutnu aktivnost
                // kako bi sprečio korisnika da se vrati na prethodni ekran nakon odjave.
                finish()
            }
        }
        // gasim akcioni meni
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        val navUserImage = findViewById<CircleImageView>(R.id.nav_user_image)

        mUserName = user.name // ovo je da bi dobili name, tj da bi ga inicijalizovali mUsername

        // Učitavanje korisničke slike u ImageView.
        Glide
            .with(this@MainActivity)
            .load(user.image) // URL slike, gde slika treba da bude u bazi
            .centerCrop() // Tip razmera slike
            .placeholder(R.drawable.ic_user_place_holder) // Default place holder
            .into(navUserImage) // prikaz u koji će slika biti učitana

        val tvUsername = findViewById<TextView>(R.id.tv_username)
        tvUsername.text = user.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this@MainActivity)
        }
    }

    // za dodavanje planera na Main
    fun populateBoardsListTOUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()
        // ovo je u content_main layout
        val rvBoardsList = findViewById<RecyclerView>(R.id.rv_boards_list)
        val tvNoboardsAvailable = findViewById<TextView>(R.id.tv_no_boards_available)
        if (boardsList.size > 0) {
            // posto nam je u content_main layout-u recyclerView setovan na gone
            // i ako imamo vec nesto upisano
            // u bazu neophodno je da ga setujemo na visible tj da bude vidljiv
            // takoce textView mora da se skloni ako ima nesto u bazi
            rvBoardsList.visibility = View.VISIBLE
            tvNoboardsAvailable.visibility = View.GONE

            rvBoardsList.layoutManager = LinearLayoutManager(this@MainActivity)
            rvBoardsList.setHasFixedSize(true)

            // potreban je Adapter
            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)
            rvBoardsList.adapter = adapter

            // dodavanje click event za stavku na boards-u i pokrecem TaskListActiviti)
            adapter.setOnClickListener(object :
                BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId) // U board klasi imam DOCUMENT_ID u koji zelim da upisem nesto, tj detalje o putovanju
                    startActivity(intent)
                }
            })

        } else {
            // ako nema nista jos od putovanja
            rvBoardsList.visibility = View.GONE
            tvNoboardsAvailable.visibility = View.VISIBLE
        }
    }

}


