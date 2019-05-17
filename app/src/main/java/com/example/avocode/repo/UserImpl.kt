package com.example.avocode.repo

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.avocode.AvaApplication
import com.example.avocode.R
import com.example.avocode.activity.HomeActivity
import com.example.avocode.config.Constants
import com.example.avocode.models.FamilyMemberData
import com.example.avocode.models.FirestoreUserModel
import com.example.avocode.utils.Util
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.*

//helper class for login and registration purpose
class UserImpl(private val activity: Activity) : IUserRepository {
    private val db: FirebaseFirestore?

    private val app: AvaApplication = AvaApplication.instance
    private val util: Util

    init {
        db = app.dbInstance
        util = Util(activity)
    }

    //Check user exist or not
    override fun doesUserExist(phone: String, firestoreUserModel: FirestoreUserModel) {
        util.showLoading(activity.getString(R.string.message_creating_profile))
        val docSnapshot = db!!.collection(Constants.USER_COLLECTION).document(phone).get()
        docSnapshot.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // user already exist in database
                    util.toast(activity.getString(R.string.message_user_already_exist))
                    util.hideLoading()
                } else {
                    //user does not exist in database
                    addNewRegisteredUser(firestoreUserModel)
                }
            } else {
                //user does not exist in database
                addNewRegisteredUser(firestoreUserModel)
            }
        }
    }

    //Add user
    override fun addNewRegisteredUser(firestoreUserModel: FirestoreUserModel) {
        val user = HashMap<String, Any>()
        user[Constants.DocumentFields.FIRST_NAME] = firestoreUserModel.firstName!!
        user[Constants.DocumentFields.LAST_NAME] = firestoreUserModel.lastName!!
        user[Constants.DocumentFields.PASSWORD] = firestoreUserModel.password!!
        user[Constants.DocumentFields.DOB] = firestoreUserModel.dob!!
        user[Constants.DocumentFields.GENDER] = firestoreUserModel.gender!!
        user[Constants.DocumentFields.PHONE] = firestoreUserModel.phone!!
        user[Constants.DocumentFields.URI_PATH] = firestoreUserModel.uriPath!!
        user[Constants.DocumentFields.FAMILY_CODE] = firestoreUserModel.familyCode!!
        val newUser = db!!.collection(Constants.USER_COLLECTION).document(firestoreUserModel.phone!!).set(user)
        newUser.addOnSuccessListener {
            Log.d(TAG, "User was successfully added")
            util.saveUser(firestoreUserModel.firstName!!,
                    firestoreUserModel.lastName!!,
                    firestoreUserModel.gender!!,
                    firestoreUserModel.dob!!,
                    firestoreUserModel.phone!!,
                    firestoreUserModel.uriPath!!,
                    firestoreUserModel.familyCode!!)
            val intent = Intent(activity, HomeActivity::class.java)
            intent.putExtra(activity.getString(R.string.firstName), firestoreUserModel.firstName)
            intent.putExtra(activity.getString(R.string.lastName), firestoreUserModel.lastName)
            intent.putExtra(activity.getString(R.string.gender_label), firestoreUserModel.gender)
            intent.putExtra(activity.getString(R.string.dob), firestoreUserModel.dob)
            intent.putExtra(activity.getString(R.string.password_label), firestoreUserModel.password)
            intent.putExtra(activity.getString(R.string.phone_label), firestoreUserModel.phone)
            intent.putExtra(activity.getString(R.string.uriPath), firestoreUserModel.uriPath)
            intent.putExtra(activity.getString(R.string.familyCode), firestoreUserModel.familyCode)
            util.hideLoading()
            // set the new task and clear flags
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }.addOnFailureListener { e ->
            util.hideLoading()
            Log.d(TAG, "Error has occured " + e.message)
            util.toast(activity.getString(R.string.message_user_registration_failed))
        }
    }

    //Login user
    override fun getLoginUserByPhone(phone: String, password: String) {
        util.showLoading(activity.getString(R.string.message_logging))
        db!!.collection(Constants.USER_COLLECTION)
                .whereEqualTo(Constants.DocumentFields.PHONE, phone)
                .whereEqualTo(Constants.DocumentFields.PASSWORD, password).addSnapshotListener { queryDocumentSnapshots, e ->
                    Log.d("onEvent", "onEvent")
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                        var firestoreUserModel: FirestoreUserModel? = null
                        for (snapshot in queryDocumentSnapshots) {
                            firestoreUserModel = snapshot.toObject<FirestoreUserModel>(FirestoreUserModel::class.java)
                        }
                        if (firestoreUserModel != null) {
                            util.saveUser(firestoreUserModel.firstName!!,
                                    firestoreUserModel.lastName!!,
                                    firestoreUserModel.gender!!,
                                    firestoreUserModel.dob!!,
                                    firestoreUserModel.phone!!,
                                    firestoreUserModel.uriPath!!,
                                    firestoreUserModel.familyCode!!)
                            val intent = Intent(activity, HomeActivity::class.java)
                            intent.putExtra(activity.getString(R.string.firstName), firestoreUserModel.firstName)
                            intent.putExtra(activity.getString(R.string.last_name), firestoreUserModel.lastName)
                            intent.putExtra(activity.getString(R.string.gender_label), firestoreUserModel.gender)
                            intent.putExtra(activity.getString(R.string.dob), firestoreUserModel.dob)
                            intent.putExtra(activity.getString(R.string.password_label), firestoreUserModel.password)
                            intent.putExtra(activity.getString(R.string.phone_label), firestoreUserModel.phone)
                            intent.putExtra(activity.getString(R.string.uriPath), firestoreUserModel.uriPath)
                            intent.putExtra(activity.getString(R.string.familyCode), firestoreUserModel.familyCode)
                            // set the new task and clear flags
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            util.hideLoading()
                            activity.startActivity(intent)
                        } else {
                            util.hideLoading()
                            util.toast(activity.getString(R.string.message_credential_invalid))
                        }
                    } else {
                        util.hideLoading()
                        util.toast(activity.getString(R.string.message_credential_invalid))
                    }
                }
    }

    override fun getFamilyMembers(familyId: String, familyMembersListener: (familyMembers: Array<FamilyMemberData>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Update user family Code
    override fun updateUserFamilyId(firestoreUserModel: FirestoreUserModel, resultListener: ResultListener) {
        util.showLoading(activity.getString(R.string.message_updating_family_id))
        db!!.collection(Constants.USER_COLLECTION).document(firestoreUserModel.phone!!)
                .update(Constants.DocumentFields.FAMILY_CODE, firestoreUserModel.familyCode!!)
                .addOnSuccessListener {
                    Log.d(TAG, "User family Id was successfully updated")
                    util.saveUser(firestoreUserModel.firstName!!,
                            firestoreUserModel.lastName!!,
                            firestoreUserModel.gender!!,
                            firestoreUserModel.dob!!,
                            firestoreUserModel.phone!!,
                            firestoreUserModel.uriPath!!,
                            firestoreUserModel.familyCode!!)
                    util.hideLoading()
                    resultListener?.invoke(true)
                }
                .addOnFailureListener { e ->
                    util.hideLoading()
                    Log.d(TAG, "Error has occured " + e.message)
                    util.toast(activity.getString(R.string.message_updating_family_id_failed))
                    resultListener?.invoke(false)
                }
    }

    override fun updateUserGeoLocation(phone: String, newLocation: GeoPoint, resultListener: ResultListener) {
        db!!.collection(Constants.USER_COLLECTION).document(phone)
                .update(Constants.DocumentFields.GEO_LOCATION, newLocation)
                .addOnSuccessListener {
                    resultListener?.invoke(true)
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Error has occured " + e.message)
                    resultListener?.invoke(false)
                }
    }

    companion object {
        private val TAG = UserImpl::class.java.simpleName
    }
}
