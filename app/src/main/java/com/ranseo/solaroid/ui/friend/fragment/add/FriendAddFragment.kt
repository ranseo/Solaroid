package com.ranseo.solaroid.ui.friend.fragment.add

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.tabs.TabLayout
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentFriendAddBinding
import com.ranseo.solaroid.dialog.NormalDialogFragment
import com.ranseo.solaroid.ui.friend.adapter.FriendAddAdapter
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.FriendDispatchFragment
import com.ranseo.solaroid.ui.friend.fragment.add.reception.FriendReceptionFragment
import com.ranseo.solaroid.room.SolaroidDatabase
import com.google.android.material.tabs.TabLayoutMediator
import com.ranseo.solaroid.convertHexStringToLongFormat

class FriendAddFragment : Fragment(), NormalDialogFragment.NormalDialogListener {
    private lateinit var binding: FragmentFriendAddBinding

    private lateinit var viewModelFactory: FriendAddViewModelFactory
    private lateinit var viewModel: FriendAddViewModel

    @RequiresApi(Build.VERSION_CODES.M)
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
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendAddViewModel::class.java]

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

        viewModel.myFriendCode.observe(viewLifecycleOwner) {
            it?.let { friendCode ->
                viewModel.refreshDispatchFriendSize(friendCode)
                viewModel.refreshReceptionFriendSize(friendCode)
            }
        }

        viewModel.friendRequest.observe(viewLifecycleOwner) { request ->
            request.getContentIfNotHandled()?.let {
                showDialog()
            }
        }

        viewModel.dispatchFriendSize.observe(viewLifecycleOwner) {
            it?.let { size ->
                if (size > 0) setBadgeOnBottomNavigationView(size,0)
            }
        }

        viewModel.receptionFriendSize.observe(viewLifecycleOwner) {
            it?.let { size ->
                if (size > 0) setBadgeOnBottomNavigationView(size,1)
            }
        }

        return binding.root
    }


    private fun showDialog() {
        val newFragment = NormalDialogFragment(this, "친구 요청을 하시겠습니까?", "요청", "아니요")
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
            tab.text = if (pos == 0) "발신" else "수신"
        }.attach()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UnsafeOptInUsageError")
    private fun setBadgeOnBottomNavigationView(cnt: Int, idx: Int) {
        val tab = binding.tablayoutFriendAdd.getTabAt(idx)
        val itemView = tab?.view

        val context = requireContext()

        BadgeDrawable.create(context).apply {
            number = cnt
            backgroundColor = ContextCompat.getColor(context, R.color.alert_color)
            badgeTextColor = ContextCompat.getColor(context, R.color.white)
            verticalOffset = 45
            if (cnt < 10) {
                horizontalOffset = 45
            } else if (cnt < 100) {
                horizontalOffset = 60
            } else {
                horizontalOffset = 75
            }
        }.let { badge ->
            itemView?.let {
                itemView.foreground = badge
                itemView.addOnLayoutChangeListener { view, i, i2, i3, i4, i5, i6, i7, i8 ->
                    BadgeUtils.attachBadgeDrawable(badge, view)
                }
            }
        }
    }

    override fun onDetach() {
//        val activity = this.requireActivity() as FriendActivity
//        activity.setActionBarTitle("")
        super.onDetach()
        viewModel.removeListener()

    }

    companion object {
        const val TAG = "프렌드_애드_프래그먼트"
    }

}