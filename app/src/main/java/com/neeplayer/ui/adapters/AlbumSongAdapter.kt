package com.neeplayer.ui.adapters

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.databinding.AlbumBinding
import com.neeplayer.databinding.SongBinding
import com.neeplayer.model.*
import org.jetbrains.anko.onClick


class AlbumSongAdapter(private val context: Context, private val albums: List<AlbumWithSongs>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ALBUM_ITEM = 0
    private val SONG_ITEM = 1

    private val indices = albums.mapIndexed { albumIndex, album ->
        listOf(Index.Album(albumIndex)).plus(
                album.songs.mapIndexed { songIndex, song -> Index.Song(albumIndex, songIndex) }
        )
    }.flatten()

    private val songs = albums.flatMap { it.songs }

    override fun getItemCount(): Int = indices.size

    override fun getItemViewType(position: Int): Int = when(indices[position]) {
        is Index.Album -> ALBUM_ITEM
        is Index.Song -> SONG_ITEM
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
            SONG_ITEM -> SongViewHolder(SongBinding.inflate(inflater, parent, false).root)
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val index = indices[position]
        when(index) {
            is Index.Album -> {
                val binding = (holder as AlbumViewHolder).binding
                val albumWithSongs = albums[index.value]
                binding.album = albumWithSongs.album
                binding.info = albumWithSongs.info
            }
            is Index.Song -> {
                val binding = (holder as SongViewHolder).binding
                val song = albums[index.albumIndex].songs[index.songIndex]
                binding.song = song
                binding.root.onClick {
                    Model.nowPlaying = Playlist(songs, songs.indexOf(song))
                    Model.paused = false
                }

                val listener = {
                    if (song == Model.nowPlaying?.currentSong) {
                        binding.songTrack.visibility = View.GONE
                        binding.animationNowPlaying.visibility = View.VISIBLE
                        (binding.animationNowPlaying.drawable as AnimatedVectorDrawable).start()
                    } else {
                        binding.songTrack.visibility = View.VISIBLE
                        binding.animationNowPlaying.visibility = View.GONE
                    }
                }

                //TODO Fix memory leak
                Model.addNowPlayingListener(listener)
            }
        }
    }


}