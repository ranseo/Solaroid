package com.example.solaroid.repositery

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.database.asDomainModel
import com.example.solaroid.database.asFirebaseModel
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.domain.asDatabaseModel
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.firebase.setPhotoTicketList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import java.lang.Exception

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
            it?.asDomainModel()
        }

    /**
     * 포토티켓의 즐겨찾기 상태가 설정된 것들로만 뽑아온 리스트.
     * Transformations.map() 에 photoTickets 를 매개변수로 넣어 만든다.
     * */
    val photoTicketsOrderByFavorte: LiveData<List<PhotoTicket>> =
        Transformations.map(photoTicketsOrderByLately) {
            it?.let { photoTicket ->
                photoTicket.filter { ticket -> ticket.favorite }
            }
        }


    /**
     * 어플리케이션을 처음 실행할 때 또는 UI를 전환할 때(프레임컨테이너 <-> 갤러리프래그먼트) 포토티켓 리스트를
     * 화면에 띄우기 위해 firebase의 실시간 데이버테이스로 부터 FirebasePhotoTicket을 불러오고 이를 다시 room database에 insert하는
     * ValueEventListener를 등록하여 refresh하는 함수.
     * */
    suspend fun refreshPhotoTickets(application: Application) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val ref = fbDatabase.reference.child("photoTicket").child(user.uid)
            if (ref == fbDatabase.reference) {
                Log.d(TAG, "Network Error")
                Toast.makeText(application, "네트워크 연결이 동기화 되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@withContext
            }
            val listener = setPhotoTicketList() {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.i(TAG, "photoTicket : ${it}")
                    dataSource.insert(it.asDatabaseModel())
                }
            }
            ref.addListenerForSingleValueEvent(listener)
            Log.i(TAG, "refreshPhotoTicket() : ValueEventListener 등록.")


        }
    }

    /**
     * 포토티켓의 업데이트 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 업데이트할 포토티켓(Domaain Model)을 매개변수로 전달 받아 RoomDatabase와 Firebase의 실시간데이터베이스 및 storaage(url업데이트 시) 내에
     * 포토티켓 정보를 업데이트 한다.
     * */
    suspend fun updatePhotoTickets(photoTicket: PhotoTicket, application: Application) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val key = photoTicket.id
            val new = photoTicket.asDatabaseModel()

            //room update
            dataSource.update(new)

            //firebase database update
            val ref = fbDatabase.reference.child("photoTicket").child(user.uid).child(key)

            ref.setValue(new.asFirebaseModel()).addOnFailureListener {
                Log.d(TAG, "Network Connection Error : ${it.message}")
                //Toast -> SnackBar 로 변경
                Toast.makeText(application, "네트워크 연결되지 않아 업데이트 되지 않았다.", Toast.LENGTH_SHORT).show()
            }


        }
    }

    /**
     * 포토티켓의 삭제 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 삭제할 포토티켓을 매개변수로 전달 받아 RoomDatabase와 Firebase 실시간 데이터 베이스 및 Storage 내에
     * 포토티켓 정보를 삭제한다.
     * */
    suspend fun deletePhotoTickets(key: String, application: Application) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {

            //room delete
            try {
                dataSource.delete(key)
            } catch (error: Exception) {
                Log.i(TAG, "room database delete error : ${error.message}")
            }


            //firebase database delete
            val ref =
                fbDatabase.reference.child("photoTicket").child(user.uid).child(key)
            ref.removeValue().addOnFailureListener {
                Log.d(TAG, "Network Connection Error : ${it.message}")
                //Toast -> SnackBar 로 변경
                Toast.makeText(application, "네트워크 연결되지 않아 삭제 되지 않았다.", Toast.LENGTH_SHORT).show()
            }

            //firebase storage delete
            val storageRef =
                fbStorage.reference.child("photoTicket").child(user.uid).child(key).child("image")
            storageRef.listAll().addOnSuccessListener {
                if (it.items.size > 0) {
                    storageRef.child(it.items[0].toString().toUri().lastPathSegment.toString())
                        .delete()
                        .addOnSuccessListener {
                            Log.i(TAG, "Storage Delete Success")
                        }.addOnFailureListener {
                            Log.i(TAG, "Storage Delete Fail : ${it.message}")
                        }
                }
            }.addOnFailureListener {
                Log.d(TAG, "Network Connection Error : ${it.message}")
                //Toast -> SnackBar 로 변경
                Toast.makeText(application, "네트워크 연결되지 않아 삭제 되지 않았다.", Toast.LENGTH_SHORT).show()
            }
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

            val new = photoTicket.asDatabaseModel().asFirebaseModel()
            val ref = fbDatabase.reference.child("photoTicket").child(user.uid).push()
            Log.i(
                TAG,
                "new : ${new}, user : ${user}, user.isVerified : ${user.isEmailVerified}, fbDatabase : ${ref}"
            )
            ref.setValue(
                new,
                DatabaseReference.CompletionListener { error: DatabaseError?, ref: DatabaseReference ->

                    if (error != null) {
                        Log.d(TAG, "Unable to write Message to database", error.toException())
                        return@CompletionListener
                    }

                    val file = photoTicket.url.toUri()
                    val mimeType: String? = application.contentResolver.getType(file)
                    Log.i(TAG, "file ${file}")
                    val key = ref.key
                    val storageRef = fbStorage.getReference("photoTicket")
                        .child(user.uid)
                        .child(key!!)
                        .child("${mimeType?.split("/")?.get(0)}/${file.lastPathSegment}")

                    insertImageInStorage(storageRef, user, file, key, new)


                })

        }
    }

    private fun insertImageInStorage(
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

        storageRef.putFile(file, metadata)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { url ->
                        CoroutineScope(Dispatchers.IO).launch {
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

    companion object {
        const val TAG = "포토티켓리포지터리"
    }


}