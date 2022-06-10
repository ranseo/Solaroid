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
import com.example.solaroid.friend.adapter.FriendListAdatper

class FriendDispatchFragment() : Fragment() {
    private lateinit var binding : FragmentFriendDispatchBinding

    private lateinit var viewModel: FriendDispatchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_dispatch, container,false)

        viewModel = ViewModelProvider(this)[FriendDispatchViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = FriendListAdatper()

        binding.recFriendDispatch.adapter = adapter

        return binding.root
    }

    companion object {
        const val TAG = "프렌드_디스패치_프래그먼트"
    }
}