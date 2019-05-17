package com.example.avocode.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.avocode.R
import com.example.avocode.models.FamilyMemberData

class RecyclerViewAdapterFamily(private val familyMemberList: ArrayList<FamilyMemberData>) : RecyclerView.Adapter<RecyclerViewAdapterFamily.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, p: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.family_member_item, viewGroup, false)
        return ViewHolder(v)
    }
    override fun getItemCount(): Int {
        return familyMemberList.size
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, pos: Int) {
        viewHolder.name.text = familyMemberList[pos].fullName
        viewHolder.count.setImageResource(familyMemberList[pos].familyImage)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.textview_family_name_id)!!
        val count = itemView.findViewById<ImageView>(R.id.imageview_family_image_id)!!
    }
}