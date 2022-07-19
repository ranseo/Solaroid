package com.example.solaroid.ui.home.fragment.album.create

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumCreateParticipantsBinding
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.ui.friend.adapter.FriendListAdatper
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.ui.friend.adapter.OnDialogClickListener

class AlbumCreateStart(val friendList:List<FriendListDataItem.DialogProfileDataItem>, val myProfile:Profile) : Fragment() {

    private lateinit var binding : FragmentAlbumCreateParticipantsBinding

    private lateinit var adapter : FriendListAdatper
    private val participants = mutableListOf<Friend>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album_create_start,container,false)

        val application = requireNotNull(this.activity).application

        binding.profile = myProfile
        adapter = FriendListAdatper(application = application, dialogClickListener = OnDialogClickListener { friend ->
            addParticipants(friend)
        })

        setTextViewParticipants(participants, myProfile)

        binding.recDialogFriend.adapter = adapter
        adapter.submitList(friendList)


        binding.btnAccept.setOnClickListener {

        }

        binding.btnCancel.setOnClickListener {

        }


        return binding.root
    }



    fun addParticipants(friend:Friend) {
        if(participants.contains(friend)) {
            participants.remove(friend)
        } else {
            participants.add(friend)
        }
        setTextViewParticipants(participants, myProfile)
    }

    fun setTextViewParticipants(participants:List<Friend>, myProfile: Profile) {
        val text = "참여자 : " + participants.fold("${myProfile.nickname}, ") { acc, v ->
            acc + v.nickname + ", "
        }.dropLast(2)

        binding.tvParticipants.text = text
    }


}