package com.luka.travelplannerapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luka.travelplannerapp.databinding.ItemTaskBinding
import com.luka.travelplannerapp.activities.TaskListActivity
import com.luka.travelplannerapp.models.Task

// u ovoj klasi Adaptera sam umesto findViewById koristio viewBinding
// sto se koristi za bolju citljivost i strukturiranost koda, u build.gradle sam takodje dodao viewBinding i stavio sam ga na true
open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<TaskListItemsAdapter.MyViewHolder>() {

    // Inflatišem layout za stavku liste koristeći View Binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    // Povezujem podatke iz modela sa View-om
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        with(holder.binding) {
            // Ako je poslednja stavka, prikazujem opciju za dodavanje liste
            if (position == list.size - 1) {
                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE
            } else {
                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
            }

            // Postavljam naslov liste
            tvTaskListTitle.text = model.title

            // Klikom na "Dodaj listu" prikazujem formu za unos naziva liste
            tvAddTaskList.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddTaskListName.visibility = View.VISIBLE
            }

            // Klikom na "X" pored naziva liste vraćam formu za unos naziva liste u početni prikaz
            ibCloseListName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddTaskListName.visibility = View.GONE
            }

            // Klikom na dugme za potvrdu dodajem novu listu
            ibDoneListName.setOnClickListener {
                val listName = etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please enter a list name.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            // Klikom na dugme za uređivanje otvaram formu za izmenu naziva liste
            ibEditListName.setOnClickListener {
                etEditTaskListName.setText(model.title)
                llTitleView.visibility = View.GONE
                cvEditTaskListName.visibility = View.VISIBLE
            }

            // Klikom na "X" pored naziva liste vraćam početni prikaz
            ibCloseEditableView.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditTaskListName.visibility = View.GONE
            }

            // Klikom na dugme za potvrdu izmene ažuriram naziv liste
            ibDoneEditListName.setOnClickListener {
                val listName = etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please enter a list name.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            // Klikom na dugme za brisanje prikazujem dijalog za potvrdu brisanja liste
            ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            // Dodajem ClickListener  za dodavanje kartice na listu zadataka.
            tvAddCard.setOnClickListener {
                tvAddCard.visibility = View.GONE
                cvAddCard.visibility = View.VISIBLE
            }

            ibDoneCardName.setOnClickListener {
                tvAddCard.visibility = View.VISIBLE
                cvAddCard.visibility = View.GONE
            }


            ibDoneCardName.setOnClickListener {

                val cardName = etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        // dodaje karticu
                        context.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT).show()
                }
            }
            rvCardList.layoutManager =
                LinearLayoutManager(context)

            rvCardList.setHasFixedSize(true)

            val adapter = CardListItemsAdapter(context, model.cards)
            rvCardList.adapter = adapter
        }



    }

    // Vraćam broj stavki u listi
    override fun getItemCount(): Int = list.size

    // ViewHolder za prikaz stavki u RecyclerView
    class MyViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    // Metoda za prikaz dijaloga za potvrdu brisanja liste
    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Warning")
            .setMessage("Are you sure you want to delete $title?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                if (context is TaskListActivity) {
                    context.deleteTaskList(position)
                }
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
