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


    suspend fun isInitProfile(): Task<DataSnapshot>? {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser
            if (user == null) null
            else {
                val profileRef = fbDatabase.reference.child("profile").child(user.uid)
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