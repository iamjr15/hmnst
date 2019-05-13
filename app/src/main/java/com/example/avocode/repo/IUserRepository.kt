package com.example.avocode.repo

import com.example.avocode.models.FirestoreUserModel

interface IUserRepository {
    fun doesUserExist(phone: String, firestoreUserModel: FirestoreUserModel)

    fun addNewRegisteredUser(firestoreUserModel: FirestoreUserModel)

    fun getLoginUserByPhone(phone: String, password: String)
}
