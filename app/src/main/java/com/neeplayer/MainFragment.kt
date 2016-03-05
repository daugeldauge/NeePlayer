package com.neeplayer

import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.AnkoLogger
import org.json.JSONObject
import retrofit2.Retrofit
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import org.jetbrains.anko.warn
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import java.util.*

class MainFragment : Fragment(), AnkoLogger {

    private var artistImages: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artistImages = activity.getSharedPreferences("ArtistImages", 0)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view as RecyclerView

        recyclerView.adapter = ArtistAdapter(activity, getArtistList()) {
            (activity as MainActivity).navigateToArtistFragment(it)
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu);
        (menu?.findItem(R.id.now_playing_item)?.icon as AnimatedVectorDrawable).start()
    }

    private fun getArtistList(): List<Artist> {
        val httpClient = OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                ).build();

        val lastFm = Retrofit.Builder()
                .baseUrl("http://ws.audioscrobbler.com")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build().create(LastFmService::class.java)

        val list = ArrayList<Artist>()

        val cursor = activity.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                arrayOf(BaseColumns._ID, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS, MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS),
                null,
                null,
                null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val artist = Artist(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3))

                val image = artistImages?.getString(artist.name, null)
                if (image.isNullOrEmpty()) {
                    lastFm.getArtistInfo(artist.name)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map {
                                getArtistImageUrl(it.string())
                            }
                            .subscribe({
                                artist.imageURL = it
                                artistImages?.edit()?.putString(artist.name, it)?.commit()
                            }, {
                                warn("Couldn't retrieve artist image", it)
                            })
                } else {
                    artist.imageURL = image
                }

                list.add(artist)
            } while (cursor.moveToNext())

            cursor.close()
        }

        return list
    }

    private fun getArtistImageUrl(artistJsonInfo: String): String? {
        val images = JSONObject(artistJsonInfo).getJSONObject("artist").getJSONArray("image")

        for (i in 0 until images.length()) {
            val image = images.getJSONObject(i)
            val size = image.getString("size")
            if (size == "extralarge") {
                return image.getString("#text")
            }
        }
        return null
    }

}