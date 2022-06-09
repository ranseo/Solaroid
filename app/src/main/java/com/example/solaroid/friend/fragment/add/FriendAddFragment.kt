package com.example.solaroid.friend.fragment.add

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentFriendAddBinding
import com.example.solaroid.dialog.NormalDialogFragment
import com.example.solaroid.friend.activity.FriendActivity
import com.example.solaroid.friend.adapter.FriendAddAdapter
import com.google.android.material.tabs.TabLayoutMediator

class FriendAddFragment : Fragment(), NormalDialogFragment.NormalDialogListener {
    private lateinit var binding: FragmentFriendAddBinding

    private lateinit var viewModel: FriendAddViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_add, container, false)

        viewModel = ViewModelProvider(this)[FriendAddViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = FriendAddAdapter(this)
        binding.viewPageFriendAdd.adapter = adapter

        setTabLayout()

        viewModel.myProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                val activity = this.requireActivity() as FriendActivity
                activity.setActionBarTitle("나의 친구코드 : ${profile.friendCode}")
            }
        }

        viewModel.friendRequest.observe(viewLifecycleOwner) { request ->

        }

        return binding.root
    }

    private fun showDialog() {
        val newFragment = NormalDialogFragment(this,"친구 요청을 하시겠습니까?", "요청", "아니요")
        newFragment.show(parentFragmentManager, "NormalDialog")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }


    private fun setTabLayout() {
        TabLayoutMediator(binding.tablayoutFriendAdd, binding.viewPageFriendAdd) { tab, pos ->
            tab.text = if (pos == 0) "수신" else "발신"
        }.attach()
    }

    override fun onDetach() {
        val activity = this.requireActivity() as FriendActivity
        activity.setActionBarTitle("")
        super.onDetach()
    }

    companion object {
        const val TAG = "프렌드_애드_프래그먼트"
    }


}