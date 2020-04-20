package com.github.odaridavid.zikk.tracks;

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
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.utils.convertMillisecondsToDuration


internal class TracksAdapter(val onClick: (Long) -> Unit) :
    ListAdapter<Track, TracksAdapter.TrackViewHolder>(TrackDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int): Unit =
        getItem(position).let { holder.bind(it) }

    inner class TrackViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(track: Track) {
            with(view) {
                val trackAlbumArt = findViewById<ImageView>(R.id.song_art_image_view).apply {
                    this.load(Uri.parse(track.albumArt))
                    contentDescription = "${track.title} Album Art"

                }
                val trackDuration = findViewById<TextView>(R.id.song_duration_text_view).apply {
                    text = convertMillisecondsToDuration(track.duration.toLong())
                }
                val trackArtist = findViewById<TextView>(R.id.song_artist_text_view).apply {
                    text = if (track.artist.contains("unknown")) "Unknown" else track.artist
                }
                val trackTitle = findViewById<TextView>(R.id.track_title_text_view).apply {
                    text = track.title
                }
                setOnClickListener { onClick(track.id) }
            }
        }
    }

    companion object {
        val TrackDiffUtil = object : DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem.albumId == newItem.albumId && oldItem.title == newItem.title && oldItem.track == newItem.track
            }
        }
    }
}