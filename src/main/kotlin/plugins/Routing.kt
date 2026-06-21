package com.adel.plugins

import com.adel.common.data.BuildInfo
import com.adel.common.data.HealthResponse
import com.adel.config.loadJwtConfig
import com.adel.features.auth.api.authRoutes
import com.adel.features.auth.di.AuthComponent
import com.adel.features.comments.api.commentRoutes
import com.adel.features.comments.di.CommentComponent
import com.adel.features.likes.api.likeRoutes
import com.adel.features.likes.di.LikeComponent
import com.adel.features.matches.api.adminMatchRoutes
import com.adel.features.matches.api.matchRoutes
import com.adel.features.matches.di.MatchComponent
import com.adel.features.notifications.api.notificationRoutes
import com.adel.features.notifications.api.pushTokenRoutes
import com.adel.features.notifications.di.NotificationComponent
import com.adel.features.posts.api.postRoutes
import com.adel.features.posts.di.PostComponent
import com.adel.features.predictions.api.predictionRoutes
import com.adel.features.predictions.di.PredictionComponent
import com.adel.features.users.api.userRoutes
import com.adel.features.users.di.UserComponent
import com.adel.features.system.api.systemStatusRoutes
import com.adel.features.system.api.SystemStatusInterceptor
import com.adel.features.system.di.SystemComponent
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun Application.configureRouting() {
    val jwtConfig = loadJwtConfig()

    val matchComponent = MatchComponent()
    val notificationComponent = NotificationComponent()
    val userComponent = UserComponent()
    val postComponent = PostComponent(matchComponent.repository)
    val likeComponent = LikeComponent(postComponent.repository, notificationComponent.service)
    val commentComponent = CommentComponent(postComponent.repository, notificationComponent.service)
    val authComponent = AuthComponent(userComponent.repository, jwtConfig)
    val systemComponent = SystemComponent()
    val predictionComponent = PredictionComponent(matchComponent.repository)

    // Install system config request interceptor middleware
    install(SystemStatusInterceptor) {
        service = systemComponent.service
    }

    // Install auth using components wired above
    configureAuthentication(jwtConfig, authComponent.jwtService)

    routing {
        get("/") {
            call.respond(
                HealthResponse(
                    service = "wc26-backend",
                    status = "ok",
                    version = BuildInfo.version,
                    timestamp = OffsetDateTime.now(ZoneOffset.UTC).toString(),
                )
            )
        }

        matchRoutes(matchComponent.service)
        adminMatchRoutes(matchComponent.service)
        userRoutes(userComponent.service)
        postRoutes(postComponent.service, likeComponent.service, matchComponent.service)
        likeRoutes(likeComponent.service, matchComponent.service)
        commentRoutes(commentComponent.service, commentComponent.likeService)
        authRoutes(authComponent.service, userComponent.service)
        systemStatusRoutes(systemComponent.service)
        notificationRoutes(notificationComponent.service)
        pushTokenRoutes(notificationComponent.pushTokenRepository)
        predictionRoutes(predictionComponent.service)
    }
}