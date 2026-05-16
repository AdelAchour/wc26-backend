package com.adel.features.posts.di

import com.adel.features.posts.data.PostRepository
import com.adel.features.posts.data.PostRepositoryImpl
import com.adel.features.posts.service.PostService

class PostComponent {
    val repository: PostRepository by lazy { PostRepositoryImpl() }
    val service: PostService by lazy { PostService(repository) }
}