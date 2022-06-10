package com.example.solaroid.friend.fragment.add.reception

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.friend.fragment.add.FriendAddViewModel
import com.example.solaroid.repositery.FriendCommunicateRepositery
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class FriendReceptionViewModel(_friendCode: Long) : ViewModel() {

    private val friendCode = _friendCode

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendCommunicateRepositery = FriendCommunicateRepositery(fbAuth, fbDatabase)

    private val _profiles = MutableLiveData<List<Profile>>(listOf())
    val profiles : LiveData<List<Profile>>
        get() = _profiles

    val profilesDistinct = Transformations.map(profiles){
        it.distinct()
    }


    init {
        refreshReceptionProfiles()
    }

    private fun refreshReceptionProfiles() {
        viewModelScope.launch {

            val valueListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val hashMap = snapshot.value as HashMap<*,*>

                    try {
                        val profile = FirebaseProfile(
                            hashMap["id"]!! as String,
                            hashMap["nickname"]!! as String,
                            hashMap["profileImg"]!! as String,
                            hashMap["friendCode"]!! as Long
                        ).asDomainModel()


                        _profiles.value = _profiles.value?.plus(listOf(profile))

                    } catch (e:Exception){
                        Log.i(TAG, "ChildEventListener  onChildAdded error : ${e.message}")
                    }


                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val hashMap = snapshot.value as HashMap<*,*>

                    try {
                        val profile = FirebaseProfile(
                            hashMap["id"]!! as String,
                            hashMap["nickname"]!! as String,
                            hashMap["profileImg"]!! as String,
                            hashMap["friendCode"]!! as Long
                        ).asDomainModel()


                        _profiles.value = _profiles.value?.filter {
                            it != profile
                        }
                    } catch (e:Exception){
                        Log.i(TAG, "ChildEventListener  onChildAdded error : ${e.message}")
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            friendCommunicateRepositery.addValueListenerToReceptionRef(friendCode,valueListener)
        }
    }

    companion object {
        const val TAG = "프렌드_리셉션_뷰모델"
    }
}
