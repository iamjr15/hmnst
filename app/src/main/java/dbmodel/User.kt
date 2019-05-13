package dbmodel

import com.orm.SugarRecord

class User(var firstName: String,
           var lastName: String,
           var gender: String,
           var dob: String,
           var phone: String,
           var uriPath: String): SugarRecord()
