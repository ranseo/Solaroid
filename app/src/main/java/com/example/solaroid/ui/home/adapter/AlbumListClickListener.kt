package com.example.solaroid.ui.home.adapter

import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.domain.RequestAlbum

class AlbumListClickListener(val onAlbumClickListener : ((album: Album)->Unit)? , val onRequestAlbumClickListener: ((album:RequestAlbum)->Unit)? ) {
    fun onClick(album:Album) {
        onAlbumClickListener!!(album)
    }

    fun onClick(album:RequestAlbum) {
        onRequestAlbumClickListener!!(album)
    }
}