package com.example.solaroid.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumCreateParticipantsBinding
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.ui.friend.adapter.FriendListAdatper
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.ui.friend.adapter.OnDialogClickListener

class AlbumCreateParticipantsDialog(_listener:AlbumCreateParticipantsDialogListener, val friendList:List<FriendListDataItem.DialogProfileDataItem>, val myProfile:Profile) : DialogFragment() {
    internal var listener: AlbumCreateParticipantsDialogListener = _listener

    private lateinit var binding : FragmentAlbumCreateParticipantsBinding

    private lateinit var adapter : FriendListAdatper
    private val participants = mutableListOf<Friend>()

    interface AlbumCreateParticipantsDialogListener {
        fun onParticipantsDialogPositiveClick(friends: List<Friend>, dialog: DialogFragment)
        fun onParticipantsDialogNegativeClick(dialog: DialogFragment)
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album_create_participants,container,false)

        val application = requireNotNull(this.activity).application

        binding.profile = myProfile
        adapter = FriendListAdatper(application = application, dialogClickListener = OnDialogClickListener { friend ->
            addParticipants(friend)
        })

        setTextViewParticipants(participants, myProfile)

        binding.recDialogFriend.adapter = adapter
        adapter.submitList(friendList)


        binding.btnAccept.setOnClickListener {
            listener.onParticipantsDialogPositiveClick(participants, this)
        }

        binding.btnCancel.setOnClickListener {
            listener.onParticipantsDialogNegativeClick(this)
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