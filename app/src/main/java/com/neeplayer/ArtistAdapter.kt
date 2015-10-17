package com.neeplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

import kotlinx.android.synthetic.artist.view.*;


class ArtistAdapter(context: Context, private val artists: List<Artist>) : BaseAdapter() {
    private val songInf: LayoutInflater
    private val imageLoader: ImageLoader

    init {
        songInf = LayoutInflater.from(context)

        val options = DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build()

        val config = ImageLoaderConfiguration.Builder(context)
                .memoryCacheSize(100 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .defaultDisplayImageOptions(options)
                .build()

        imageLoader = ImageLoader.getInstance()
        imageLoader.init(config)
    }

    override fun getCount(): Int = artists.size()

    override fun getItem(position: Int): Any = artists.get(position)

    override fun getItemId(position: Int): Long = 0

    internal data class ViewHolder(
        var nameView: TextView,
        var descriptionView: TextView,
        var imageView: ImageView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: let {
            val view = songInf.inflate(R.layout.artist, parent, false)
            view.tag = ViewHolder(view.artist_name, view.artist_description, view.artist_image)
            view
        }

        val holder = view.tag as ViewHolder
        val artist = artists[position]

        holder.nameView.text = artist.name
        holder.descriptionView.text = "%d albums, %d songs".format(artist.numberOfAlbums, artist.numberOfSongs)

        imageLoader.displayImage(artist.imageURL, holder.imageView)

        return view
    }

}
