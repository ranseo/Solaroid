package com.example.solaroid.ui.home.fragment.album.create

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumCreateStartBinding
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.ui.friend.adapter.FriendListAdatper
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.ui.friend.adapter.OnDialogClickListener

class AlbumCreateStart(val friendList:List<FriendListDataItem.DialogProfileDataItem>) : Fragment() {

    private lateinit var binding : FragmentAlbumCreateStartBinding

    private lateinit var viewModel : AlbumCreateViewModel
    private lateinit var viewModelFactory: AlbumCreateViewModelFactory

    private lateinit var adapter : FriendListAdatper
    private val participants = mutableListOf<Friend>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album_create_start,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao

        viewModelFactory = AlbumCreateViewModelFactory(dataSource)
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory)[AlbumCreateViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = FriendListAdatper(application = application, dialogClickListener = OnDialogClickListener { friend ->
            addParticipants(friend)
        })


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
        viewModel.setParticipants(participants)
    }




}