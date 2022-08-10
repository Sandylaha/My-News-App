package com.example.mynewsapp.api

import com.example.mynewsapp.model.News
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {

    @GET("top-headlines")
     fun getNews(
        @Query("country") country: String?,
        @Query("apiKey") apiKey: String?
    ): Call<News>

    @GET("everything")
     fun getNewsSearch(
        @Query("q") keyword: String?,
        @Query("language") language: String?,
        @Query("sortBy") sortBy: String?,
        @Query("apiKey") apiKey: String?
    ): Call<News>
}