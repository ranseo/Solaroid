package com.example.solaroid.friend.fragment.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentFriendAddBinding

class FriendAddFragment : Fragment() {

    private lateinit var binding : FragmentFriendAddBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_add, container, false)
        return binding.root
    }
}