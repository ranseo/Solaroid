package com.ranseo.solaroid.ui.friend.fragment.add.reception

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.R
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.databinding.FragmentFriendReceptionBinding
import com.ranseo.solaroid.ui.friend.adapter.FriendListAdatper
import com.ranseo.solaroid.ui.friend.adapter.OnReceptionClickListener
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchStatus

class FriendReceptionFragment() : Fragment() {
    private lateinit var binding: FragmentFriendReceptionBinding

    private lateinit var viewModel: FriendReceptionViewModel
    private lateinit var viewModelFactory: FriendReceptionViewModelFactory

    private lateinit var myProfile: Profile

    override fun onStart() {
        super.onStart()
        Log.i(TAG,"onStart()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG,"onDestroy")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_reception, container, false)

        arguments?.takeIf { it.containsKey(KEY) }?.apply {
            try {
                myProfile = getParcelable<Profile>(KEY)!!

            } catch (error:Exception) {
                Log.d(TAG,"arguments?.takeIf : ${error.message}")
            }
        }

        Log.i(TAG, "friendCode : ${myProfile}")
        viewModelFactory = FriendReceptionViewModelFactory(myProfile)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendReceptionViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.clickAction.observe(viewLifecycleOwner) {
            if(it) {
                viewModel.deleteReceptionList()

                val flag = viewModel.isClick.value!!.peekContent()
                val friend = viewModel.friend.value ?: return@observe
                val friendCode = convertHexStringToLongFormat(friend.friendCode)

                if (flag) {

                    viewModel.setValueMyFriendList(friend)
                    viewModel.setValueTmpFrientList(friendCode)
                    viewModel.setValueDispatchList(friendCode, DispatchStatus.ACCEPT)
                } else {
                    viewModel.setValueDispatchList(friendCode, DispatchStatus.DECLINE)
                }
            }
        }


        val adapter =
            FriendListAdatper(receptionClickListener = OnReceptionClickListener { friend, flag ->
                if (flag) {
                    viewModel.onAccept(friend)
                } else {
                    viewModel.onDecline(friend)
                }
            })

        binding.recFriendReception.adapter = adapter
        return binding.root
    }

    companion object {
        const val TAG = "프렌드_리셉션_프래그먼트"
        const val KEY = "ReceptionKey"
    }
}