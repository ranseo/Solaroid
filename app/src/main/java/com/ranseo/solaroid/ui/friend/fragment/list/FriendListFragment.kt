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
import com.ranseo.solaroid.ui.friend.adapter.FriendListDataItem
import com.ranseo.solaroid.ui.friend.adapter.OnNormalClickListener

class FriendListFragment : Fragment(), ListSetDialogFragment.ListSetDialogListener {

    companion object {
        const val TAG = "프렌드_리스트_프래그먼트"
        const val FRIEND_EMPTY_TEXT = "추가된 친구가 존재하지 않습니다.\n'친구' - '친구 요청' 을 이용해\n친구를 만들고 사진을 공유해보세요"
    }
    
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

        viewModel.friendList.observe(viewLifecycleOwner) { list ->
            var adapterList : List<FriendListDataItem> = listOf()
            adapterList = if(!list.isNullOrEmpty()) {
                list.map { v-> FriendListDataItem.NormalProfileDataItem(v)}
            } else {
                listOf(FriendListDataItem.FriendEmptyHead().apply { title = FRIEND_EMPTY_TEXT })
            }

            adapter.submitList(adapterList)
        }

        TODO("이 부분 때문에 현재 검색창 부분에 검색을 해도 제대로 작동이 안됨 (단, X 눌렀을때 작동은 잘됨) ")
        viewModel.isSearch.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{ search ->
                if(!search) binding.etSearch.text.clear()
            }
        }
        return binding.root
    }

    fun showDialog() {
        val new = ListSetDialogFragment(R.array.friend_long_click_dialog_items,this )
        new.show(parentFragmentManager, "FriendDialog")

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