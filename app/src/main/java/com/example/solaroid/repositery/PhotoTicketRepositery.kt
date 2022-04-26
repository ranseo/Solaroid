package com.example.solaroid.repositery

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.database.DatabasePhotoTicket
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.database.asDomainModel
import com.example.solaroid.database.asFirebaseModel
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.domain.asDatabaseModel
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicketContainer
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.firebase.setPhotoTicketList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.*
import java.net.URL

class PhotoTicketRepositery(
    private val dataSource: DatabasePhotoTicketDao,
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val fbStorage: FirebaseStorage
) {

    /**
     * 포토티켓의 최신순 정렬.
     * */
    val photoTicketsOrderByLately: LiveData<List<PhotoTicket>> =
        Transformations.map(dataSource.getAllDatabasePhotoTicket()) {
            it?.let {
                it.asDomainModel()
            }
        }

    /**
     * 포토티켓의 즐겨찾기 상태가 설정된 것들로만 뽑아온 리스트.
     * Transformations.map() 에 photoTickets 를 매개변수로 넣어 만든다.
     * */
    val photoTicketsOrderByFavorte: LiveData<List<PhotoTicket>> =
        Transformations.map(photoTicketsOrderByLately) {
            it?.let { photoTicket ->
                photoTicket.filter { it.favorite }
            }
        }


    /**
     * 어플리케이션을 처음 실행할 때 또는 UI를 전환할 때(프레임컨테이너 <-> 갤러리프래그먼트) 포토티켓 리스트를
     * 화면에 띄우기 위해 firebase의 실시간 데이버테이스로 부터 FirebasePhotoTicket을 불러오고 이를 다시 room database에 insert하는
     * ValueEventListener를 등록하여 refresh하는 함수.
     * */
    suspend fun refreshPhotoTickets() {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val ref = fbDatabase.reference.child("photoTicket").child(user.uid)
            ref.addListenerForSingleValueEvent(setPhotoTicketList() {
                launch(Dispatchers.IO) { dataSource.insert(it.asDatabaseModel()) }
            })
        }
    }

    /**
     * 포토티켓의 업데이트 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 업데이트할 포토티켓(Domaain Model)을 매개변수로 전달 받아 RoomDatabase와 Firebase의 실시간데이터베이스 및 storaage(url업데이트 시) 내에
     * 포토티켓 정보를 업데이트 한다.
     * */
    suspend fun updatePhotoTickets(photoTicket: PhotoTicket) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val key = dataSource.getDatabasePhotoTicket(photoTicket.id).firebaseKey
            val new = photoTicket.asDatabaseModel(key)

            //room update
            dataSource.update(new)

            //firebase database update
            val ref = fbDatabase.reference.child(user.uid).child("photoTicket").child(key)
            ref.setValue(new.asFirebaseModel())


        }
    }

//    suspend fun updatePhotoTicketForUrl(application: Application, user: FirebaseUser, fbDatabase: FirebaseDatabase, fbStorage: FirebaseStorage, photoTicket:PhotoTicket, url: URL) {
//            val file = new.url.toUri()
//            val mimeType : String? = application.contentResolver.getType(file)
//            val storageRef = fbStorage.reference.child(user.uid).child("photoTicket").child(key)
//                .child("${mimeType?.split("/")?.get(0)}/${file.lastPathSegment}")
//
//            storageRef.putFile(new.url)
//        }
//    }


    /**
     * 포토티켓의 삭제 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 삭제할 포토티켓을 매개변수로 전달 받아 RoomDatabase와 Firebase 실시간 데이터 베이스 및 Storage 내에
     * 포토티켓 정보를 삭제한다.
     * */
    suspend fun deletePhotoTickets(key: Long) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val del = dataSource.getDatabasePhotoTicket(key)

            //room delete
            dataSource.delete(del.id)

            //firebase database delete
            val ref =
                fbDatabase.reference.child(user.uid).child("photoTicket").child(del.firebaseKey)
            ref.removeValue()

            //firebase storage delete
            val storageRef =
                fbStorage.reference.child(user.uid).child("photoTicket").child(del.firebaseKey)
            storageRef.delete()

        }
    }


    /**
     * 포토티켓의 삽입 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 삽입할 포토티켓을 매개변수로 전달 받아 RoomDatabase 및 Realtime database
     * 내에 포토티켓 정보를 삽입한다. Firebase Storage 내에는 파일 (URL) 정보를 입력한다.
     * */
    suspend fun insertPhotoTickets(photoTicket: PhotoTicket, application: Application) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val new = photoTicket.asDatabaseModel("").asFirebaseModel()

            fbDatabase.reference.child("photoTicket").child(user.uid).push().setValue(new,
                DatabaseReference.CompletionListener { error: DatabaseError?, ref: DatabaseReference ->
                    if (error != null) {
                        Log.d(TAG, "Unable to write Message to database", error.toException())
                        return@CompletionListener
                    }

                    launch(Dispatchers.IO) {

                        val file = photoTicket.url.toUri()
                        val mimeType: String? = application.contentResolver.getType(file)

                        val key = ref.key
                        val storageRef = fbStorage.getReference("photoTicket")
                            .child(user.uid)
                            .child(key!!)
                            .child("${mimeType?.split("/")?.get(0)}/${file.lastPathSegment}")

                        insertImageInStorage(storageRef, user, file, key, new)

                    }

                })
        }
    }

    private suspend fun insertImageInStorage(
        storageRef: StorageReference,
        user: FirebaseUser,
        file: Uri,
        key: String,
        pre: FirebasePhotoTicket
    ) {
        //metadata 지정
        var metadata = storageMetadata {
            contentType = "image/jpeg"
        }

        withContext(Dispatchers.IO) {
            storageRef.putFile(file, metadata)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { url ->
                            launch(Dispatchers.IO) {
                                val new = FirebasePhotoTicket(
                                    key = key,
                                    url = url.toString(),
                                    frontText = pre.frontText,
                                    backText = pre.backText,
                                    date = pre.date,
                                    favorite = pre.favorite
                                )

                                fbDatabase.reference.child("photoTicket")
                                    .child(user.uid)
                                    .child(key)
                                    .setValue(new)

                                dataSource.insert(new.asDatabaseModel())
                            }
                        }

                        .addOnFailureListener { error ->
                            Log.d(TAG, "taskSnapShot error ${error.message}")
                        }
                }
                .addOnFailureListener { error ->
                    Log.d(TAG, "sotrageRef.putfile error ${error.message}")
                }

        }

    }

    companion object {
        const val TAG = "포토티켓리포지터리"
    }


}