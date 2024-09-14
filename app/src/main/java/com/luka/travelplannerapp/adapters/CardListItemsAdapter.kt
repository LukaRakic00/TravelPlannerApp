package com.luka.travelplannerapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luka.travelplannerapp.R
import com.luka.travelplannerapp.models.Card
import android.view.View
import android.widget.TextView

// TODO Adapter klasa za listu kartica
open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates (prikazuje) stavke u prikazu koje je dizajniran u XML rasporedu.
     *
     * Kreira novi {@link ViewHolder} i inicijalizuje neke privatne atribute koji će se koristiti od strane RecyclerView-a.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card, // item_card treba nam poseban layout
                parent,
                false
            )
        )
    }

    /**
     * Povezuje svaku stavku u ArrayList sa prikazom.
     *
     * Poziva se kada RecyclerView treba novi {@link ViewHolder} odgovarajuće vrste za predstavljanje stavke.
     *
     * Ovaj novi ViewHolder treba biti konstruisan sa novim prikazom koji može predstavljati stavke
     * odgovarajuće vrste. Možete ili ručno kreirati novi View ili ga inflatovati iz XML
     * rasporeda.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name
        }
    }

    /**
     * Vraća broj stavki u listi.
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
     * Interfejs za klik na stavke.
     */
    interface OnClickListener {
        fun onClick(position: Int, card: Card)
    }

    /**
     * ViewHolder opisuje stavku u prikazu i metapodatke o njenom mestu unutar RecyclerView-a.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
