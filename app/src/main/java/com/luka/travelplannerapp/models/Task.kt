package com.luka.travelplannerapp.models

import android.os.Parcel
import android.os.Parcelable

// svaki Board moze da ima vise zadataka
data class Task(
    var title: String = "",
    val createdBy: String = "",
    val cards: ArrayList<Card> = ArrayList()

) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.createTypedArrayList(Card.CREATOR)!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(createdBy)
        writeTypedList(cards)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Task> = object : Parcelable.Creator<Task> {
            override fun createFromParcel(source: Parcel): Task = Task(source)
            override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
        }
    }


}