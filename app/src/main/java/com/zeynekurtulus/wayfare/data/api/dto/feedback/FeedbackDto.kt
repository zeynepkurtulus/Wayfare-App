package com.zeynekurtulus.wayfare.data.api.dto.feedback

import com.google.gson.annotations.SerializedName

// Request DTOs
data class PlaceFeedbackRequest(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("visited_on") val visitedOn: String
)

data class RouteFeedbackRequest(
    @SerializedName("route_id") val routeId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("visited_on") val visitedOn: String
)

data class UpdatePlaceFeedbackRequest(
    @SerializedName("rating") val rating: Int?,
    @SerializedName("comment") val comment: String?,
    @SerializedName("visited_on") val visitedOn: String?
)

data class UpdateRouteFeedbackRequest(
    @SerializedName("rating") val rating: Int?,
    @SerializedName("comment") val comment: String?,
    @SerializedName("visited_on") val visitedOn: String?
)

// Response DTOs
data class FeedbackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("feedback_id") val feedbackId: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class PlaceFeedbackListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<PlaceFeedbackDto>
)

data class RouteFeedbackListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<RouteFeedbackDto>
)

data class FeedbackStatsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: FeedbackStatsDto
)

data class UpdateFeedbackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("updated_at") val updatedAt: String
)

data class DeleteFeedbackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("deleted_at") val deletedAt: String
)

data class PlaceFeedbackDto(
    @SerializedName("feedback_id") val feedbackId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("visited_on") val visitedOn: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class RouteFeedbackDto(
    @SerializedName("feedback_id") val feedbackId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("route_id") val routeId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("visited_on") val visitedOn: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class FeedbackStatsDto(
    @SerializedName("total_feedback") val totalFeedback: Int,
    @SerializedName("average_rating") val averageRating: Double,
    @SerializedName("rating_distribution") val ratingDistribution: Map<String, Int>
)


 
 
 