package com.neeplayer

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.databinding.AlbumBinding
import com.neeplayer.databinding.SongBinding
import com.neeplayer.model.Album
import com.neeplayer.model.Index
import com.neeplayer.model.Song
import org.jetbrains.anko.onClick


class AlbumSongAdapter(private val context: Context, private val albums: List<Album>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ALBUM_ITEM = 0
    private val SONG_ITEM = 1

    private val types = albums.flatMap { listOf(ALBUM_ITEM).plus(it.songs.map { SONG_ITEM }) }

    private val indices = albums.mapIndexed { albumIndex, album ->
        listOf(Index(albumIndex)).plus(
                album.songs.mapIndexed { songIndex, song -> Index(albumIndex, songIndex) }
        )
    }.flatten()
// TODO:
//    private sealed class Item {
//        class AlbumItem(val album: Album)
//        class SongItem(val song: Song)
//    }
//
//    private val items = albums.flatMap { listOf(Item.AlbumItem(it) as Item).plus(it.songs.map { Item.SongItem(it) as Item}) }

    var onSongClickListener: (Index) -> Unit = {}

    override fun getItemCount(): Int = types.size

    override fun getItemViewType(position: Int): Int = types[position]

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
        when(types[position]) {
            ALBUM_ITEM -> (holder as AlbumViewHolder).binding.album = albums[index.albumIndex]
            SONG_ITEM -> {
                val binding = (holder as SongViewHolder).binding
                binding.song = albums[index.albumIndex].songs[index.songIndex!!]
                binding.root.onClick { onSongClickListener(index) }
            }
        }
    }
}