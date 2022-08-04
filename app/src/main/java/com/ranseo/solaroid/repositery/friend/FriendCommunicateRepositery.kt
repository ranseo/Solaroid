package com.ranseo.solaroid.repositery.friend

import android.util.Log
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.models.domain.asFirebaseModel
import com.ranseo.solaroid.models.domain.asFriend
import com.ranseo.solaroid.datasource.friend.FriendCommunicationDataSource
import com.ranseo.solaroid.firebase.FirebaseFriend
import com.ranseo.solaroid.firebase.asFirebaseDispatchFriend
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchFriend
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendCommunicateRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val friendCommunicationDataSource: FriendCommunicationDataSource,
) {

    /**
     * DispatchFragment에서 Firebase/DispatchList 에 있는 Friend객체를 읽기 위해 해당 경로에 ValueEventListener를 추가하는 함수.
     * */
    suspend fun addValueListenerToDisptachRef(friendCode: Long, setFriends: (friends:List<DispatchFriend>)->Unit) {
        return withContext(Dispatchers.IO) {
            try {
                val listener = friendCommunicationDataSource.getDispatchValueEventListener(setFriends)
                fbDatabase.reference.child("friendDispatch").child("$friendCode").child("list")
                    .addValueEventListener(listener)
            } catch (e: Exception) {
                Log.i(TAG, "addValueListenerToDisptachRef error : ${e.message}")
            }
        }
    }

    /**
     * ReceptionFragment에서 Firebase/ReceptionList 에 있는 Friend객체를 읽기 위해 해당 경로에 ValueEventListnener를 추가하는 함수.
     * */
    suspend fun addValueListenerToReceptionRef(friendCode: Long, setFriends: (friends:List<Friend>)->Unit ) {
        return withContext(Dispatchers.IO) {
            try {
                val listener = friendCommunicationDataSource.getReceptionValueEventListener(setFriends)
                fbDatabase.reference.child("friendReception").child("$friendCode").child("list")
                    .addValueEventListener(listener)
            } catch (e: Exception) {
                Log.i(TAG, "addValueListenerToReceptionRef error : ${e.message}")
            }
        }
    }


    /**
     * receptionFragment에서 친구추가 또는 거절 이후 Firebase의 ReceptionList에서 해당  friend 객체 삭제.
     * */
    suspend fun deleteReceptionList(friendCode: Long, key: Long) {
        return withContext(Dispatchers.IO) {
            fbDatabase.reference.child("friendReception").child("${friendCode}").child("list")
                .child("${key}").removeValue()
        }

    }


    /**
     * dispatchFragment에서 제거버튼을 누르면 Firebase의 dispatchList 해당  friend 객체 삭제.
     * */
    suspend fun deleteFriendInDispatchList(friendCode: Long, key: Long) {
        return withContext(Dispatchers.IO) {
            fbDatabase.reference.child("friendDispatch").child("${friendCode}").child("list")
                .child("${key}").removeValue()
        }
    }

    /**
     * receptionFragment 또는 dispatchFragment에서 Friend객체를 Firebase/FriendList에 추가하는 함수
     * */
    suspend fun setValueMyFriendList(_friend: Friend) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser ?: return@withContext

            val ref = fbDatabase.reference.child("myFriendList").child(user.uid).child("list").child("${convertHexStringToLongFormat(_friend.friendCode)}")
            val friend = FirebaseFriend(
                _friend.id,
                _friend.nickname,
                _friend.profileImg,
                convertHexStringToLongFormat(_friend.friendCode),
                "key"
            )

            ref.setValue(friend)

        }
    }

    /**
     * receptionFragment에서 친구추가가 되었음을 상대에게도 알리기 위해 Firebase/tmpFriendList 경로에 Friend객체를 쓰는 함수.
     * */
    suspend fun setValueTmpList(friendCode:Long, myProfile: Profile) {
        return withContext(Dispatchers.IO) {

            val ref = fbDatabase.reference.child("tmpFriendList").child("${friendCode}").child("list").child("${convertHexStringToLongFormat(myProfile.friendCode)}")

            val friend = myProfile.asFriend("key").asFirebaseModel()
            ref.setValue(friend)

        }
    }


    /**
     * receptionFragment에서 친구추가 또는 거절을 했을 때 상대의 DispatchFragment에 수신여부를 전달하기 위해
     * Firebase/DispatchList 에 새로운 DispatchFragment 객체를 쓰는 함수.
     * */
    suspend fun setValueFriendDispatch(friendCode: Long, myProfile: Profile, flag: DispatchStatus, myFriendCode:Long) {
        return withContext(Dispatchers.IO) {

            val ref = fbDatabase.reference.child("friendDispatch").child("${friendCode}").child("list").child("${myFriendCode}")
            val key=ref.key ?:return@withContext

            val friend = myProfile.asFriend(key).asFirebaseModel().asFirebaseDispatchFriend(flag.status)

            ref.setValue(friend)

        }
    }


    companion object {
        const val TAG = "프렌드_커뮤니케이션_리포지터리"
    }
}