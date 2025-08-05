package com.zeynekurtulus.wayfare.presentation.adapters

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ItemTripCardBinding
import com.zeynekurtulus.wayfare.presentation.activities.Trip

class TripsAdapter(
    private var trips: List<Trip>,
    private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int = trips.size

    fun updateTrips(newTrips: List<Trip>) {
        trips = newTrips
        notifyDataSetChanged()
    }

    inner class TripViewHolder(
        private val binding: ItemTripCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTripClick(trips[position])
                }
            }
        }

        fun bind(trip: Trip) {
            binding.tripNameTextView.text = trip.name

            if (trip.imageUrl.isNotEmpty()) {
                Log.d("TripsAdapter", "Görsel yükleniyor: ${trip.imageUrl}")
                Glide.with(binding.root.context)
                    .load(trip.imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("TripsAdapter", "Görsel yüklenemedi: ${trip.imageUrl}", e)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("TripsAdapter", "Görsel başarıyla yüklendi: ${trip.imageUrl}")
                            return false
                        }
                    })
                    .into(binding.tripImageView)
            } else {
                Log.w("TripsAdapter", "Görsel URL boş, placeholder gösteriliyor.")
                binding.tripImageView.setImageResource(R.drawable.ic_placeholder_image)
            }
        }
    }
}