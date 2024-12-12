package com.example.nnews

import PreferencesManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip


class NewsViewer : AppCompatActivity() {
    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.news_viewer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nw_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val preferencesManager = PreferencesManager(this)
        val retrievedList: MutableList<SaveNews?>? = preferencesManager.classList
        val checkNews = retrievedList?.find { x -> x?.title == intent.getStringExtra("newsTitle").toString() }

        if (checkNews != null) {
            val btnLove: ImageButton = findViewById(R.id.btn_love)
            btnLove.setImageResource(R.drawable.heart_red)
        }

        val newsImageView: ImageView = findViewById(R.id.newsImage)
        val newsNameView: TextView = findViewById(R.id.newsName)
        val newsTextView: TextView = findViewById(R.id.newsText)

        newsNameView.text = intent.getStringExtra("newsTitle")
        newsTextView.text = intent.getStringExtra("newsText")

        Glide.with(this)
        .load(intent.getStringExtra("newsImageUrl"))
        .into(newsImageView)

        val btnShowPic: Chip = findViewById(R.id.buttonGoToSource)
        btnShowPic.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("newsSource")))
            startActivity(browserIntent)
        }

        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val btnLove: ImageButton = findViewById(R.id.btn_love)
        btnLove.setOnClickListener {
            if (checkNews == null) {
                btnLove.setImageResource(R.drawable.heart_red)

                val sNews: SaveNews = SaveNews(
                    newsNameView.text.toString(),
                    newsTextView.text.toString(),
                    intent.getStringExtra("newsImageUrl").toString(),
                    intent.getStringExtra("newsSource").toString())

                if (retrievedList == null){
                    val newList: MutableList<SaveNews?> = mutableListOf(sNews)
                    newList.add(sNews)
                    preferencesManager.saveClassList(newList);
                }
                else
                {
                    retrievedList!!.add(sNews)
                    preferencesManager.saveClassList(retrievedList);
                }

            }
            else
            {
                btnLove.setImageResource(R.drawable.heart_white)
                retrievedList?.remove(checkNews)
                preferencesManager.saveClassList(retrievedList);
            }

        }
    }

}

data class SaveNews (
    val title: String,
    val text: String,
    val imageUrl: String,
    val url: String
)