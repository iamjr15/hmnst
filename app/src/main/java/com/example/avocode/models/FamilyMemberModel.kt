package com.example.avocode.models

import com.google.firebase.firestore.GeoPoint

class FamilyMemberModel(
        var firstName: String? = null,
        var lastName: String? = null,
        var gender: String? = null,
        var dob: String? = null,
        var phone: String? = null,
        var uriPath: String? = null,
        var familyCode: String? = null,
        var geoPosition: GeoPoint? = null
)