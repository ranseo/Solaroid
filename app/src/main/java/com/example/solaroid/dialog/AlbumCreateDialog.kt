package com.example.solaroid.dialog

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.solaroid.R
import com.example.solaroid.custom.view.AlbumThumbnailView
import com.example.solaroid.custom.view.getBitmapFromView
import com.example.solaroid.databinding.FragmentAlbumCreateBinding
import com.example.solaroid.getAlbumIdWithFriendCodes
import com.example.solaroid.getAlbumNameWithFriendsNickname
import com.example.solaroid.joinProfileImgListToString
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.Profile

class AlbumCreateDialog(val listener : AlbumCreateDialogListener ,val participants : List<Friend>, val myProfile: Friend) : DialogFragment() {

    private lateinit var binding : FragmentAlbumCreateBinding

    private lateinit var thumbnail : Bitmap
    private lateinit var albumId : String
    private lateinit var albumName : String


    interface AlbumCreateDialogListener {
        fun onCreateDialogPositiveClick(albumId:String, albumName:String, thumbnail:Bitmap, dialog: DialogFragment)
        fun onCreateDialogNegativeClick(dialog: DialogFragment)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_album_create, container, false)
        albumName = getAlbumNameWithFriendsNickname(participants.map { it.nickname }, myProfile.nickname)
        albumId = getAlbumIdWithFriendCodes(participants.map{it.friendCode})



        binding.thumbnail = joinProfileImgListToString(listOf(myProfile)+participants)
        binding.participants = participants.size
        binding.tvAlbumName.text = albumName



        binding.albumThumbnail.addOnLayoutChangeListener { p0, _, _, _, _, _, _, _, _ ->
            if (p0 != null) {
                thumbnail = (p0 as AlbumThumbnailView).getBitmapFromView()
            }
        }


        binding.btnAccept.setOnClickListener {
            listener.onCreateDialogPositiveClick(albumId, albumName, thumbnail, this)
        }

        binding.btnCancel.setOnClickListener {
            listener.onCreateDialogNegativeClick(this)
        }

        return binding.root
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

}