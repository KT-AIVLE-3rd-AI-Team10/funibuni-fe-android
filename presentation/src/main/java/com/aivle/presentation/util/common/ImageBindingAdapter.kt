package com.aivle.presentation.util.common

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.aivle.presentation.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView

object ImageBindingAdapter {

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun imageUrl(imageView: ImageView, imageUrl: String?) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .centerCrop()
            .error(R.drawable.placeholder_1440)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }
}