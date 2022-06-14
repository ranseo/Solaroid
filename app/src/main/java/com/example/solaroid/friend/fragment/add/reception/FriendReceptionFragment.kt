package com.example.solaroid.friend.fragment.add.reception

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.databinding.FragmentFriendReceptionBinding
import com.example.solaroid.friend.adapter.FriendListAdatper
import com.example.solaroid.friend.adapter.OnReceptionClickListener
import com.example.solaroid.friend.fragment.add.dispatch.DispatchStatus

class FriendReceptionFragment() : Fragment() {
    private lateinit var binding : FragmentFriendReceptionBinding

    private lateinit var viewModel: FriendReceptionViewModel
    private lateinit var viewModelFactory: FriendReceptionViewModelFactory

    private var friendCode : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_reception, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG,"onViewCreated")

        arguments?.takeIf { it.containsKey(KEY)}?.apply{
            friendCode=getLong(KEY)
        }

        Log.i(TAG,"friendCode : ${friendCode}")
        viewModelFactory = FriendReceptionViewModelFactory(friendCode ?: -1L)
        viewModel = ViewModelProvider(this,viewModelFactory)[FriendReceptionViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.clickAction.observe(viewLifecycleOwner) {
            viewModel.deleteReceptionList()

            val friend = viewModel.friend.value ?: return@observe
            val friendCode = convertHexStringToLongFormat(friend.friendCode)

            if(it) {
                viewModel.setValueMyFriendList(friend)
                viewModel.setValueTmpFrientList(friendCode,friend)
                viewModel.setValueDispatchList(friendCode,friend,DispatchStatus.ACCEPT)
            } else {
                viewModel.setValueDispatchList(friendCode,friend,DispatchStatus.DECLINE)
            }
        }


        val adapter = FriendListAdatper(receptionClickListener = OnReceptionClickListener{ friend,flag ->
            if(flag) {
                viewModel.onAccept(friend)
            } else {
                viewModel.onDecline(friend)
            }
        })

        viewModel.profilesDistinct.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.recFriendReception.adapter = adapter
    }

    companion object {
        const val TAG = "프렌드_리셉션_프래그먼트"
        const val KEY = "ReceptionKey"
    }
}