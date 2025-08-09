package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ItemDestinationGridBinding
import com.zeynekurtulus.wayfare.domain.model.TopRatedPlace

/**
 * AllDestinationsAdapter - RecyclerView adapter for destinations grid
 * 
 * Features:
 * - Grid layout optimized item display
 * - Image loading with Glide
 * - Click handling
 * - Efficient updates with DiffUtil
 */
class AllDestinationsAdapter(
    private val onDestinationClick: (TopRatedPlace) -> Unit
) : RecyclerView.Adapter<AllDestinationsAdapter.DestinationViewHolder>() {

    private var destinations: List<TopRatedPlace> = emptyList()

    fun updateDestinations(newDestinations: List<TopRatedPlace>) {
        val diffCallback = DestinationDiffCallback(destinations, newDestinations)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        destinations = newDestinations
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemDestinationGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount(): Int = destinations.size

    inner class DestinationViewHolder(
        private val binding: ItemDestinationGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(destination: TopRatedPlace) {
            binding.apply {
                // Set destination name
                destinationNameText.text = destination.name
                
                // Set location (use address or create a simplified location string)
                locationText.text = destination.address
                
                // Set description (you may need to add this field to TopRatedPlace model)
                descriptionText.text = "Discover amazing places and experiences"
                
                // Set rating
                ratingText.text = String.format("%.1f", destination.rating)
                
                // Set category (you may need to add this field or derive it)
                categoryChip.text = getCategoryForDestination(destination)
                
                // Load destination image
                loadDestinationImage(destination)
                
                // Set click listener
                root.setOnClickListener {
                    onDestinationClick(destination)
                }
                
                actionButton.setOnClickListener {
                    onDestinationClick(destination)
                }
            }
        }

        private fun loadDestinationImage(destination: TopRatedPlace) {
            val imageUrl = getDestinationImageUrl(destination)
            
            val requestOptions = RequestOptions()
                .transform(RoundedCorners(16))
                .placeholder(R.drawable.ic_map_placeholder)
                .error(R.drawable.ic_map_placeholder)

            Glide.with(binding.root.context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(binding.destinationImageView)
        }

        private fun getDestinationImageUrl(destination: TopRatedPlace): String? {
            return destination.image
        }

        private fun getCategoryForDestination(destination: TopRatedPlace): String {
            // Use wayfareCategory field
            return destination.wayfareCategory.takeIf { it.isNotBlank() } ?: "Other"
        }
    }


    private class DestinationDiffCallback(
        private val oldList: List<TopRatedPlace>,
        private val newList: List<TopRatedPlace>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].placeId == newList[newItemPosition].placeId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            return oldItem.name == newItem.name &&
                   oldItem.address == newItem.address &&
                   oldItem.rating == newItem.rating
        }
    }
}