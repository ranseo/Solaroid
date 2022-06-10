package com.example.solaroid.friend.fragment.add.dispatch

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.friend.fragment.add.FriendAddViewModel
import com.example.solaroid.repositery.FriendCommunicateRepositery
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class FriendDispatchViewModel : ViewModel() {

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendCommunicateRepositery = FriendCommunicateRepositery(fbAuth, fbDatabase)

    private val _profiles = MutableLiveData<List<Profile>>(listOf())
    val profiles: LiveData<List<Profile>>
        get() = _profiles

    val profilesDistinct = Transformations.map(profiles) {
        it.distinct()
    }


    init {
        refreshDispatchProfiles()
    }

    private fun refreshDispatchProfiles() {
        viewModelScope.launch {

            val valueListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val hashMap = snapshot.value as HashMap<*, *>

                        val profile = FirebaseProfile(
                            hashMap["id"]!! as String,
                            hashMap["nickname"]!! as String,
                            hashMap["profileImg"]!! as String,
                            hashMap["friendCode"]!! as Long
                        ).asDomainModel()

                        _profiles.value = _profiles.value?.plus(listOf(profile))

                    } catch (e: Exception) {
                        Log.i(TAG, "valueEventListener onDataChange error : ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "valueEventListener onCancelled error : ${error.message}")
                }

            }
            friendCommunicateRepositery.addValueListenerToDisptachRef(valueListener)
        }
    }

    companion object {
        const val TAG = "프렌드_디스패치_뷰모델"
    }
}
