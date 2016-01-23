package com.neeplayer

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.GlideModule

class CustomGlideModule : GlideModule {
    override fun applyOptions(context: Context?, builder: GlideBuilder?) {
        builder?.setMemoryCache(LruResourceCache(50 * 1024 * 1024))
    }

    override fun registerComponents(context: Context?, glide: Glide?) {
    }
}