package dbmodel;

import com.orm.SugarRecord;

public class User extends SugarRecord {
    public String firstName, lastName, gender, dob, phone, uriPath;

    public User() {
    }

    public User(String firstName, String lastName, String gender, String dob, String phone, String uriPath) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dob = dob;
        this.phone = phone;
        this.uriPath = uriPath;
    }
}
