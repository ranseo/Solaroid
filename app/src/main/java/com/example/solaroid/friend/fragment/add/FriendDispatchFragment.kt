package com.example.solaroid.friend.fragment.add

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentFriendReceptionBinding

class FriendDispatchFragment() : Fragment() {
    private lateinit var binding : FragmentFriendReceptionBinding

    private lateinit var pfm : FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_reception, container,false)


        return binding.root
    }

    companion object {
        const val TAG = "프렌드_디스패치_프래그먼트"
    }
}