package com.zeynekurtulus.wayfare.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R

object ImageLoader {
    
    /**
     * Load image from URL into ImageView with default options
     */
    fun loadImage(
        context: Context,
        imageUrl: String?,
        imageView: ImageView,
        placeholder: Int = R.drawable.ic_placeholder_image,
        error: Int = R.drawable.ic_error_image
    ) {
        Glide.with(context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(placeholder)
                    .error(error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
            )
            .into(imageView)
    }
    
    /**
     * Load circular image (for profile pictures)
     */
    fun loadCircularImage(
        context: Context,
        imageUrl: String?,
        imageView: ImageView,
        placeholder: Int = R.drawable.ic_profile_placeholder
    ) {
        Glide.with(context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(placeholder)
                    .error(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
            )
            .into(imageView)
    }
    
    /**
     * Load place image with specific size optimization
     */
    fun loadPlaceImage(
        context: Context,
        imageUrl: String?,
        imageView: ImageView,
        width: Int = 400,
        height: Int = 300
    ) {
        Glide.with(context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_place_placeholder)
                    .error(R.drawable.ic_place_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(width, height)
                    .centerCrop()
            )
            .into(imageView)
    }
    
    /**
     * Load thumbnail image
     */
    fun loadThumbnail(
        context: Context,
        imageUrl: String?,
        imageView: ImageView,
        size: Int = 150
    ) {
        Glide.with(context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(size, size)
                    .centerCrop()
            )
            .into(imageView)
    }
    
    /**
     * Clear image cache for specific ImageView
     */
    fun clearImageCache(context: Context, imageView: ImageView) {
        Glide.with(context).clear(imageView)
    }
    
    /**
     * Clear all image cache
     */
    fun clearAllCache(context: Context) {
        Glide.get(context).clearMemory()
        // Clear disk cache in background thread
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }
}