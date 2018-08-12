package org.effervescence.app18.ca.utilities

import org.effervescence.app18.ca.EffervescenceCA.Companion.BASE_URL


object Constants{
    private const val BASE_URL = "https://fb78147e.ngrok.io/api/"
    const val REGULAR_USER_URL = BASE_URL + "regular_user/"
    const val CHANGE_PASSWORD_URL = BASE_URL + "password/change/"
    const val EVENTS_LIST_URL = BASE_URL + "events/"
    const val FILE_UPLOAD_URL = BASE_URL + "file_upload/"

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
    const val FB_ID_KEY = "fb_id"
    const val SUGGESTED_REFERRAL_KEY = "suggested_referral"
    const val NEW_PASSWORD_KEY = "new_password1"
    const val CONFIRM_NEW_PASSWORD_KEY = "new_password2"
    const val CURRENT_PASSWORD_KEY = "old_password"
    const val AUTHORIZATION_KEY = "Authorization"
    const val TOKEN_STRING = "Token "

    const val EVENT_ID_KEY = "id"
    const val EVENT_NAME_KEY = "event_name"
    const val EVENT_DESCRIPTION_KEY = "description"
    const val EVENT_PRIZE_KEY = "prize"
    const val EVENT_POINTS_KEY = "points"
    const val EVENT_FEE_KEY = "fee"

}


