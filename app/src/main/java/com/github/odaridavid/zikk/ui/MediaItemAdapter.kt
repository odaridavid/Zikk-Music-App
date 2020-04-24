package com.github.odaridavid.zikk.ui;

/**
 *
 * Copyright 2020 David Odari
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 **/
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.github.odaridavid.zikk.PlayableTrack
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.utils.invisible
import com.github.odaridavid.zikk.utils.show
import timber.log.Timber


internal class MediaItemAdapter(
    val onClick: (String?, Int, PlayableTrack) -> Unit
) :
    RecyclerView.Adapter<MediaItemAdapter.TrackViewHolder>() {

    private lateinit var mediaItems: MutableList<PlayableTrack>

    fun setList(mediaItem: MutableList<PlayableTrack>) {
        mediaItems = mediaItem
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(mediaItems[position])
    }


    inner class TrackViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(mediaItem: PlayableTrack) {
            with(view) {
                findViewById<ImageView>(R.id.track_art_image_view).apply {
                    this.load(mediaItem.icon)
                    contentDescription = "${mediaItem.title} Album Art"
                }
                findViewById<TextView>(R.id.track_title_text_view).apply {
                    text = mediaItem.title
                }
                findViewById<TextView>(R.id.track_artist_text_view).apply {
                    text = mediaItem.artist
                }
                val showPlaying = findViewById<ImageView>(R.id.track_now_playing_image_view)
                if (mediaItem.isPlaying) {
                    Timber.d("Is Playing")
                    showPlaying.show()
                } else {
                    showPlaying.invisible()
                }
                setOnClickListener {
                    onClick(mediaItem.mediaId, adapterPosition, mediaItems[adapterPosition])
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return if (::mediaItems.isInitialized)
            mediaItems.size
        else 0
    }

}