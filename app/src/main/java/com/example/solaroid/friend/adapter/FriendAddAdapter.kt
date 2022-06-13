package com.example.solaroid.friend.adapter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.solaroid.friend.fragment.add.dispatch.FriendDispatchFragment
import com.example.solaroid.friend.fragment.add.reception.FriendReceptionFragment

class FriendAddAdapter(fm:Fragment) : FragmentStateAdapter(fm) {
    val list = mutableListOf<Fragment>()
    override fun getItemCount(): Int = list.size


    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    fun addReceptionFragment(fragment: Fragment, friendCode:Long) {
        Log.i(TAG,"addReceptionFragment")
        fragment.arguments = Bundle().apply {
            putLong("ReceptionKey", friendCode)
        }

        list.add(fragment)
    }



    fun addDispatchFragment(fragment: Fragment, friendCode:Long) {
        Log.i(TAG,"addDispatchFragment")
        fragment.arguments = Bundle().apply {
            putLong("DispatchKey", friendCode)
        }
        list.add(fragment)
    }
    
    companion object {
        const val TAG = "프렌드_애드_어댑터"
    }


}