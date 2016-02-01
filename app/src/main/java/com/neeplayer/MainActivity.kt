package com.neeplayer

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.ArtistColumns
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import kotlinx.android.synthetic.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.warn
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

class MainActivity : Activity(), AnkoLogger {

    private var artistImages: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        artistImages = getSharedPreferences("ArtistImages", 0)

        artist_list.adapter = ArtistAdapter(this, getArtistList()) {
            startActivity<ArtistActivity>(
                    "ARTIST_NAME" to it.name,
                    "ARTIST_ID"   to it.id
            )
        }
        artist_list.layoutManager = LinearLayoutManager(this)

    }

    fun getArtistList(): List<Artist> {
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

        val cursor = contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                arrayOf(BaseColumns._ID, ArtistColumns.ARTIST, ArtistColumns.NUMBER_OF_TRACKS, ArtistColumns.NUMBER_OF_ALBUMS),
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        (menu?.findItem(R.id.now_playing_item)?.icon as AnimatedVectorDrawable).start()
        return true
    }


}
