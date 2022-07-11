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
import com.example.solaroid.ui.friend.adapter.FriendListAdatper
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.ui.friend.adapter.OnDialogClickListener

class AlbumCreateParticipantsDialog(_listener:AlbumCreateDialogListener, val friendList:List<FriendListDataItem.DialogProfileDataItem>) : DialogFragment() {
    internal var listener: AlbumCreateDialogListener = _listener

    private lateinit var binding : FragmentAlbumCreateParticipantsBinding

    private lateinit var adapter : FriendListAdatper
    private val participants = mutableListOf<Friend>()

    interface AlbumCreateDialogListener {
        fun onDialogPositiveClick(friends: List<Friend>, dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album_create_participants,container,false)

        val application = requireNotNull(this.activity).application

        adapter = FriendListAdatper(application = application, dialogClickListener = OnDialogClickListener { friend ->
            addParticipants(friend)
        })


        binding.recDialogFriend.adapter = adapter
        adapter.submitList(friendList)


        binding.btnAccept.setOnClickListener {
            listener.onDialogPositiveClick(participants, this)
        }

        binding.btnCancel.setOnClickListener {
            listener.onDialogNegativeClick(this)
        }


        return binding.root
    }

    fun addParticipants(friend:Friend) {
        if(participants.contains(friend)) {
            participants.remove(friend)
        } else {
            participants.add(friend)
        }
        setTextViewParticipants(participants)
    }

    fun setTextViewParticipants(participants:List<Friend>) {
        val text = "참여자 : " + participants.fold("") { acc, v ->
            acc + v.nickname + ", "
        }.dropLast(2)

        binding.tvParticipants.text = text
    }


}