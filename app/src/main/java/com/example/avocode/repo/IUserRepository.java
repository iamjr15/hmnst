package com.example.avocode.repo;

import com.example.avocode.models.FirestoreUserModel;

public interface IUserRepository {
    void doesUserExist(String phone,FirestoreUserModel firestoreUserModel);

    void addNewRegisteredUser(FirestoreUserModel firestoreUserModel);

    void getLoginUserByPhone(String phone, String password);
}
