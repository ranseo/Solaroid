package com.example.solaroid.friend.fragment.add.dispatch

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentFriendDispatchBinding
import com.example.solaroid.databinding.FragmentFriendReceptionBinding
import com.example.solaroid.domain.Profile
import com.example.solaroid.friend.adapter.FriendListAdatper
import com.example.solaroid.friend.adapter.FriendListDataItem
import com.example.solaroid.friend.adapter.OnDispatchClickListener
import com.example.solaroid.friend.fragment.add.reception.FriendReceptionFragment

class FriendDispatchFragment() : Fragment() {

    private lateinit var binding: FragmentFriendDispatchBinding

    private lateinit var viewModelFactory: FriendDispatchViewModelFactory
    private lateinit var viewModel: FriendDispatchViewModel

    private lateinit var myProfile: Profile

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_dispatch, container, false)

        arguments?.takeIf { it.containsKey(KEY) }?.apply {
            try {
                myProfile = getParcelable<Profile>(KEY)!!
            } catch (error: Exception) {
                Log.d(TAG, "arguments?.takeIf : ${error.message}")
            }
        }


        Log.i(TAG, "myProfile : ${myProfile}")
        viewModelFactory = FriendDispatchViewModelFactory(myProfile)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendDispatchViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        val adapter = FriendListAdatper(dispatchClickListener = OnDispatchClickListener { friend, click ->
            if(click) {

            } else {

            }
        })

        binding.recFriendDispatch.adapter = adapter

        viewModel.friends.observe(viewLifecycleOwner) {
            it?.let { dispatchFriend ->
                adapter.submitList(dispatchFriend)
            }

            Log.i(TAG, "friendDistinct.observe : ${it}")
        }

        return binding.root
    }

    companion object {
        const val TAG = "프렌드_디스패치_프래그먼트"
        const val KEY = "DispatchKey"


    }
}