package com.example.avocode.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.avocode.R
import com.example.avocode.adapters.RecyclerViewAdapterFamily
import com.example.avocode.models.FamilyData

class FamilyActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family)
        val familyList = ArrayList<FamilyData>()
        familyList.add(FamilyData("ya", "rger", R.drawable.ic_add_image))
        familyList.add(FamilyData("ya", "rger", R.drawable.ic_add_image))
        familyList.add(FamilyData("yrega", "rger", R.drawable.ic_add_image))
        familyList.add(FamilyData("yaerh", "rger", R.drawable.ic_add_image))
        familyList.add(FamilyData("tjya", "rehgger", R.drawable.ic_add_image))
        familyList.add(FamilyData("yytja", "rgeregr", R.drawable.ic_add_image))


        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_family_id)
        recyclerView.apply {
            val gridLayoutManager = GridLayoutManager(context, 2)
            layoutManager = gridLayoutManager
            adapter = RecyclerViewAdapterFamily(familyList)
        }
    }
}