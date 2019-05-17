package com.example.avocode.config

//Constants for the app
object Constants {
    //Fragments
    const val FRAGMENT_HOME = 1

    //LOCATION
    const val DEFAULT_LOCATION_UPDATE_TIME_INTERVAL = 10//IN SECONDS

    //FIRESTORE
    const val USER_COLLECTION = "USERS"

    object DocumentFields {
        const val PHONE = "phone"
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val DOB = "dob"
        const val GENDER = "gender"
        const val URI_PATH = "uriPath"
        const val PASSWORD = "password"
        const val FAMILY_CODE = "familyCode"
        const val GEO_LOCATION = "geoLocation"
    }
}
