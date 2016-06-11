package com.neeplayer.ui.adapters

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.databinding.AlbumBinding
import com.neeplayer.databinding.SongBinding
import com.neeplayer.model.*
import org.jetbrains.anko.onClick

import com.neeplayer.R
import com.neeplayer.lollipop
import com.neeplayer.targetApi

class AlbumSongAdapter(private val context: Context, private val albums: List<AlbumWithSongs>, private val onSongClicked: (Song) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ALBUM_ITEM = 0
    private val SONG_ITEM = 1

    private sealed class Item {
        class AlbumItem(val albumWithSongs: AlbumWithSongs) : Item()
        class SongItem(val song: Song) : Item()
    }

    private val items = albums.flatMap { listOf(Item.AlbumItem(it)).plus(it.songs.map { Item.SongItem(it) }) }

    var nowPlaying: Song? = null
        set(value) {
            notifySongChanged(field)
            notifySongChanged(value)
            field = value
        }

    var paused = false

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when(items[position]) {
        is Item.AlbumItem -> ALBUM_ITEM
        is Item.SongItem -> SONG_ITEM
    }


    class SongViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = DataBindingUtil.bind<SongBinding>(view)
    }

    class AlbumViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = DataBindingUtil.bind<AlbumBinding>(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val inflater = LayoutInflater.from(context)
        return when(viewType) {
            ALBUM_ITEM -> AlbumViewHolder(AlbumBinding.inflate(inflater, parent, false).root)
            SONG_ITEM -> {
                val binding = SongBinding.inflate(inflater, parent, false)
                binding.animationNowPlaying.setImageResource(
                        lollipop({ R.drawable.now_playing },{ R.drawable.ic_equalizer_black_24dp })
                )
                SongViewHolder(binding.root)
            }
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val item = items[position]
        when(item) {
            is Item.AlbumItem -> {
                val binding = (holder as AlbumViewHolder).binding
                binding.album = item.albumWithSongs.album
                binding.info = item.albumWithSongs.info
            }
            is Item.SongItem -> {
                val binding = (holder as SongViewHolder).binding
                val song = item.song
                binding.song = song
                binding.root.onClick {
                    onSongClicked(song)
                }

                if (song == nowPlaying) {
                    binding.songTrack.visibility = View.GONE
                    binding.animationNowPlaying.visibility = View.VISIBLE
                    lollipop {
                        (binding.animationNowPlaying.drawable as AnimatedVectorDrawable).apply {
                            if (paused) {
                                stop()
                            }  else {
                                start()
                            }
                        }
                    }
                } else {
                    binding.songTrack.visibility = View.VISIBLE
                    binding.animationNowPlaying.visibility = View.GONE
                }
            }
        }
    }

    private fun notifySongChanged(song: Song?) {
        items.mapIndexed { i, item -> if (item is Item.SongItem && item.song == song) i else null }
                .filterNotNull()
                .forEach { notifyItemChanged(it) }
    }
}
