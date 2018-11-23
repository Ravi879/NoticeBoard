package coms.dypatil.noticeboard.util.validation

class FormFieldValidation {

    companion object {

        fun isEmailValid(email: String): Boolean = Validation.isValid(email, "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2,12}")

        fun isNameValid(name: String) = Validation.isValid(name, "[a-zA-Z]{4,12}")

        fun isPhoneNoValid(phoneNo: String) = Validation.isValid(phoneNo, "[+]?[0-9]{10}")

        fun isPasswordValid(password: String) = Validation.isValid(password, "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{6,}\$")

    }

}