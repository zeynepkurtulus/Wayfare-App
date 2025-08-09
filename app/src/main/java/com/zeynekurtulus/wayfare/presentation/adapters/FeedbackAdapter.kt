package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ItemFeedbackBinding
import com.zeynekurtulus.wayfare.domain.model.RouteFeedback
import java.text.SimpleDateFormat
import java.util.*

class FeedbackAdapter : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {
    
    private var feedbackList: List<RouteFeedback> = emptyList()
    
    fun updateFeedback(newFeedback: List<RouteFeedback>) {
        feedbackList = newFeedback
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val binding = ItemFeedbackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeedbackViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.bind(feedbackList[position])
    }
    
    override fun getItemCount(): Int = feedbackList.size
    
    inner class FeedbackViewHolder(
        private val binding: ItemFeedbackBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(feedback: RouteFeedback) {
            // Set star rating
            updateStarDisplay(feedback.rating)
            
            // Set comment
            if (feedback.comment.isNullOrBlank()) {
                binding.commentText.visibility = View.GONE
            } else {
                binding.commentText.visibility = View.VISIBLE
                binding.commentText.text = feedback.comment
            }
            
            // Set visit date
            binding.visitDateText.text = formatVisitDate(feedback.visitedOn)
            
            // Set user info (anonymous for now)
            binding.userText.text = "Anonymous Traveler"
        }
        
        private fun updateStarDisplay(rating: Int) {
            val stars = listOf(
                binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
            )
            
            stars.forEachIndexed { index, star ->
                if (index < rating) {
                    star.setImageResource(R.drawable.ic_star_filled)
                } else {
                    star.setImageResource(R.drawable.ic_star_outline)
                }
            }
        }
        
        private fun formatVisitDate(visitedOn: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                val date = inputFormat.parse(visitedOn)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                visitedOn
            }
        }
    }
}