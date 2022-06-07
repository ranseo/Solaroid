package com.example.solaroid.friend.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.solaroid.friend.fragment.add.FriendDispatchFragment
import com.example.solaroid.friend.fragment.add.FriendReceptionFragment

class FriendAddAdapter(fm:Fragment) : FragmentStateAdapter(fm) {
    val list : List<Fragment> = listOf(FriendDispatchFragment(), FriendReceptionFragment())
    override fun getItemCount(): Int = list.size


    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}