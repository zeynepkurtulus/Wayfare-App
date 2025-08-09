package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeynekurtulus.wayfare.databinding.ItemMustVisitSearchBinding
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlaceSearch

/**
 * MustVisitPlacesAdapter - For TripMaker search functionality
 * This adapter works with MustVisitPlaceSearch for searching and selecting places
 */
class MustVisitPlacesAdapter(
    private val onPlaceClick: (MustVisitPlaceSearch) -> Unit
) : RecyclerView.Adapter<MustVisitPlacesAdapter.PlaceViewHolder>() {

    private var places: List<MustVisitPlaceSearch> = emptyList()

    fun updatePlaces(newPlaces: List<MustVisitPlaceSearch>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemMustVisitSearchBinding.inflate(
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
        private val binding: ItemMustVisitSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(place: MustVisitPlaceSearch) {
            binding.apply {
                placeName.text = place.name
                placeCategory.text = place.wayfareCategory
                
                root.setOnClickListener {
                    onPlaceClick(place)
                }
            }
        }
    }
}