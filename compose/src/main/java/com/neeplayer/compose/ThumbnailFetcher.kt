package com.neeplayer.compose

import android.content.ContentUris
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.CancellationSignal
import android.provider.MediaStore
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ThumbnailFetcher(private val context: Context) : Fetcher<Uri> {
    override suspend fun fetch(pool: BitmapPool, data: Uri, size: Size, options: Options): FetchResult {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->

                val cancellationSignal = CancellationSignal()
                continuation.invokeOnCancellation { cancellationSignal.cancel() }

                val albumId = requireNotNull(data.lastPathSegment?.toLongOrNull())
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId)

                val androidSize = when (size) {
                    OriginalSize -> android.util.Size(1000, 1000)
                    is PixelSize -> android.util.Size(size.width, size.height)
                }

                val bitmap = context.contentResolver.loadThumbnail(uri, androidSize, cancellationSignal)

                continuation.resume(
                    DrawableResult(
                        drawable = BitmapDrawable(context.resources, bitmap),
                        isSampled = false,
                        dataSource = DataSource.DISK,
                    )
                )
            }
        }
    }

    override fun key(data: Uri) = data.toString()

    override fun handles(data: Uri) = data.scheme == "neeplayer"
}
