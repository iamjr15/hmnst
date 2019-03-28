package com.example.avocode.config;

//Constants for the app
public class Constants {
    //Fragments
    public static final int FRAGMENT_HOME = 1;


    //LOCATION
    public static final int DEFAULT_LOCATION_UPDATE_TIME_INTERVAL = 10;//IN SECONDS

    //FIRESTORE
    public static final String USER_COLLECTION = "USERS";

    public static class DocumentFields {
        public static final String PHONE = "phone";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String DOB = "dob";
        public static final String GENDER = "gender";
        public static final String URI_PATH = "uriPath";
        public static final String PASSWORD = "password";
    }
}
