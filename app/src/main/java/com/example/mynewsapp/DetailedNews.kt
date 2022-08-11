package com.example.mynewsapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.mynewsapp.databinding.ActivityDetailedNewsBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlin.math.abs

class DetailedNews : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var binding: ActivityDetailedNewsBinding

    private var isHideToolbarView = false

    private var mUrl: String? = null
    private var mTitle: String? = null
    private var mSource: String? = null
     override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityDetailedNewsBinding.inflate(layoutInflater)
        val view:View = binding.root
        setContentView(view)
         supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)





        val collapsingToolbarLayout: CollapsingToolbarLayout = binding.collapsingToolbar
        collapsingToolbarLayout.title = ""
        binding.appbar.addOnOffsetChangedListener(this)
        val intent: Intent = intent
        val mUrl = intent.getStringExtra("url")
        val mImg = intent.getStringExtra("img")
        val mTitle = intent.getStringExtra("title")
        val  mDate = intent.getStringExtra("date")
        val mSource = intent.getStringExtra("source")
        val mAuthor = intent.getStringExtra("author")
        val requestOptions = RequestOptions()
        requestOptions.error(Utils.randomDrawbleColor)
        Glide.with(this)
            .load(mImg)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.backdrop)
        binding.titleOnAppbar.text = mSource
        binding.subtitleOnAppbar.text =mUrl
//        date.setText(Utils.DateFormat(mDate))
        title = mTitle
         val author: String = if (mAuthor != null) {
             " \u2022 $mAuthor"
         } else {
             ""
         }
        binding.time.text = mSource + author + " \u2022 " + Utils.DateToTimeFormat(mDate)
        initWebView(mUrl)
    }

    private fun initWebView(url: String?) {
        val webView: WebView = findViewById(R.id.webView)
        webView.settings.loadsImagesAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url!!)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val maxScroll: Int = appBarLayout.totalScrollRange
        val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()
        if (percentage == 1f && isHideToolbarView) {
//           binding.date_behavior.setVisibility(View.GONE)
            binding.titleAppbar.visibility = View.VISIBLE
            isHideToolbarView = !isHideToolbarView
        } else if (percentage < 1f && !isHideToolbarView) {
//            date_behavior.setVisibility(View.VISIBLE)
            binding.titleAppbar.visibility = View.GONE
            isHideToolbarView = !isHideToolbarView
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_news, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.webView) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(mUrl)
            startActivity(i)
            return true
        } else if (id == R.id.share) {
            try {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plan"
                i.putExtra(Intent.EXTRA_SUBJECT, mSource)
                val body = "$mTitle\n$mUrl\nShare from the News App\n"
                i.putExtra(Intent.EXTRA_TEXT, body)
                startActivity(Intent.createChooser(i, "Share with :"))
            } catch (e: Exception) {
                Toast.makeText(this, "Hmm.. Sorry, \nCannot be share", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}