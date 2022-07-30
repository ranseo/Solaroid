package com.example.solaroid.ui.friend.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.R
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.databinding.ListItemFriendDispatchBinding
import com.example.solaroid.databinding.ListItemFriendPartyListBinding
import com.example.solaroid.databinding.ListItemFriendReceptionBinding
import com.example.solaroid.databinding.ListItemSolaroidFriendBinding
import com.example.solaroid.ui.friend.fragment.add.dispatch.DispatchFriend
import com.example.solaroid.ui.friend.fragment.add.dispatch.DispatchStatus
import com.example.solaroid.ui.friend.fragment.add.reception.ReceptionFriend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.ClassCastException

private val VIEW_TYPE_NORMAL_PROFILE = 0
private val VIEW_TYPE_RECEPTION_PROFILE = 1
private val VIEW_TYPE_DISPATCH_PROFILE = 2
private val VIEW_TYPE_DIALOG_PROFILE = 3

class FriendListAdatper(
    val application: Application? = null,
    val normalClickListener: OnNormalClickListener,
    val receptionClickListener: OnReceptionClickListener? = null,
    val dispatchClickListener: OnDispatchClickListener? = null,
    val dialogClickListener: OnDialogClickListener? = null
) : ListAdapter<FriendListDataItem, RecyclerView.ViewHolder>(FriendListDataItemCallback()) {
    val adapterScope = CoroutineScope(Dispatchers.Default)


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FriendListDataItem.NormalProfileDataItem -> VIEW_TYPE_NORMAL_PROFILE
            is FriendListDataItem.ReceptionProfileDataItem -> VIEW_TYPE_RECEPTION_PROFILE
            is FriendListDataItem.DispatchProfileDataItem -> VIEW_TYPE_DISPATCH_PROFILE
            is FriendListDataItem.DialogProfileDataItem -> VIEW_TYPE_DIALOG_PROFILE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL_PROFILE -> FriendListViewHolder.from(parent)
            VIEW_TYPE_RECEPTION_PROFILE -> FriendReceptionViewHolder.from(parent)
            VIEW_TYPE_DISPATCH_PROFILE -> FriendDispatchViewHolder.from(parent)
            VIEW_TYPE_DIALOG_PROFILE -> FriendDialogViewHolder.from(parent)
            else -> throw ClassCastException("UNKNOWN_VIEWTYPE_${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FriendListViewHolder -> {
                val item = getItem(position) as FriendListDataItem.NormalProfileDataItem
                holder.bind(item.friend, normalClickListener)
            }
            is FriendReceptionViewHolder -> {
                val item = getItem(position) as FriendListDataItem.ReceptionProfileDataItem
                holder.bind(item.friend, receptionClickListener!!)
            }
            is FriendDispatchViewHolder -> {
                val item = getItem(position) as FriendListDataItem.DispatchProfileDataItem
                holder.bind(item.friend, dispatchClickListener!!)
            }
            is FriendDialogViewHolder -> {
                val item = getItem(position) as FriendListDataItem.DialogProfileDataItem
                holder.bind(item.friend, dialogClickListener!!, application)
            }
        }
    }

    class FriendListViewHolder(private val binding: ListItemSolaroidFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Friend, listener: OnNormalClickListener) {
            binding.friend = item
            binding.onClickListener =  listener
        }

        companion object {
            fun from(parent: ViewGroup): FriendListViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidFriendBinding.inflate(layoutInflater, parent, false)
                return FriendListViewHolder(binding)
            }
        }
    }

    class FriendReceptionViewHolder(private val binding: ListItemFriendReceptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReceptionFriend, onClickListener: OnReceptionClickListener) {
            binding.friend = item.friend
            binding.onClickListener = onClickListener
        }

        companion object {
            fun from(parent: ViewGroup): FriendReceptionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemFriendReceptionBinding.inflate(layoutInflater, parent, false)
                return FriendReceptionViewHolder(binding)
            }
        }
    }

    class FriendDispatchViewHolder(private val binding: ListItemFriendDispatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DispatchFriend, onClickListener: OnDispatchClickListener) {
            binding.friend = item
            binding.onClickListener = onClickListener
            val statusMsg = when (item.flag) {
                DispatchStatus.UNKNOWN -> {
                    UNKNOWN
                }
                DispatchStatus.DECLINE -> {
                    DECLINE
                }
                DispatchStatus.ACCEPT -> {
                    ACCEPT
                }
            }
            val status = when (item.flag) {
                DispatchStatus.UNKNOWN -> {
                    false
                }
                DispatchStatus.DECLINE -> {
                    true
                }
                DispatchStatus.ACCEPT -> {
                    true
                }
            }

            binding.statusMsg = statusMsg
            binding.status = status

        }

        companion object {
            fun from(parent: ViewGroup): FriendDispatchViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemFriendDispatchBinding.inflate(layoutInflater, parent, false)
                return FriendDispatchViewHolder(binding)
            }

            const val UNKNOWN = "아직 상대가 요청을 확인하지 않았습니다."
            const val DECLINE = "상대가 요청을 거절하였습니다."
            const val ACCEPT = "상대가 요청을 수락하였습니다."

        }
    }


    class FriendDialogViewHolder(private val binding: ListItemFriendPartyListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Friend, onClickListener: OnDialogClickListener, application: Application?) {
            binding.friend = item
            binding.flag = false
            val lambda : (view: View) -> Unit = {
                try {
                    binding.flag = !binding.flag
                    if (binding.flag) {
                        binding.layoutListItem.background =
                            application!!.getDrawable(R.drawable.border_line_yellow)
                    } else {
                        binding.layoutListItem.background =
                            application!!.getDrawable(R.drawable.border_line_grey)
                    }
                    onClickListener.listener(item)
                } catch (error:Exception){

                }
            }
            binding.layoutListItem.setOnClickListener(lambda)
            binding.btnCheck.setOnClickListener(lambda)
            binding.btnEmpty.setOnClickListener(lambda)
        }

        companion object {
            fun from(parent: ViewGroup): FriendDialogViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemFriendPartyListBinding.inflate(layoutInflater, parent, false)
                return FriendDialogViewHolder(binding)
            }
        }
    }
}

