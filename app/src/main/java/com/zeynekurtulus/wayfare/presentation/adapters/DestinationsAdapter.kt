package com.zeynekurtulus.wayfare.presentation.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zeynekurtulus.wayfare.databinding.ItemDestinationCardBinding
import com.zeynekurtulus.wayfare.presentation.activities.Destination

/**
 * RecyclerView adapter for displaying destination cards in horizontal list
 */
class DestinationsAdapter(
    private var destinations: List<Destination>,
    private val onDestinationClick: (Destination) -> Unit
) : RecyclerView.Adapter<DestinationsAdapter.DestinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemDestinationCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        
        // Keep the original XML layout dimensions (200dp x 240dp)
        // Don't override the height - let XML control the card size
        
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount(): Int = destinations.size
    
    /**
     * Update the destinations list and notify adapter of changes
     */
    fun updateDestinations(newDestinations: List<Destination>) {
        destinations = newDestinations
        notifyDataSetChanged()
        android.util.Log.d("DestinationsAdapter", "📋 Updated destinations: ${destinations.size} items")
    }

    inner class DestinationViewHolder(
        private val binding: ItemDestinationCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDestinationClick(destinations[position])
                }
            }
        }

        fun bind(destination: Destination) {
            android.util.Log.d("DestinationsAdapeter", "🎨 BINDING Destination: '${destination.name}' with rating: ${destination.rating}")
            android.util.Log.d("DestinationsAdapter", "🖼️ Image URL: '${destination.imageUrl}'")
            
            binding.apply {
                destinationNameTextView.text = destination.name
                ratingTextView.text = destination.rating.toString()
                
                android.util.Log.d("DestinationsAdapter", "✅ Set name: '${destinationNameTextView.text}', rating: '${ratingTextView.text}'")
                
                // Load destination image using Glide directly (same as TripsAdapter)
                if (destination.imageUrl.isNotEmpty()) {
                    android.util.Log.d("DestinationsAdapter", "🖼️ Loading image: ${destination.imageUrl}")
                    
                    // Check if this is a TripAdvisor URL that might have DNS issues
                    if (destination.imageUrl.contains("tripadvisor.com")) {
                        android.util.Log.w("DestinationsAdapter", "⚠️ TripAdvisor domain detected - testing DNS resolution")
                    }
                    
                    Glide.with(binding.root.context)
                        .load(destination.imageUrl)
                        .placeholder(com.zeynekurtulus.wayfare.R.drawable.ic_place_placeholder)
                        .error(com.zeynekurtulus.wayfare.R.drawable.ic_error_image)
                        .centerCrop()
                        .timeout(15000) // 15 second timeout
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                android.util.Log.e("DestinationsAdapter", "❌ Image load FAILED for: ${destination.name}")
                                android.util.Log.e("DestinationsAdapter", "❌ Failed URL: ${destination.imageUrl}")
                                e?.let { 
                                    android.util.Log.e("DestinationsAdapter", "❌ Error details: ${it.message}")
                                    // Log root causes for DNS issues
                                    it.rootCauses.forEach { cause ->
                                        android.util.Log.e("DestinationsAdapter", "❌ Root cause: ${cause.javaClass.simpleName}: ${cause.message}")
                                    }
                                }
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                android.util.Log.d("DestinationsAdapter", "✅ Image load SUCCESS for: ${destination.name}")
                                return false
                            }
                        })
                        .into(destinationImageView)
                } else {
                    android.util.Log.d("DestinationsAdapter", "⚠️ No image URL, showing placeholder")
                    destinationImageView.setImageResource(com.zeynekurtulus.wayfare.R.drawable.ic_place_placeholder)
                }
            }
        }
    }
}