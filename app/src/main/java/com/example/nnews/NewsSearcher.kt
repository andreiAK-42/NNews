package com.example.nnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class NewsSearcher: AppCompatActivity() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    public lateinit var adapter: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.news_searcher)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nw_main_search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSearch: ImageView = findViewById(R.id.im_search)
        val etSearch: EditText = findViewById(R.id.et_search)
        btnSearch.setOnClickListener {
            scope.launch {
                val news =
                    loadNews("https://newsapi.org/v2/everything?q=" + etSearch.text + "&apiKey=a5a72530f7c9425893230093aa01521b")
                val cleanNews: List<News> =
                    news.articles.filter { news -> news.urlToImage != null && news.urlToImage.length > 0 && news.title != "[Removed]" && !news.content.isNullOrEmpty() }

                withContext(Dispatchers.Main) {
                    val recyclerView = findViewById<RecyclerView>(R.id.rView)
                    recyclerView.layoutManager = GridLayoutManager(this@NewsSearcher, 1)
                    recyclerView.adapter = NewsAdapter(cleanNews)
                    adapter = recyclerView
                }
                Log.w("Ааааааа ЗАПУСК", cleanNews.size.toString())
            }
        }

        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }

    public suspend fun loadNews(url: String): ListNews {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    ListNews("false", 0, emptyList())
                } else {
                    val responseBody = response.body?.string()
                    val gson = Gson()
                    val type = object : TypeToken<ListNews>() {}.type
                    gson.fromJson(responseBody, type)
                }
            } catch (e: Exception) {
                ListNews("false", 0, emptyList())
            }
        }
    }
}

class SearchNewsAdapter(private val news: List<News>) :
    RecyclerView.Adapter<SearchNewsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // val picture: ImageView = itemView.findViewById(R.id.newsImage)
        val title: TextView = itemView.findViewById(R.id.newsName)
        val text: TextView = itemView.findViewById(R.id.newsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rview_news, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsItem = news[position]
        val imageUrl = newsItem.urlToImage

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, NewsViewer::class.java)
            intent.putExtra("newsImageUrl", imageUrl)
            intent.putExtra("newsTitle", newsItem.title)
            intent.putExtra("newsText", newsItem.content)
            intent.putExtra("newsSource", newsItem.url)
            intent.putExtra("newsPublishDate", newsItem.publishedAt)
            holder.itemView.context.startActivity(intent)
        }

        holder.text.text = newsItem.content
        holder.title.text = newsItem.title
    }

    override fun getItemCount(): Int {
        return news.size
    }
}