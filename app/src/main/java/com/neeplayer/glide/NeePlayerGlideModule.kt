@file:Suppress("PackageDirectoryMismatch", "unused", "UNUSED_PARAMETER") // Some tweaks to make Glide work without kapt
package com.bumptech.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.neeplayer.glide.AlbumThumbnailModelLoader

private class GeneratedAppGlideModuleImpl(context: Context) : GeneratedAppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(String::class.java, Bitmap::class.java, object : ModelLoaderFactory<String, Bitmap> {
            override fun build(multiFactory: MultiModelLoaderFactory) = AlbumThumbnailModelLoader(context)
            override fun teardown() = Unit
        })
    }

    override fun isManifestParsingEnabled(): Boolean = false

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setMemoryCache(LruResourceCache(50 * 1024 * 1024))
    }

    override fun getExcludedModuleClasses(): MutableSet<Class<*>> = mutableSetOf()
}