class FriendListDataItemCallback() : DiffUtil.ItemCallback<FriendListDataItem>() {
    override fun areItemsTheSame(
        oldItem: FriendListDataItem,
        newItem: FriendListDataItem
    ): Boolean = oldItem.id == newItem.id


    override fun areContentsTheSame(
        oldItem: FriendListDataItem,
        newItem: FriendListDataItem
    ): Boolean = oldItem == newItem
}


sealed class FriendListDataItem() {
    abstract val id: String

    class NormalProfileDataItem(val friend: Friend) : FriendListDataItem() {
        override val id: String
            get() = friend.id
    }

    class ReceptionProfileDataItem(val friend: ReceptionFriend) : FriendListDataItem() {
        override val id: String
            get() = friend.friend.id
    }

    class DispatchProfileDataItem(val friend: DispatchFriend) : FriendListDataItem() {
        override val id: String
            get() = friend.id
    }

    class DialogProfileDataItem(val friend: Friend) : FriendListDataItem() {
        override val id: String
            get() = friend.id
    }
}

class OnNormalClickListener(val listener : (friend:Friend)->Unit) {
    fun onLongClick(friend:Friend) {
        listener(friend)
    }
}

class OnReceptionClickListener(val listener: (friend: Friend, flag: Boolean) -> Unit) {
    fun onPositiveClick(friend: Friend) {
        listener(friend, true)
    }

    fun onNegativeClick(friend: Friend) {
        listener(friend, false)
    }
}

class OnDispatchClickListener(val listener: (dispatchFriend: DispatchFriend) -> Unit) {
    fun onClick(dispatchFriend: DispatchFriend) {
        listener(dispatchFriend)
    }
}

class OnDialogClickListener(val listener: (dialogFriend: Friend) -> Unit) {
    fun onClick(dialogFriend: Friend) {
        listener(dialogFriend)
    }
}
