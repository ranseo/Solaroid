package com.ranseo.solaroid.ui.friend.fragment.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.R
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.ranseo.solaroid.room.SolaroidDatabase
import com.ranseo.solaroid.databinding.FragmentFriendListBinding
import com.ranseo.solaroid.dialog.ListSetDialogFragment
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.ui.album.viewmodel.ClickTag
import com.ranseo.solaroid.ui.friend.adapter.FriendListAdatper
import com.ranseo.solaroid.ui.friend.adapter.OnNormalClickListener

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

        val longClickListener : (friend: Friend) -> Unit = { friend ->
            viewModel.onLongClick(friend, ClickTag.LONG)
        }

        val adapter = FriendListAdatper(normalClickListener= OnNormalClickListener(longClickListener))

        binding.recFriendList.adapter = adapter


        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let{ profile->
                val friendCode = convertHexStringToLongFormat(profile.friendCode)
                viewModel.initRefreshFriendList(friendCode)
            }
        }

        viewModel.longClick.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { ft->
                viewModel.setCurrFriend(ft)
            }
        }

        viewModel.currFriend.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { ft->
                when(ft.second) {
                    ClickTag.CLICK -> {

                    }
                    ClickTag.LONG -> {
                        showDialog()
                    }
                }
            }
        }

        viewModel.tmpFriend.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{ friend ->
                viewModel.deleteTmpList(friend)
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
        val friend = viewModel.currFriend.value?.peekContent()?.first ?: return

        when(position) {
            0 -> {
                viewModel.deleteFriend(friend)
            }
            1 -> {

            }
            else -> {

            }
        }
    }


}