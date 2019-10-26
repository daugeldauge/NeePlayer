@file:Suppress("PackageDirectoryMismatch", "unused", "UNUSED_PARAMETER") // Some tweaks to make Glide work without kapt
package com.bumptech.glide

import android.content.Context
import com.bumptech.glide.load.engine.cache.LruResourceCache

private class GeneratedAppGlideModuleImpl(context: Context) : GeneratedAppGlideModule() {

    override fun isManifestParsingEnabled(): Boolean = false

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setMemoryCache(LruResourceCache(50 * 1024 * 1024))
    }

    override fun getExcludedModuleClasses(): MutableSet<Class<*>> = mutableSetOf()
}