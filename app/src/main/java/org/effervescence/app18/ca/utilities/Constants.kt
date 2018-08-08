package org.effervescence.app18.ca.utilities

import org.effervescence.app18.ca.EffervescenceCA.Companion.BASE_URL


object Constants{
    const val BASE_URL = "https://36f1d73f.ngrok.io/api/"
    const val REGULAR_USER_URL = BASE_URL + "regular_user/"
    const val CHANGE_PASSWORD_URL = BASE_URL + "password/change/"
    const val LEADERBOARD_URL = BASE_URL + "leaderboard/"

    const val MY_SHARED_PREFERENCE = "mypreference"
    const val KEY_TOKEN = "token"
    const val TOKEN_DEFAULT = "0"

    const val NAME_KEY = "name"
    const val NAME_DEFAULT = "name_NA"
    const val COLLEGE_NAME_KEY = "college"
    const val COLLEGE_NAME_DEFAULT = "college_NA"
    const val DATE_OF_BIRTH_KEY = "birthday"
    const val DATE_OF_BIRTH_DEFAULT = "0000-00-00"
    const val GENDER_KEY = "gender"
    const val GENDER_DEFAULT = "gender_NA"
    const val MOBILE_NO_KEY = "phone"
    const val MOBILE_NO_DEFAULT = "0000000000"
    const val NEW_PASSWORD_KEY = "new_password1"
    const val CONFIRM_NEW_PASSWORD_KEY = "new_password2"
    const val CURRENT_PASSWORD_KEY = "old_password"
    const val AUTHORIZATION_KEY = "Authorization"
    const val TOKEN_STRING = "Token "

}


