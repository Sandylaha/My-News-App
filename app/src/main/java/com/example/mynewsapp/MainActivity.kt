package com.example.mynewsapp

import android.app.SearchManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mynewsapp.api.RetrofitClient
import com.example.mynewsapp.api.RetrofitInterface
import com.example.mynewsapp.databinding.ActivityMainBinding
import com.example.mynewsapp.databinding.ErrorBinding
import com.example.mynewsapp.model.Article
import com.example.mynewsapp.model.News
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding1: ActivityMainBinding
    private lateinit var binding2: ErrorBinding
    val API_KEY = "7bc44e829be14a1dbf068ac684791c57"
    private var adapter: Adapter? = null
    private var articles: ArrayList<Article> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding1 = ActivityMainBinding.inflate(layoutInflater)
        val view1:View = binding1.root
        setContentView(view1)

        binding2 = ErrorBinding.inflate(layoutInflater)
        binding1.topheadelines
        val recyclerView = binding1.recyclerView
        val swipeRefreshLayout = binding1.swipeRefreshLayout

        swipeRefreshLayout.setOnRefreshListener{
            onRefresh()
            swipeRefreshLayout.isRefreshing = false}
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false

        onLoadingSwipeRefresh("")


    }


    override fun onRefresh() {
            LoadJson("")
        }


    private fun onLoadingSwipeRefresh( keyword: String) {

        binding1.swipeRefreshLayout.post(
            Runnable {
                    LoadJson(keyword)
                }
        )
    }

    private fun LoadJson(keyword: String) {
        binding2.errorLayout.setVisibility(View.GONE)
        binding1.swipeRefreshLayout.setRefreshing(true)
        val retrofitClient = RetrofitClient()
        val apiInterface: RetrofitInterface =
            retrofitClient.getInstance()!!.create(RetrofitInterface::class.java)
        val country: String = Utils.country
        val language: String = Utils.language
        val call: Call<News> = if (keyword.isNotEmpty()) {
            apiInterface.getNewsSearch(keyword, language, "publishedAt", API_KEY)
        } else {
            Log.d("Sandip", country + " " + API_KEY)
            apiInterface.getNews(country, API_KEY)
        }
        call.enqueue(object : Callback<News> {
            override fun onResponse(call: Call<News>, response: Response<News>) {
                if (response.isSuccessful() && response.body()?.articles != null) {
                    if (!articles.isEmpty()) {
                        articles.clear()
                    }
                    articles = response.body()?.articles as ArrayList<Article>
                    adapter = Adapter(articles, this@MainActivity)
                    binding1.recyclerView.setAdapter(adapter)
                    adapter!!.notifyDataSetChanged()
                    initListener()
                    binding1.topheadelines.setVisibility(View.VISIBLE)
                    binding1.swipeRefreshLayout.setRefreshing(false)
                } else {
                    binding1.topheadelines.setVisibility(View.INVISIBLE)
                    binding1.swipeRefreshLayout.setRefreshing(false)
                    val errorCode: String
                    errorCode = when (response.code()) {
                        404 -> "404 not found"
                        500 -> "500 server broken"
                        else -> "unknown error"
                    }
                    showErrorMessage(
                        R.drawable.no_result,
                        "No Result",
                        """
                        Please Try Again!
                        $errorCode
                        """.trimIndent()
                    )
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                binding1.topheadelines.visibility = View.INVISIBLE
                binding1.swipeRefreshLayout.isRefreshing = false
                showErrorMessage(
                    R.drawable.no_result,
                    "Oops..",
                    """
                    Network failure, Please Try Again
                    $t
                    """.trimIndent()
                )
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView: SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setQueryHint("Search Latest News...")
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.length > 2) {
                    onLoadingSwipeRefresh(query)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Type more than two letters!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        searchMenuItem.icon.setVisible(false, false)
        return true
    }


    private fun initListener() {


        val obj = object : Adapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val imageView = view?.findViewById<ImageView>(R.id.img)
                val intent = Intent(applicationContext, DetailedNews::class.java)

                val article: Article = articles.get(position)
                intent.putExtra("url", article.url)
                intent.putExtra("title", article.title)
                intent.putExtra("img", article.urlToImage)
                intent.putExtra("date", article.publishedAt)
                intent.putExtra("source", article.source?.name)
                intent.putExtra("author", article.author)

                val pair: Pair<View, String> =
                    Pair.create(imageView as View, ViewCompat.getTransitionName(imageView))
                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@MainActivity,
                    pair
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, optionsCompat.toBundle())
                } else {
                    startActivity(intent)
                }
            }
        }
        adapter!!.setOnItemClickListener(obj)
    }

    private fun showErrorMessage(imageView: Int, title: String, message: String) {
        if (binding2.errorLayout.getVisibility() == View.GONE) {
            binding2.errorLayout.setVisibility(View.VISIBLE)
        }
        binding2.errorImage.setImageResource(imageView)
        binding2.errorTitle.setText(title)
        binding2.errorMessage.setText(message)
        binding2.btnRetry.setOnClickListener(View.OnClickListener { onLoadingSwipeRefresh("") })
    }

}


