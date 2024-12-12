package com.example.nnews

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date


class MainActivity : AppCompatActivity() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private val scope2 = CoroutineScope(Dispatchers.Main + job)
    public lateinit var adapter: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scope2.launch {
            val category = listOf("general", "business", "health", "science", "technology", "sports", "entertainment")
            val recyclerView3 = findViewById<RecyclerView>(R.id.rViewCategoryNews)
            val layoutManager3 = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            recyclerView3.layoutManager = layoutManager3
            recyclerView3.adapter = CategoryNewsAdapter(category, this@MainActivity)
        }

        scope.launch {
            val news = loadNews("https://newsapi.org/v2/top-headlines?country=us&apiKey=a5a72530f7c9425893230093aa01521b")
            val cleanNews: List<News> = news.articles.filter { news -> news.urlToImage != null && news.urlToImage.length > 0 && news.title != "[Removed]" && !news.content.isNullOrEmpty() }

            withContext(Dispatchers.Main) {
                val recyclerView = findViewById<RecyclerView>(R.id.rView)
                recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 1)
                recyclerView.adapter = NewsAdapter(cleanNews)
                adapter = recyclerView


                val recyclerView2 = findViewById<RecyclerView>(R.id.rViewMainNews)
                val layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                recyclerView2.layoutManager = layoutManager
                recyclerView2.adapter = MainNewsAdapter(cleanNews)
            }
        }

        val btnSearch: ImageView = findViewById(R.id.im_search)
        btnSearch.setOnClickListener {
            val intent = Intent(this, NewsSearcher::class.java)
            startActivity(intent)
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
                    ListNews("false",0, emptyList())
                } else {
                    val responseBody = response.body?.string()
                    val gson = Gson()
                    val type = object : TypeToken<ListNews>() {}.type
                    gson.fromJson(responseBody, type)
                }
            } catch (e: Exception) {
                ListNews("false",0, emptyList())
            }
        }
    }

}


class CategoryNewsAdapter(private val category: List<String>, private val mainActivity: MainActivity) :
    RecyclerView.Adapter<CategoryNewsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.catregoryNewsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rview_news_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsItem = category[position]
        holder.title.text = newsItem

        holder.itemView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val news = mainActivity.loadNews("https://newsapi.org/v2/top-headlines?country=us&category=" + newsItem + "&apiKey=a5a72530f7c9425893230093aa01521b")
                val cleanNews: List<News> = news.articles.filter { news -> news.urlToImage != null && news.urlToImage.length > 0 && news.title != "[Removed]" && !news.content.isNullOrEmpty() }
                mainActivity.adapter.adapter = NewsAdapter(cleanNews)
            }
        }
    }

    override fun getItemCount(): Int {
        return category.size
    }
}


class MainNewsAdapter(private val news: List<News>) :
    RecyclerView.Adapter<MainNewsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val picture: ImageView = itemView.findViewById(R.id.mainNewsImage)
        val title: TextView = itemView.findViewById(R.id.mainNewsTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rview_main_news, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsItem = news[position]
        val imageUrl = newsItem.urlToImage

        Glide.with(holder.itemView)
            .load(imageUrl)
            .into(holder.picture)

        holder.title.text = newsItem.title

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, NewsViewer::class.java)
            intent.putExtra("newsImageUrl", imageUrl)
            intent.putExtra("newsTitle", newsItem.title)
            intent.putExtra("newsText", newsItem.content)
            intent.putExtra("newsSource", newsItem.url)
            intent.putExtra("newsPublishDate", newsItem.publishedAt)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return news.size
    }
}



class NewsAdapter(private val news: List<News>) :
    RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

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

data class News (
    val source: Source,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: Date,
    val content: String
)

data class ListNews (
    val status: String,
    val totalResults: Int,
    val articles: List<News>
)

data class Source (
    val id: String,
    val name: String
)



