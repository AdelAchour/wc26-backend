package com.adel.features.comments.di

import com.adel.features.comments.data.CommentLikeRepository
import com.adel.features.comments.data.CommentLikeRepositoryImpl
import com.adel.features.comments.data.CommentRepository
import com.adel.features.comments.data.CommentRepositoryImpl
import com.adel.features.comments.service.CommentLikeService
import com.adel.features.comments.service.CommentService
import com.adel.features.posts.data.PostRepository

class CommentComponent(
    postRepository: PostRepository,
) {
    val repository: CommentRepository by lazy { CommentRepositoryImpl() }
    val likeRepository: CommentLikeRepository by lazy { CommentLikeRepositoryImpl() }

    val service: CommentService by lazy { CommentService(repository, postRepository) }
    val likeService: CommentLikeService by lazy { CommentLikeService(likeRepository, repository) }
}