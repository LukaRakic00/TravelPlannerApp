package com.luka.travelplannerapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.models.Board

//Ova klasa ima zadatak
// da poveže podatke iz liste Board objekata sa prikazima u RecyclerView-u.
open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Povecava prikaze predmeta koji su dizajnirani u xml layout
     *
     * kreiram novu
     * {@link ViewHolder} i inicijalizujem neka privatna polja koja će koristiti RecyclerView.
     */
    // ukratko kreira novi prikaz za svaki element u RecyclerView-u
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_board,
                parent,
                false
            )
        )
    }

    /**
     * Veze svaku stavku u ArrayList za prikaz
     *
     * Poziva se kada je RecyclerView potreban novi {@link ViewHolder} datog tipa za predstavljanje
     * item-a.
     *
     * Ovaj novi ViewHolder treba da bude napravljen sa novim prikazom koji može predstavljati stavke
     * datog tipa. Možete kreirati novi prikaz ručno ili ga uzme iz XML layout-a
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]


        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_board_image))

            holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by).text = "Created By : ${model.createdBy}"

            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    /**
     * Dobija broj stavki u listi
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Funkcija za OnClickListener gde je interfejs očekivani parametar.
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * Interfejs za onclick stavke.
     */
    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    /**
     * ViewHolder opisuje prikaz stavke i metapodatke o njenom mestu unutar RecyclerView-a.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}