package me.osku.doyouremember

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SEQ_CARD_TYPE = "seqCardType"
        private const val KEY_BRIEF_REVEAL = "briefReveal"
    }

    var seqCardType: String
        get() = prefs.getString(KEY_SEQ_CARD_TYPE, "數字牌") ?: "數字牌"
        set(value) = prefs.edit().putString(KEY_SEQ_CARD_TYPE, value).apply()

    var briefReveal: Boolean
        get() = prefs.getBoolean(KEY_BRIEF_REVEAL, false)
        set(value) = prefs.edit().putBoolean(KEY_BRIEF_REVEAL, value).apply()
}
