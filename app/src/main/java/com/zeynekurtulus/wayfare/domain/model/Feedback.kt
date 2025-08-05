package com.zeynekurtulus.wayfare.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaceFeedback(
    val feedbackId: String,
    val userId: String,
    val placeId: String,
    val rating: Int,
    val comment: String?,
    val visitedOn: String,
    val createdAt: String,
    val updatedAt: String
) : Parcelable

@Parcelize
data class RouteFeedback(
    val feedbackId: String,
    val userId: String,
    val routeId: String,
    val rating: Int,
    val comment: String?,
    val visitedOn: String,
    val createdAt: String,
    val updatedAt: String
) : Parcelable

@Parcelize
data class FeedbackStats(
    val totalFeedback: Int,
    val averageRating: Double,
    val ratingDistribution: Map<String, Int>
) : Parcelable

data class CreatePlaceFeedback(
    val placeId: String,
    val rating: Int,
    val comment: String?,
    val visitedOn: String
)

data class CreateRouteFeedback(
    val routeId: String,
    val rating: Int,
    val comment: String?,
    val visitedOn: String
)

data class UpdatePlaceFeedback(
    val rating: Int?,
    val comment: String?,
    val visitedOn: String?
)

data class UpdateRouteFeedback(
    val rating: Int?,
    val comment: String?,
    val visitedOn: String?
)