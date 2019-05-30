package com.example.avocode.models

import com.google.firebase.firestore.GeoPoint

//Model class to store Firestore data
class FirestoreUserModel(
    var firstName: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
    var dob: String? = null,
    var phone: String? = null,
    var uriPath: String? = null,
    var password: String? = null,
    var familyCode: String? = null,
    var geoPosition: GeoPoint? = null,
    var sharedMessage: String? = null)
