package com.example.solaroid.ui.friend.fragment.add

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.databinding.FragmentFriendAddBinding
import com.example.solaroid.dialog.NormalDialogFragment
import com.example.solaroid.ui.friend.activity.FriendActivity
import com.example.solaroid.ui.friend.adapter.FriendAddAdapter
import com.example.solaroid.ui.friend.fragment.add.dispatch.FriendDispatchFragment
import com.example.solaroid.ui.friend.fragment.add.reception.FriendReceptionFragment
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.tabs.TabLayoutMediator

class FriendAddFragment : Fragment(), NormalDialogFragment.NormalDialogListener {
    private lateinit var binding: FragmentFriendAddBinding

    private lateinit var viewModelFactory: FriendAddViewModelFactory
    private lateinit var viewModel: FriendAddViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_add, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao
        viewModelFactory = FriendAddViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this,viewModelFactory)[FriendAddViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = FriendAddAdapter(childFragmentManager, lifecycle)
        binding.viewPageFriendAdd.adapter = adapter




        viewModel.myProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
//                val activity = this.requireActivity() as FriendActivity
//                activity.setActionBarTitle("나의 친구코드 : ${profile.friendCode}")

                adapter.addDispatchFragment(FriendDispatchFragment(), profile)
                adapter.addReceptionFragment(FriendReceptionFragment(), profile)
                setTabLayout()

            }
        }

        viewModel.friendRequest.observe(viewLifecycleOwner) { request ->
            request.getContentIfNotHandled()?.let{
                showDialog()
            }

        }

        return binding.root
    }


    private fun showDialog() {
        val newFragment = NormalDialogFragment(this,"친구 요청을 하시겠습니까?", "요청", "아니요")
        newFragment.show(parentFragmentManager, "NormalDialog")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        viewModel.setValueFriendDispatch()
        viewModel.setValueFriendReception()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }


    private fun setTabLayout() {
        TabLayoutMediator(binding.tablayoutFriendAdd, binding.viewPageFriendAdd) { tab, pos ->
            tab.text = if (pos == 0)  "발신" else "수신"
        }.attach()
    }

    override fun onDetach() {
//        val activity = this.requireActivity() as FriendActivity
//        activity.setActionBarTitle("")
        super.onDetach()
    }

    companion object {
        const val TAG = "프렌드_애드_프래그먼트"
    }

}