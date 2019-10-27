package com.neeplayer.ui.artists

import com.neeplayer.model.Artist
import com.neeplayer.model.ArtistImagesStorage
import com.neeplayer.model.Database
import com.neeplayer.network.deezer.DeezerApi
import com.neeplayer.network.spotify.SpotifyApi
import com.neeplayer.ui.Router
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class ArtistsPresenter(
        private val database: Database,
        private val artistImagesStorage: ArtistImagesStorage,
        private val deezer: DeezerApi,
        private val spotify: SpotifyApi,
        private val router: Router
) {

    fun bind(scope: CoroutineScope, view: ArtistsView) {
        val artists = database.getArtists()
        view.showArtists(artists)
        scope.launch {
            fetchUnknownArtistImages(artists, view)
        }
    }

    fun onArtistClicked(artist: Artist) {
        router.gotToAlbums(artist)
    }

    private suspend fun fetchUnknownArtistImages(artists: List<Artist>, view: ArtistsView) {
        artists.filter { it.imageUrl == null }.forEach { artist ->
            val image = fetchImageFromSpotify(artist) ?: fetchImageFromDeezer(artist)
            if (image != null) {
                artistImagesStorage.put(artist.name, image)
                view.updateArtist(artist.withImage(image))
            } else {
                Timber.w("Couldn't fetch artist image url")
            }
        }
    }

    private suspend fun fetchImageFromSpotify(artist: Artist): String? {
        val response = spotify.searchArtist(artist.name).success() ?: return null

        return response.body
                .artists
                .items
                .firstOrNull()
                ?.images
                ?.run { getOrNull(1) ?: firstOrNull() } // Take second-quality image (if present)
                ?.url
    }

    private suspend fun fetchImageFromDeezer(artist: Artist): String? {
        return deezer.searchArtist(artist.name)
                .success()
                ?.body
                ?.data
                ?.firstOrNull()
                ?.artist
                ?.pictureMedium
    }

}