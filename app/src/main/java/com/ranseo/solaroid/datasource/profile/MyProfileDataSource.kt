package com.ranseo.solaroid.datasource.profile


import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.models.firebase.FirebaseProfile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MyProfileDataSource() {

    suspend fun getMyProfileListener(
        insertRoomDb: suspend (profile: Profile) -> Unit
    )  {

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(FirebaseProfile::class.java)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }


    }


    companion object {
        const private val TAG = "마이프로필_데이터소스"
    }

}
