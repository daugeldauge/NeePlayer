package com.neeplayer.ui.albums

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.neeplayer.R
import com.neeplayer.databinding.AlbumBinding
import com.neeplayer.databinding.SongBinding
import com.neeplayer.model.AlbumWithSongs
import com.neeplayer.model.Song
import org.jetbrains.anko.onClick

class AlbumSongAdapter(private val context: Context, albums: List<AlbumWithSongs>, private val onSongClicked: (Song) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ALBUM_ITEM = 0
    private val SONG_ITEM = 1

    private sealed class Item {
        class AlbumItem(val albumWithSongs: AlbumWithSongs) : Item()
        class SongItem(val song: Song) : Item()
    }

    private val items = albums.flatMap { listOf(Item.AlbumItem(it)).plus(it.songs.map { Item.SongItem(it) }) }

    private var nowPlaying: Song? = null

    private var paused = false

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Item.AlbumItem -> ALBUM_ITEM
        is Item.SongItem -> SONG_ITEM
    }


    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = DataBindingUtil.bind<SongBinding>(view)!!
    }

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = DataBindingUtil.bind<AlbumBinding>(view)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            ALBUM_ITEM -> AlbumViewHolder(AlbumBinding.inflate(inflater, parent, false).root)
            SONG_ITEM -> {
                val binding = SongBinding.inflate(inflater, parent, false)
                binding.animationNowPlaying.setImageResource(R.drawable.now_playing)
                SongViewHolder(binding.root)
            }
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
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
                    (binding.animationNowPlaying.drawable as AnimatedVectorDrawable).apply {
                        if (paused) {
                            stop()
                        } else {
                            start()
                        }
                    }
                } else {
                    binding.songTrack.visibility = View.VISIBLE
                    binding.animationNowPlaying.visibility = View.GONE
                }
            }
        }
    }

    fun updateNowPlaying(nowPlaying: Song, paused: Boolean) {
        val previous = this.nowPlaying
        this.paused = paused
        this.nowPlaying = nowPlaying
        notifySongChanged(previous)
        notifySongChanged(nowPlaying)
    }

    private fun notifySongChanged(song: Song?) {
        items.mapIndexed { i, item -> if (item is Item.SongItem && item.song == song) i else null }
                .filterNotNull()
                .forEach { notifyItemChanged(it) }
    }
}
