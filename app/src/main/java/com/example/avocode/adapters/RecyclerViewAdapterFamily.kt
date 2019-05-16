package com.example.avocode.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.avocode.R
import com.example.avocode.models.FamilyData

class RecyclerViewAdapterFamily(val familyList: ArrayList<FamilyData>) : RecyclerView.Adapter<RecyclerViewAdapterFamily.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.cardview_item_family, p0, false)
        return ViewHolder(v);
    }
    override fun getItemCount(): Int {
        return familyList.size
    }
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        p0.name?.text = familyList[p1].fullName
        p0.count?.setImageResource(familyList[p1].familyImage)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.textview_family_name_id)
        val count = itemView.findViewById<ImageView>(R.id.imageview_family_image_id)

    }
}