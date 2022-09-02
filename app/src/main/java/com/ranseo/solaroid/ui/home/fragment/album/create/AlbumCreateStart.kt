package com.ranseo.solaroid.ui.home.fragment.album.create

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentAlbumCreateStartBinding
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.room.SolaroidDatabase
import com.ranseo.solaroid.ui.friend.adapter.FriendListAdatper
import com.ranseo.solaroid.ui.friend.adapter.FriendListDataItem
import com.ranseo.solaroid.ui.friend.adapter.OnDialogClickListener

class AlbumCreateStart() : Fragment() {

    private lateinit var binding : FragmentAlbumCreateStartBinding

    private lateinit var viewModel : AlbumCreateViewModel
    private lateinit var viewModelFactory: AlbumCreateViewModelFactory

    private val TAG = "AlbumCreateStart"

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


        binding.recStartFriend.adapter = adapter

        viewModel.myFriendList!!.observe(viewLifecycleOwner) {
            it?.let{ list ->
                if(!list.isNullOrEmpty()) {
                    adapter.submitList(list)
                } else {
                    adapter.submitList(listOf(FriendListDataItem.FriendEmptyHead))
                }

            }
        }

        viewModel.participants.observe(viewLifecycleOwner) {
            it?.let{
                Log.i(TAG, "participants observe : ${it}")
            }
        }

        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let{
                Log.i(TAG, "myProfile observe : ${it.nickname}")
            }
        }




        binding.btnAccept.setOnClickListener {
            viewModel.checkParticipants()
            findNavController().navigate(
                AlbumCreateStartDirections.actionStartToFinal()
            )
        }

        binding.btnCancel.setOnClickListener {
            viewModel.setNullCreateProperty()
            requireActivity().onBackPressed()
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