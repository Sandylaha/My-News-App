package com.example.mynewsapp.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

 class News{
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("totalResult")
    @Expose
    var totalResult = 0

    @SerializedName("articles")
    @Expose
    var articles: List<Article>? = null
}
