package com.zeynekurtulus.wayfare.presentation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlaceSearch

/**
 * Adapter for displaying must-visit places search results
 */
class MustVisitPlacesAdapter(
    private val onPlaceClick: (MustVisitPlaceSearch) -> Unit
) : RecyclerView.Adapter<MustVisitPlacesAdapter.PlaceViewHolder>() {

    private var places = mutableListOf<MustVisitPlaceSearch>()

    fun updatePlaces(newPlaces: List<MustVisitPlaceSearch>) {
        Log.d("MustVisitPlacesAdapter", "Updating places: ${newPlaces.size} items")
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
        Log.d("MustVisitPlacesAdapter", "notifyDataSetChanged called, itemCount: $itemCount")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_must_visit_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        Log.d("MustVisitPlacesAdapter", "Binding place at position $position: ${place.name}")
        holder.bind(place)
    }

    override fun getItemCount(): Int = places.size

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeImageView: de.hdodenhof.circleimageview.CircleImageView = itemView.findViewById(R.id.mustVisitPlaceImageView)
        private val placeNameTextView: TextView = itemView.findViewById(R.id.mustVisitPlaceNameText)
        private val placeCategoryTextView: TextView = itemView.findViewById(R.id.mustVisitPlaceCategoryText)
        private val placeRatingTextView: TextView = itemView.findViewById(R.id.mustVisitPlaceRatingText)
        private val placeAddressTextView: TextView = itemView.findViewById(R.id.mustVisitPlaceAddressText)

        fun bind(place: MustVisitPlaceSearch) {
            placeNameTextView.text = place.name
            placeCategoryTextView.text = place.wayfareCategory
            placeRatingTextView.text = place.rating?.toString() ?: "N/A"
            placeAddressTextView.text = place.address ?: "Address not available"

            // Update selection appearance by changing card background or stroke
            val card = itemView as MaterialCardView
            if (place.isSelected) {
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
                card.strokeWidth = 3
            } else {
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.border_light)
                card.strokeWidth = 1
            }

            // Load place image if available
            Log.d("MustVisitPlacesAdapter", "Place: ${place.name}, Image URL: '${place.image}'")
            
            if (!place.image.isNullOrEmpty()) {
                Log.d("MustVisitPlacesAdapter", "Loading image for place: ${place.name} from ${place.image}")
                Glide.with(itemView.context)
                    .load(place.image)
                    .placeholder(R.drawable.ic_place_placeholder)
                    .error(R.drawable.ic_place_placeholder)
                    .timeout(15000)
                    .centerCrop()
                    .into(placeImageView)
            } else {
                Log.d("MustVisitPlacesAdapter", "No image URL for place: ${place.name}, using placeholder")
                placeImageView.setImageResource(R.drawable.ic_place_placeholder)
            }

            // Handle click
            itemView.setOnClickListener {
                onPlaceClick(place)
            }
        }
    }
}