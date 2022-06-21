package com.example.solaroid.repositery.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.room.DatabaseProfile
import com.example.solaroid.room.asDomainModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileRepostiery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val fbStorage: FirebaseStorage,
    private val database: DatabasePhotoTicketDao,
) {


    val user = fbAuth.currentUser?.email ?: "UNKNOWN"
    val myProfile: LiveData<Profile> = Transformations.map(database.getMyProfileInfo(user)) {
        it?.let { it.asDomainModel() }
    }


    suspend fun insertProfileInfo(profile: FirebaseProfile, application: Application) {
        val user = fbAuth.currentUser ?: return

        withContext(Dispatchers.IO) {

            val profileRef = fbDatabase.reference.child("profile").child(user.uid)

            profileRef.setValue(
                profile,
                DatabaseReference.CompletionListener { error: DatabaseError?, _: DatabaseReference ->
                    if (error != null) {
                        Log.d(TAG, "Unable to Write Message to Database", error.toException())
                        return@CompletionListener
                    }

                    val file = profile.profileImg.toUri()
                    val mimeType: String? = application.contentResolver.getType(file)

                    Log.i(TAG, "file : ${file}")

                    val storageRef = fbStorage.getReference("profile")
                        .child(user.uid)
                        .child("${mimeType}/${file.lastPathSegment}")

                    insertProfileInStorage(storageRef, profile, file, user)

                })
        }
    }

    private fun insertProfileInStorage(
        storageRef: StorageReference,
        profile: FirebaseProfile,
        file: Uri,
        user: FirebaseUser
    ) {

        val metadata = storageMetadata {
            contentType = "image/jpeg"
        }

        storageRef.putFile(file, metadata).addOnSuccessListener { taskSnapShot ->
            taskSnapShot.metadata!!.reference!!.downloadUrl
                .addOnSuccessListener { url ->
                    val new = FirebaseProfile(
                        id = profile.id,
                        nickname = profile.nickname,
                        profileImg = url.toString(),
                        friendCode = profile.friendCode
                    )

                    fbDatabase.reference.child("profile")
                        .child(user.uid)
                        .setValue(new)

                    Log.i(TAG,"profileListener.insertRoomDatabase(new.asDatabaseModel())")
                }
        }
    }


    /**
     * LoginFragment에서 첫 로그인을 할 때, 로그인이 되어있지 상태이기 때문에 fbAuth는 current user를 불러올 수 없다. 반드시 ProfileFragment로 이동
     * 따라서 LoginFragment, 로그인에 성공하여 fbAuth에 currentUser가 설정될때 해당 아이디로 firebase realtime database / profile / user.uid .get()
     * 해당 경로에 Profile data가 있는지 확인할 수 있는 task를 반환하는 함수.
     * */
    suspend fun isProfile(): Task<DataSnapshot>? {
        return withContext(Dispatchers.IO) {
            val uid = fbAuth.currentUser!!.uid
            run {
                val profileRef = fbDatabase.reference.child("profile").child(uid)
                val task = profileRef.get()

                task
            }
        }
    }

    suspend fun getProfileInfo(): Task<DataSnapshot>? {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser
            if (user == null) null
            else {
                val profileRef: Task<DataSnapshot>? =
                    fbDatabase.reference.child("profile").child(user!!.uid).get()


                profileRef

            }
        }
    }


    companion object {
        const val TAG = "프로필 리포지터리"
    }


}