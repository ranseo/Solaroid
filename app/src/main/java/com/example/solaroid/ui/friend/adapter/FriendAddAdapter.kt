package com.example.solaroid.ui.friend.adapter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.solaroid.models.domain.Profile


class FriendAddAdapter(fm: FragmentManager, lifecycle:Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
    var list : List<Fragment> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = list.size


    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    fun addReceptionFragment(fragment: Fragment, myProfile: Profile) {
        Log.i(TAG,"addReceptionFragment")
        fragment.arguments = Bundle().apply {
            putParcelable("ReceptionKey", myProfile)
        }

        list = list + listOf(fragment)
    }


    fun addDispatchFragment(fragment: Fragment, myProfile:Profile) {
        Log.i(TAG,"addDispatchFragment")
        fragment.arguments = Bundle().apply {
            putParcelable("DispatchKey", myProfile)
        }
        list = list + listOf(fragment)
    }
    
    companion object {
        const val TAG = "프렌드_애드_어댑터"
    }


}