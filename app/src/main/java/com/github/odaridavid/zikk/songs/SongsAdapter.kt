package com.github.odaridavid.zikk.songs;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.odaridavid.zikk.R

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
class SongsAdapter : ListAdapter<Song, SongsAdapter.SongViewHolder>(
    SongsDiffUtil
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int): Unit =
        getItem(position).let { holder.bind(it) }

    inner class SongViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Song) {
            TODO("Implementation")
        }
    }

    companion object {
        val SongsDiffUtil = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                TODO("Implementation")
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                TODO("Implementation")
            }
        }
    }
}