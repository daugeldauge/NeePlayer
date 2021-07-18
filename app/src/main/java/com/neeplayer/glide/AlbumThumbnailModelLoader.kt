package com.neeplayer.glide

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey

class AlbumThumbnailModelLoader(private val context: Context) : ModelLoader<String, Bitmap> {
    override fun buildLoadData(model: String, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), AlbumThumbnailDataFetcher(
            context = context,
            model = model,
            width = width,
            height = height,
        ))
    }

    override fun handles(model: String): Boolean {
        return model.startsWith("neeplayer://")
    }

}

private class AlbumThumbnailDataFetcher(
    private val context: Context,
    private val model: String,
    private val width: Int,
    private val height: Int,
) : DataFetcher<Bitmap> {


    private val cancellationSignal = CancellationSignal()

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val albumId = requireNotNull(Uri.parse(model).lastPathSegment?.toLongOrNull())
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId)
        val bitmap = context.contentResolver.loadThumbnail(uri, Size(width, height), cancellationSignal)
        callback.onDataReady(bitmap)
    }

    override fun cleanup() {}

    override fun cancel() {
        cancellationSignal.cancel()
    }

    override fun getDataClass() = Bitmap::class.java

    override fun getDataSource() = DataSource.LOCAL

}
