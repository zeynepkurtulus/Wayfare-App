package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ItemPlaceCardBinding
import com.zeynekurtulus.wayfare.domain.model.Place

class PlaceSearchAdapter(
    private val onPlaceClick: (Place) -> Unit
) : RecyclerView.Adapter<PlaceSearchAdapter.PlaceViewHolder>() {
    
    private var places: List<Place> = emptyList()
    
    fun updatePlaces(newPlaces: List<Place>) {
        places = newPlaces
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaceViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }
    
    override fun getItemCount(): Int = places.size
    
    inner class PlaceViewHolder(
        private val binding: ItemPlaceCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(place: Place) {
            // Set place name
            binding.placeNameText.text = place.name
            
            // Set address
            if (place.address.isNullOrBlank()) {
                binding.addressText.visibility = View.GONE
            } else {
                binding.addressText.visibility = View.VISIBLE
                binding.addressText.text = place.address
            }
            
            // Set place image
            if (place.image.isNullOrBlank()) {
                binding.placeImageView.setImageResource(R.drawable.ic_placeholder)
            } else {
                Glide.with(binding.placeImageView.context)
                    .load(place.image)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .centerCrop()
                    )
                    .into(binding.placeImageView)
            }
            
            // Set rating
            if (place.rating != null && place.rating > 0) {
                binding.ratingLayout.visibility = View.VISIBLE
                binding.ratingText.text = String.format("%.1f", place.rating)
            } else {
                binding.ratingLayout.visibility = View.GONE
            }
            
            // Set category
            if (place.category.isNullOrBlank()) {
                binding.categoryChip.visibility = View.GONE
            } else {
                binding.categoryChip.visibility = View.VISIBLE
                binding.categoryChip.text = place.category
            }
            
            // Set duration
            if (place.duration != null && place.duration > 0) {
                binding.durationText.visibility = View.VISIBLE
                val hours = place.duration / 60
                val minutes = place.duration % 60
                binding.durationText.text = if (hours > 0) {
                    if (minutes > 0) {
                        "Recommended visit: ${hours}h ${minutes}m"
                    } else {
                        "Recommended visit: ${hours}h"
                    }
                } else {
                    "Recommended visit: ${minutes}m"
                }
            } else {
                binding.durationText.visibility = View.GONE
            }
            
            // Set click listener
            binding.root.setOnClickListener {
                onPlaceClick(place)
            }
        }
    }
}