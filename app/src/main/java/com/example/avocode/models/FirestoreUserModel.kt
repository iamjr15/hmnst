package com.example.avocode.models

//Model class to store Firestore data
class FirestoreUserModel(
    var firstName: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
    var dob: String? = null,
    var phone: String? = null,
    var uriPath: String? = null,
    var password: String? = null,
    var familyCode: String? = null)
