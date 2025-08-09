package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ItemMustVisitPlaceBinding
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlace

/**
 * TripDetailsMustVisitAdapter - RecyclerView adapter for must-visit places in trip details
 * 
 * Features:
 * - Horizontal scrolling list
 * - Image loading with Glide
 * - Click handling
 * - Efficient updates with DiffUtil
 */
class TripDetailsMustVisitAdapter(
    private val onPlaceClick: (MustVisitPlace) -> Unit
) : RecyclerView.Adapter<TripDetailsMustVisitAdapter.MustVisitPlaceViewHolder>() {

    private var places: List<MustVisitPlace> = emptyList()

    fun updatePlaces(newPlaces: List<MustVisitPlace>) {
        val diffCallback = MustVisitPlaceDiffCallback(places, newPlaces)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        places = newPlaces
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MustVisitPlaceViewHolder {
        val binding = ItemMustVisitPlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MustVisitPlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MustVisitPlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size

    inner class MustVisitPlaceViewHolder(
        private val binding: ItemMustVisitPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(place: MustVisitPlace) {
            binding.apply {
                // Set place name
                placeNameText.text = place.placeName
                
                // Set place category (derive from source or use default)
                placeCategoryText.text = place.source.takeIf { it.isNotBlank() } ?: "Attraction"
                
                // Load place image
                loadPlaceImage(place)
                
                // Set click listener
                root.setOnClickListener {
                    onPlaceClick(place)
                }
            }
        }

        private fun loadPlaceImage(place: MustVisitPlace) {
            val requestOptions = RequestOptions()
                .transform(RoundedCorners(12))
                .placeholder(R.drawable.ic_map_placeholder)
                .error(R.drawable.ic_map_placeholder)

            Glide.with(binding.root.context)
                .load(place.image)
                .apply(requestOptions)
                .into(binding.placeImageView)
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private class MustVisitPlaceDiffCallback(
        private val oldList: List<MustVisitPlace>,
        private val newList: List<MustVisitPlace>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].placeId == newList[newItemPosition].placeId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            return oldItem.placeName == newItem.placeName &&
                   oldItem.source == newItem.source &&
                   oldItem.address == newItem.address
        }
    }
}