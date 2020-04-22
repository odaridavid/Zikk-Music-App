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
import android.support.v4.media.MediaBrowserCompat
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


internal class MediaItemAdapter(val onClick: (String?) -> Unit) :
    ListAdapter<MediaBrowserCompat.MediaItem, MediaItemAdapter.TrackViewHolder>(
        TrackDiffUtil
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int): Unit =
        getItem(position).let { holder.bind(it) }

    inner class TrackViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(mediaItem: MediaBrowserCompat.MediaItem) {
            with(view) {
                //TODO Create separate layout for this adapter
                findViewById<ImageView>(R.id.track_art_image_view).apply {
                    this.load(mediaItem.description.iconUri)
                    contentDescription = "${mediaItem.description.title} Album Art"

                }
                findViewById<TextView>(R.id.track_title_text_view).apply {
                    text = mediaItem.description.title
                }
                findViewById<TextView>(R.id.track_artist_text_view).apply {
                    text = mediaItem.description.subtitle
                }
                setOnClickListener { onClick(mediaItem.description.mediaId) }
            }
        }
    }

    companion object {
        val TrackDiffUtil = object : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {
            override fun areItemsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem.description.mediaId == newItem.description.mediaId
            }
        }
    }
}