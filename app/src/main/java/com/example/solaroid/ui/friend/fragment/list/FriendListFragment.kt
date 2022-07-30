package com.example.solaroid.ui.friend.fragment.list

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.databinding.FragmentFriendListBinding
import com.example.solaroid.dialog.ListSetDialogFragment
import com.example.solaroid.ui.friend.adapter.FriendListAdatper
import com.example.solaroid.ui.friend.adapter.OnNormalClickListener

class FriendListFragment : Fragment(), ListSetDialogFragment.ListSetDialogListener {
    private lateinit var binding: FragmentFriendListBinding
    private lateinit var viewModel: FriendListViewModel
    private lateinit var viewModelFactory : FriendListViewModelFactory

    private lateinit var dataSource : DatabasePhotoTicketDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_list, container, false)


        val application = requireNotNull(this.activity).application
        dataSource = SolaroidDatabase.getInstance(application).photoTicketDao

        viewModelFactory = FriendListViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendListViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = FriendListAdatper(normalClickListener = OnNormalClickListener{ friend ->

        })

        binding.recFriendList.adapter = adapter


        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let{ profile->
                val friendCode = convertHexStringToLongFormat(profile.friendCode)
                viewModel.initRefreshFriendList(friendCode)
            }
        }
        return binding.root
    }

    fun showDialog() {
        val new = ListSetDialogFragment(R.array.friend_long_click_dialog_items,this )
        new.show(parentFragmentManager, "FriendDialog")

    }

    companion object {
        const val TAG = "프렌드_리스트_프래그먼트"
    }

    override fun onDialogListItem(dialog: DialogFragment, position: Int) {

    }


}