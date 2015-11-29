package com.neeplayer

import android.app.Activity
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.ArtistColumns
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.activity_main.artist_list
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.warn
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*

class MainActivity : Activity(), AnkoLogger {

    internal var artistImages: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        artistImages = getSharedPreferences("ArtistImages", 0)

        val artistAdapter = ArtistAdapter(this, getArtistList())
        artist_list.adapter = artistAdapter
        artist_list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val artist = parent.getItemAtPosition(position) as Artist
            startActivity<ArtistActivity>(
                    "ARTIST_NAME" to artist.name,
                    "ARTIST_ID"   to artist.id
            )
        }

    }

    fun getArtistList(): List<Artist> {
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
                    RetrieveArtistImageUrl().execute(artist)
                } else {
                    artist.imageURL = image
                }

                list.add(artist)
            } while (cursor.moveToNext())

            cursor.close()
        }

        return list
    }

    internal inner class RetrieveArtistImageUrl : AsyncTask<Artist, Void, Void>() {
        override fun doInBackground(vararg params: Artist): Void? {
            try {
                val apiKey = "76b52a83c8c82ae436524353bcea2da0"
                val uri = Uri.Builder()
                        .scheme("http")
                        .authority("ws.audioscrobbler.com")
                        .appendPath("2.0")
                        .appendQueryParameter("method", "artist.getinfo")
                        .appendQueryParameter("artist", params[0].name)
                        .appendQueryParameter("api_key", apiKey)
                        .appendQueryParameter("format", "json")
                        .build()

                val response = URL(uri.toString()).openConnection().inputStream
                val responseString = Scanner(response).useDelimiter("\\A").next()

                val json = JSONObject(responseString)
                val artistInfo = json.getJSONObject("artist")
                val images = artistInfo.getJSONArray("image")

                for (i in 0 until images.length()) {
                    val image = images.getJSONObject(i)
                    val size = image.getString("size")
                    if (size == "extralarge") {
                        val imageURL = image.getString("#text")
                        params[0].imageURL = imageURL

                        val editor = artistImages?.edit()
                        editor?.putString(params[0].name, imageURL)
                        editor?.commit()

                        return null
                    }
                }
            } catch(e: JSONException) {
                warn("Couldn't retrieve artist image", e)
            } catch(e: IOException) {
                warn("Couldn't retrieve artist image", e)
            }

            return null
        }
    }


}
