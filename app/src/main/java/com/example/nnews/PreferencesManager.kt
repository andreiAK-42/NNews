import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.nnews.News
import com.example.nnews.SaveNews
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.log

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val gson: Gson

    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gson = Gson()
    }

    fun saveClassList(classList: MutableList<SaveNews?>?) {
        Log.e("ааааааа", classList?.size.toString())
        val jsonString = gson.toJson(classList)
        sharedPreferences.edit().putString(LIST_KEY, jsonString).apply()
    }

    val classList: MutableList<SaveNews?>?
        get() {
            val jsonString = sharedPreferences.getString(LIST_KEY, null)
            val type = object : TypeToken<ArrayList<SaveNews?>?>() {}.type
            return gson.fromJson<MutableList<SaveNews?>>(jsonString, type)
        }

    companion object {
        private const val PREFS_NAME = "LoveNews"
        private const val LIST_KEY = "LoveNewsList"
    }
}