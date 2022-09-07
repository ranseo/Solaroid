package com.ranseo.solaroid.ui.friend.fragment.add.dispatch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.R
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.databinding.FragmentFriendDispatchBinding
import com.ranseo.solaroid.ui.friend.adapter.FriendListAdatper
import com.ranseo.solaroid.ui.friend.adapter.FriendListDataItem
import com.ranseo.solaroid.ui.friend.adapter.OnDispatchClickListener

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


        val adapter = FriendListAdatper(dispatchClickListener = OnDispatchClickListener { friend ->
            viewModel.deleteFriendInDispatchList(friend)
        })

        binding.recFriendDispatch.adapter = adapter

        viewModel.friends.observe(viewLifecycleOwner) {
            if(!it.isNullOrEmpty()) {
                adapter.submitList(it)
            } else {
                val friendEmptyHead = FriendListDataItem.FriendEmptyHead()
                friendEmptyHead.title = FRIEND_EMPTY_TEXT
                adapter.submitList(listOf(friendEmptyHead))
            }
            Log.i(TAG, "friendDistinct.observe : ${it}")
        }

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.removeListener()
    }

    companion object {
        const val TAG = "프렌드_디스패치_프래그먼트"
        const val KEY = "DispatchKey"
        private const val FRIEND_EMPTY_TEXT = "발신이 존재하지 않습니다.\n" +
                "상단의 검색창을 이용해\n\n" +
                "친구 코드를 검색하고\n친구 요청을 해보세요"
    }
}