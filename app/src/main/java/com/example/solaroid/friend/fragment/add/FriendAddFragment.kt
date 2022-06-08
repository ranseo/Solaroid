package com.example.solaroid.friend.fragment.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentFriendAddBinding
import com.example.solaroid.friend.adapter.FriendAddAdapter
import com.google.android.material.tabs.TabLayoutMediator

class FriendAddFragment : Fragment() {
    private lateinit var binding : FragmentFriendAddBinding

    private lateinit var viewModel : FriendAddViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_add, container, false)

        viewModel = ViewModelProvider(this)[FriendAddViewModel::class.java]

        binding.viewModel=viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = FriendAddAdapter(this)
        binding.viewPageFriendAdd.adapter = adapter

        setTabLayout()

        return binding.root
    }


    private fun setTabLayout() {
        TabLayoutMediator(binding.tablayoutFriendAdd, binding.viewPageFriendAdd) { tab, pos ->
            tab.text = if (pos == 0) "수신 목록" else "발신 목록"
        }.attach()
    }
}