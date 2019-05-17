package com.example.avocode.activity

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.Window
import com.example.avocode.R
import com.example.avocode.adapters.RecyclerViewAdapterFamily
import com.example.avocode.models.FamilyMemberData
import kotlinx.android.synthetic.main.activity_family.*


class FamilyActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.avocode.R.layout.activity_family)
        textViewFamilyCode.text = String.format(getString(R.string.family_code),
                intent.getStringExtra(getString(R.string.familyCode)))

        btnAddToFamily.setOnClickListener {
            val dialog = Dialog(this@FamilyActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(true)
            dialog.setContentView(com.example.avocode.R.layout.dialog_add_family_member)
//            val body = dialog .findViewById(R.id.body) as TextView
//            body.text = title
//            val yesBtn = dialog .findViewById(R.id.yesBtn) as Button
//            val noBtn = dialog .findViewById(R.id.noBtn) as TextView
//            yesBtn.setOnClickListener {
//                dialog .dismiss()
//            }
//            noBtn.setOnClickListener { dialog .dismiss() }
            dialog .show()
        }

        val familyList = ArrayList<FamilyMemberData>()
        familyList.add(FamilyMemberData("ya", "rger", R.drawable.ic_add_photo))
        familyList.add(FamilyMemberData("ya", "rger", R.drawable.ic_add_photo))
        familyList.add(FamilyMemberData("yrega", "rger", R.drawable.ic_add_photo))
        familyList.add(FamilyMemberData("yaerh", "rger", R.drawable.ic_add_photo))
        familyList.add(FamilyMemberData("tjya", "rehgger", R.drawable.ic_add_photo))
        familyList.add(FamilyMemberData("yytja", "rgeregr", R.drawable.ic_add_photo))

        rvFamilyGrid.apply {
            val gridLayoutManager = GridLayoutManager(context, 3)
            layoutManager = gridLayoutManager
            adapter = RecyclerViewAdapterFamily(familyList)
        }
    }
}