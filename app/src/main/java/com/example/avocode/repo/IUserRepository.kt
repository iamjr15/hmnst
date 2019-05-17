package com.example.avocode.repo

import com.example.avocode.models.FamilyMemberData
import com.example.avocode.models.FirestoreUserModel
import com.google.firebase.firestore.GeoPoint

typealias ResultListener = ((s: Boolean) -> Unit)?

interface IUserRepository {
    fun doesUserExist(phone: String, firestoreUserModel: FirestoreUserModel)

    fun addNewRegisteredUser(firestoreUserModel: FirestoreUserModel)

    fun getLoginUserByPhone(phone: String, password: String)

    fun getFamilyMembers(familyId: String, familyMembersListener: (familyMembers: Array<FamilyMemberData>) -> Unit)

    fun updateUserFamilyId(firestoreUserModel: FirestoreUserModel, resultListener: ResultListener = null)

    fun updateUserGeoLocation(phone: String, newLocation: GeoPoint, resultListener: ResultListener = null)
}
