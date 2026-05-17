package com.adel.features.comments.di

import com.adel.features.comments.data.CommentRepository
import com.adel.features.comments.data.CommentRepositoryImpl
import com.adel.features.comments.service.CommentService
import com.adel.features.posts.data.PostRepository

class CommentComponent(
    postRepository: PostRepository,
) {
    val repository: CommentRepository by lazy { CommentRepositoryImpl() }
    val service: CommentService by lazy { CommentService(repository, postRepository) }
}