package coms.dypatil.noticeboard.util.validation

import java.util.regex.Pattern


class Validation {
    companion object {
        fun isValid(value: String, regex: String): Boolean {
            val pattern: Pattern = Pattern.compile(regex)
            return pattern.matcher(value).matches()
        }
    }
}