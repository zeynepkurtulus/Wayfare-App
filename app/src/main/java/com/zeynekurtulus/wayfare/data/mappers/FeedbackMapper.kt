package com.zeynekurtulus.wayfare.data.mappers

import com.zeynekurtulus.wayfare.data.api.dto.feedback.*
import com.zeynekurtulus.wayfare.domain.model.*

object FeedbackMapper {
    
    fun mapToPlaceFeedbackRequest(createPlaceFeedback: CreatePlaceFeedback): PlaceFeedbackRequest {
        return PlaceFeedbackRequest(
            placeId = createPlaceFeedback.placeId,
            rating = createPlaceFeedback.rating,
            comment = createPlaceFeedback.comment,
            visitedOn = createPlaceFeedback.visitedOn
        )
    }
    
    fun mapToRouteFeedbackRequest(createRouteFeedback: CreateRouteFeedback): RouteFeedbackRequest {
        return RouteFeedbackRequest(
            routeId = createRouteFeedback.routeId,
            rating = createRouteFeedback.rating,
            comment = createRouteFeedback.comment,
            visitedOn = createRouteFeedback.visitedOn
        )
    }
    
    fun mapToUpdatePlaceFeedbackRequest(updatePlaceFeedback: UpdatePlaceFeedback): UpdatePlaceFeedbackRequest {
        return UpdatePlaceFeedbackRequest(
            rating = updatePlaceFeedback.rating,
            comment = updatePlaceFeedback.comment,
            visitedOn = updatePlaceFeedback.visitedOn
        )
    }
    
    fun mapToUpdateRouteFeedbackRequest(updateRouteFeedback: UpdateRouteFeedback): UpdateRouteFeedbackRequest {
        return UpdateRouteFeedbackRequest(
            rating = updateRouteFeedback.rating,
            comment = updateRouteFeedback.comment,
            visitedOn = updateRouteFeedback.visitedOn
        )
    }
    
    fun mapToPlaceFeedback(placeFeedbackDto: PlaceFeedbackDto): PlaceFeedback {
        return PlaceFeedback(
            feedbackId = placeFeedbackDto.feedbackId,
            userId = placeFeedbackDto.userId,
            username = placeFeedbackDto.username,
            placeId = placeFeedbackDto.placeId,
            rating = placeFeedbackDto.rating,
            comment = placeFeedbackDto.comment,
            visitedOn = placeFeedbackDto.visitedOn,
            createdAt = placeFeedbackDto.createdAt,
            updatedAt = placeFeedbackDto.updatedAt
        )
    }
    
    fun mapToRouteFeedback(routeFeedbackDto: RouteFeedbackDto): RouteFeedback {
        return RouteFeedback(
            feedbackId = routeFeedbackDto.feedbackId,
            userId = routeFeedbackDto.userId,
            username = routeFeedbackDto.username,
            routeId = routeFeedbackDto.routeId,
            rating = routeFeedbackDto.rating,
            comment = routeFeedbackDto.comment,
            visitedOn = routeFeedbackDto.visitedOn,
            createdAt = routeFeedbackDto.createdAt,
            updatedAt = routeFeedbackDto.updatedAt
        )
    }
    
    fun mapToFeedbackStats(feedbackStatsDto: FeedbackStatsDto): FeedbackStats {
        return FeedbackStats(
            totalFeedback = feedbackStatsDto.totalFeedback,
            averageRating = feedbackStatsDto.averageRating,
            ratingDistribution = feedbackStatsDto.ratingDistribution
        )
    }
}