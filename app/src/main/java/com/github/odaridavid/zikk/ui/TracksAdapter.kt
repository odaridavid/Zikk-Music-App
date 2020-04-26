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
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.models.PlayableTrack
import com.github.odaridavid.zikk.utils.invisible
import com.github.odaridavid.zikk.utils.show

internal class TracksAdapter(val onClick: (String?, Int, PlayableTrack) -> Unit) :
    RecyclerView.Adapter<TracksAdapter.TrackViewHolder>() {

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

    override fun onBindViewHolder(
        holder: TrackViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else
            holder.setIsPlaying(payloads[0] as Boolean)
    }


    inner class TrackViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val nowPlayingImageView: ImageView =
            view.findViewById(R.id.track_now_playing_image_view)

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

                setNowPlayingViewVisibility(mediaItem.isPlaying, nowPlayingImageView)

                setOnClickListener {
                    onClick(mediaItem.mediaId, adapterPosition, mediaItems[adapterPosition])
                }
            }
        }

        fun setIsPlaying(isPlaying: Boolean) {
            setNowPlayingViewVisibility(isPlaying, nowPlayingImageView)
        }

        private fun setNowPlayingViewVisibility(isPlaying: Boolean, showPlaying: ImageView) {
            if (isPlaying)
                showPlaying.show()
            else
                showPlaying.invisible()
        }
    }

    override fun getItemCount(): Int {
        return if (::mediaItems.isInitialized) mediaItems.size else 0
    }

    fun updateIsPlaying(position: Int, payload: Any) {
        notifyItemChanged(position, payload)
    }
}