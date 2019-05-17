package com.example.avocode.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.example.avocode.R
import com.example.avocode.adapters.RecyclerViewAdapterFamily
import com.example.avocode.models.FamilyMemberData
import com.example.avocode.models.FirestoreUserModel
import com.example.avocode.repo.UserImpl
import com.example.avocode.utils.Util
import com.orm.SugarRecord
import dbmodel.User
import kotlinx.android.synthetic.main.activity_family.*


class FamilyActivity: AppCompatActivity() {

    val familyList = ArrayList<FamilyMemberData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family)
        val oldFamilyCode = intent.getStringExtra(getString(R.string.familyCode))
        Util.showFamilyCode(textViewFamilyCode, oldFamilyCode)

        btnAddToFamily.setOnClickListener {
            val dialog = Dialog(this@FamilyActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                val lp = WindowManager.LayoutParams()
                lp.copyFrom(attributes)
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.MATCH_PARENT
                attributes = lp
            }
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_add_family_member)
            val etFamilyCode = dialog.findViewById<EditText>(R.id.editTextFamilyCode)
            etFamilyCode.requestFocus()
            dialog.findViewById<Button>(R.id.buttonGo).setOnClickListener {
                when {
                    !etFamilyCode.text.matches(Regex("[a-zA-Z0-9]{5}")) -> {
                        etFamilyCode.error = getString(R.string.message_family_code_not_fit)
                        etFamilyCode.requestFocus()
                    }
                    else -> {
                        //Get  user's profile
                        val user = SugarRecord.findById(User::class.java, 1)
                        if (user != null) {
                            val newFamilyCode = etFamilyCode.text.toString()
                            val userModel = FirestoreUserModel().apply {
                                firstName = user.firstName
                                lastName = user.lastName
                                gender = user.gender
                                dob = user.dob
                                phone = user.phone
                                uriPath = user.uriPath
                                familyCode = newFamilyCode// Set New Family Code
                            }
                            UserImpl(this@FamilyActivity).updateUserFamilyId(userModel) { success ->
                                if(success) {
                                    Util.showFamilyCode(textViewFamilyCode, newFamilyCode)
                                    //familyList.add()
                                }
                            }
                            dialog.dismiss()
                        }
                    }
                }
            }
            dialog.findViewById<Button>(R.id.buttonShare).setOnClickListener {
                val shareBody = "Hi! This is my Family Code: $oldFamilyCode"
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Avocode App")
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_code)))
                dialog.dismiss()
            }
            dialog .show()
        }

//        familyList.add(FamilyMemberData("ya", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("ya", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("yrega", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("yaerh", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("tjya", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("yytja", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("ya", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("ya", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("yrega", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("yaerh", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("tjya", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))
//        familyList.add(FamilyMemberData("yytja", "https://firebasestorage.googleapis.com/v0/b/hmnst001.appspot.com/o/images%2FMI_29032019_1322.jpg?alt=media&token=b21704ed-19be-418f-a3ad-05be953e7793"))

        rvFamilyGrid.apply {
            val gridLayoutManager = GridLayoutManager(context, 3)
            layoutManager = gridLayoutManager
            adapter = RecyclerViewAdapterFamily(familyList)
        }
    }
}