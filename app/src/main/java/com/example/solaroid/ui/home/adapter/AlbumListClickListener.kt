package com.example.solaroid.ui.home.adapter

import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.domain.RequestAlbum

class AlbumListClickListener(
    val onAlbumClickListener: ((album: Album) -> Unit)? = null,
    val onAlbumLongClickListener: ((album: Album) -> Unit)? = null,
    val onRequestAlbumClickListener: ((album: RequestAlbum) -> Unit)? = null
) {
    fun onClick(album: Album) {
        onAlbumClickListener!!(album)
    }

    fun onLongClick(album: Album) : Boolean{
        onAlbumLongClickListener!!(album)
        return true
    }

    fun onClick(album: RequestAlbum) {
        onRequestAlbumClickListener!!(album)
    }
}