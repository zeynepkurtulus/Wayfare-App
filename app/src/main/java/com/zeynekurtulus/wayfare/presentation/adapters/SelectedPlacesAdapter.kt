package com.zeynekurtulus.wayfare.presentation.adapters

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlaceSearch

/**
 * Adapter for displaying selected must-visit places
 */
class SelectedPlacesAdapter(
    private val onRemoveClick: (MustVisitPlaceSearch) -> Unit
) : RecyclerView.Adapter<SelectedPlacesAdapter.SelectedPlaceViewHolder>() {

    private var selectedPlaces = mutableListOf<MustVisitPlaceSearch>()

    fun updateSelectedPlaces(newPlaces: List<MustVisitPlaceSearch>) {
        Log.d("SelectedPlacesAdapter", "Updating selected places: ${newPlaces.size} items")
        selectedPlaces.clear()
        selectedPlaces.addAll(newPlaces)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedPlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_place, parent, false)
        return SelectedPlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedPlaceViewHolder, position: Int) {
        val place = selectedPlaces[position]
        Log.d("SelectedPlacesAdapter", "Binding selected place at position $position: ${place.name}")
        holder.bind(place)
    }

    override fun getItemCount(): Int = selectedPlaces.size

    inner class SelectedPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val selectedPlaceImageView: ImageView = itemView.findViewById(R.id.selectedPlaceImageView)
        private val selectedPlaceNameTextView: TextView = itemView.findViewById(R.id.selectedPlaceNameTextView)
        private val selectedPlaceCategoryTextView: TextView = itemView.findViewById(R.id.selectedPlaceCategoryTextView)
        private val removeButton: ImageView = itemView.findViewById(R.id.removeButton)

        fun bind(place: MustVisitPlaceSearch) {
            selectedPlaceNameTextView.text = place.name
            selectedPlaceCategoryTextView.text = place.wayfareCategory
            
            Log.d("SelectedPlacesAdapter", "Binding selected place: ${place.name}")
            Log.d("SelectedPlacesAdapter", "Selected place image URL: '${place.image}'")
            Log.d("SelectedPlacesAdapter", "Selected place category: ${place.wayfareCategory}")

            // Load place image using the EXACT same method as working DestinationsAdapter
            if (!place.image.isNullOrEmpty()) {
                Log.d("SelectedPlacesAdapter", "🖼️ Loading image for selected place: ${place.name}")
                Log.d("SelectedPlacesAdapter", "🖼️ Image URL: ${place.image}")
                
                // Check if this is a TripAdvisor URL that might have DNS issues (same as DestinationsAdapter)
                if (place.image.contains("tripadvisor.com")) {
                    Log.w("SelectedPlacesAdapter", "⚠️ TripAdvisor domain detected - testing DNS resolution")
                }
                
                Glide.with(itemView.context)
                    .load(place.image) // Use original HTTPS URL, don't convert to HTTP
                    .placeholder(R.drawable.ic_place_placeholder)
                    .error(R.drawable.ic_place_placeholder)
                    .centerCrop()
                    .timeout(15000) // Same timeout as DestinationsAdapter
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("SelectedPlacesAdapter", "❌ Image load FAILED for selected place: ${place.name}")
                            Log.e("SelectedPlacesAdapter", "❌ Failed URL: ${place.image}")
                            e?.let { 
                                Log.e("SelectedPlacesAdapter", "❌ Error details: ${it.message}")
                                // Log root causes for SSL/DNS issues
                                it.rootCauses.forEach { cause ->
                                    Log.e("SelectedPlacesAdapter", "❌ Root cause: ${cause.javaClass.simpleName}: ${cause.message}")
                                }
                            }
                            
                            // For now, just show placeholder on failure - we'll handle SSL issues in network config
                            Log.w("SelectedPlacesAdapter", "🔄 Using placeholder due to load failure")
                            
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("SelectedPlacesAdapter", "✅ Image loaded successfully for selected place: ${place.name}")
                            return false
                        }
                    })
                    .into(selectedPlaceImageView)
            } else {
                Log.d("SelectedPlacesAdapter", "No image URL for selected place: ${place.name}, using placeholder")
                selectedPlaceImageView.setImageResource(R.drawable.ic_place_placeholder)
            }

            // Handle remove click
            removeButton.setOnClickListener {
                onRemoveClick(place)
            }
        }
    }
}