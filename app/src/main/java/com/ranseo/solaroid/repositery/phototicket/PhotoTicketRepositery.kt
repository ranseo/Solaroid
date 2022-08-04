package com.ranseo.solaroid.repositery.phototicket

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.models.domain.asDatabaseModel
import com.ranseo.solaroid.models.domain.asFirebaseModel
import com.ranseo.solaroid.models.room.asDomainModel
import com.ranseo.solaroid.models.room.asFirebaseModel
import com.ranseo.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.ranseo.solaroid.firebase.FirebasePhotoTicket
import com.ranseo.solaroid.firebase.asDatabaseModel
import com.ranseo.solaroid.models.firebase.FirebaseAlbum
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.NullPointerException

class PhotoTicketRepositery(
    private val dataSource: DatabasePhotoTicketDao,
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val fbStorage: FirebaseStorage,
    private val photoTicketListenerDataSource: PhotoTicketListenerDataSource
) {

    private var listener : ValueEventListener? = null
    val user = fbAuth.currentUser!!.email!!

    /**
     * 포토티켓의 날짜기준 내림차순 정렬.
     * */
    val photoTicketsOrderByDesc: LiveData<List<PhotoTicket>> =
        Transformations.map(dataSource.getAllPhotoTicketWithUserDesc(user)) {
            it?.asDomainModel()
        }

    /**
     * 포토티켓의 날짜기준 오름차순 정렬.
     * */
    val photoTicketsOrderByAsc: LiveData<List<PhotoTicket>> =
        Transformations.map(dataSource.getAllPhotoTicketWithUserAsc(user)) {
            it?.asDomainModel()
        }

    /**
     * 포토티켓의 즐겨찾기만 정렬
     * */
    val photoTicketsOrderByFavorite: LiveData<List<PhotoTicket>> =
        Transformations.map(dataSource.getAllPhotoTicketWithUserFavorite(user, true)) {
            it?.asDomainModel()
        }


//    suspend fun getPhotoTicket(key: String): PhotoTicket = withContext(Dispatchers.IO) {
//        dataSource.getDatabasePhotoTicket(key).asDomainModel()
//    }


    /**
     * 어플리케이션을 처음 실행할 때 또는 UI를 전환할 때(프레임컨테이너 <-> 갤러리프래그먼트) 포토티켓 리스트를
     * 화면에 띄우기 위해 firebase의 실시간 데이버테이스로 부터 FirebasePhotoTicket을 불러오고 이를 다시 room database에 insert하는
     * ValueEventListener를 등록하여 refresh하는 함수.
     * */
    suspend fun refreshPhotoTickets(albumId:String, albumKey:String , insertRoomDb: (List<FirebasePhotoTicket>) -> Unit) =
        withContext(Dispatchers.IO) {
            listener = photoTicketListenerDataSource.setGalleryPhotoTicketList(insertRoomDb)
            val ref = fbDatabase.reference.child("photoTicket").child(albumId).child(albumKey)
            ref.addValueEventListener(listener!!)
        }

    /**
     * 어플리케이션을 처음 실행할 때 또는 UI를 전환할 때(프레임컨테이너 <-> 홈 갤러리 프래그먼트) 모든 포토티켓 리스트를
     * 화면에 띄우기 위해 firebase의 실시간 데이버테이스로 부터 FirebasePhotoTicket을 불러오고 이를 다시 room database에 insert하는
     * firebase - ValueEventListener를 등록하여 refresh하는 함수.
     * */
    suspend fun refreshPhotoTickets(albumId: String, insertRoomDb: (List<FirebasePhotoTicket>) -> Unit) =
        withContext(Dispatchers.IO) {
            listener = photoTicketListenerDataSource.setHomePhotoTicketList(insertRoomDb)
            val ref = fbDatabase.reference.child("photoTicket").child(albumId)
            ref.addListenerForSingleValueEvent(listener!!)
        }

    /**
     * 포토티켓의 업데이트 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 업데이트할 포토티켓(Domaain Model)을 매개변수로 전달 받아 RoomDatabase와 Firebase의 실시간데이터베이스 및 storaage(url업데이트 시) 내에
     * 포토티켓 정보를 업데이트 한다.
     * */
    suspend fun updatePhotoTickets(albumId:String, albumKey:String ,photoTicket: PhotoTicket, application: Application) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val key = photoTicket.id
            val new = photoTicket.asDatabaseModel(user.email!!)

            //room update
            dataSource.update(new)

            //firebase database update
            val ref = fbDatabase.reference.child("photoTicket").child(albumId).child(albumKey).child(key)

            ref.setValue(new.asFirebaseModel()).addOnFailureListener {
                Log.d(TAG, "Network Connection Error : ${it.message}")
                //Toast -> SnackBar 로 변경
                Toast.makeText(application, "네트워크 연결되지 않아 업데이트 불가능.", Toast.LENGTH_SHORT).show()
            }


        }
    }

    /**
     * room database 내에서 photoTicket삭제
     * */
    suspend fun deletePhotoTicketInRoom(key:String) {
        withContext(Dispatchers.IO) {
            dataSource.delete(key)
        }
    }
    /**
     * 포토티켓의 삭제 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 삭제할 포토티켓을 매개변수로 전달 받아 RoomDatabase와 Firebase 실시간 데이터 베이스 및 Storage 내에
     * 포토티켓 정보를 삭제한다.
     * */
    suspend fun deletePhotoTicket(albumId:String, albumKey:String ,key: String, application: Application) {
        withContext(Dispatchers.IO) {

            //room delete
            try {
                deletePhotoTicketInRoom(key)
                //firebase database delete
                val ref =
                    fbDatabase.reference.child("photoTicket").child(albumId).child(albumKey).child(key)
                ref.removeValue().addOnFailureListener {
                    Log.d(TAG, "Network Connection Error : ${it.message}")
                    //Toast -> SnackBar 로 변경
                    Toast.makeText(application, "네트워크 연결되지 않아 삭제 되지 않았다.", Toast.LENGTH_SHORT).show()
                }

                //firebase storage delete
                val storageRef =
                    fbStorage.reference.child("photoTicket").child(albumId).child(key).child("image")
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
            } catch (error: Exception) {
                Log.i(TAG, "room database delete error : ${error.message}")
            } catch (error:NullPointerException) {
                error.printStackTrace()
            }
        }
    }

    /**
     * 포토티켓의 앨범을 삭제할 때 사용자는 더 이상 해당 앨범에 속한 포토티켓을 볼 수 없어야 한다.
     * 따라서 firebase-photoTicket 경로에 albumId - albumKey 경로를 삭제한다.
     * 단, 앨범의 참여자가 나 혼자 일때만 가능.
     * */
    suspend fun deletePhotoTickets(album:FirebaseAlbum) {
        return withContext(Dispatchers.IO) {
            try {
                fbDatabase.reference.child("photoTicket").child(album.id).child(album.key).removeValue()
            }catch (error:IOException) {
                error.printStackTrace()
            }catch (error:NullPointerException) {
                error.printStackTrace()
            }
        }
    }

    /**
     * room database photoTicket table내에 전달받은 albumId를 가진 포토티켓을 모두 삭제
     * */
    suspend fun deletePhotoTicketsInRoom(albumId: String) {
        return withContext(Dispatchers.IO) {
            dataSource.deletePhotoTicketsWithAlbumId(albumId)
        }
    }


    /**
     * 포토티켓의 삽입 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 삽입할 포토티켓을 매개변수로 전달 받아 RoomDatabase 및 Realtime database
     * 내에 포토티켓 정보를 삽입한다. Firebase Storage 내에는 파일 (URL) 정보를 입력한다.
     * */
    suspend fun insertPhotoTickets(album: FirebaseAlbum, photoTicket: PhotoTicket, application: Application) {
        val user = fbAuth.currentUser!!
        withContext(Dispatchers.IO) {
            val insertRef = fbDatabase.reference.child("photoTicket").child(album.id).child(album.key).push()
            val key = insertRef.key ?: ""
            var new = photoTicket.asFirebaseModel(key)
            var file: Uri? = null
            var storageRef: StorageReference? = null

            suspendCancellableCoroutine<Unit> { continuation ->
                insertRef.setValue(
                    new,
                    DatabaseReference.CompletionListener { error: DatabaseError?, _: DatabaseReference ->
                        if (error != null) {
                            Log.d(TAG, "Unable to write Message to database", error.toException())
                            continuation.resume(Unit, null)
                            return@CompletionListener
                        }


                        file = photoTicket.url.toUri()
                        val mimeType: String? = application.contentResolver.getType(file!!)

                        Log.i(TAG, "file ${file}")

                        storageRef = fbStorage.getReference("photoTicket")
                            .child(album.id)
                            .child(key)
                            .child("${mimeType?.split("/")?.get(0)}/${file!!.lastPathSegment}")

                        continuation.resume(Unit, null)
                    })


            }
            try {
                insertImageInStorage(storageRef!!, user, file!!, key, new)
            } catch (error: Exception) {
                Log.i(TAG, "error : ${error.message}")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun insertImageInStorage(
        storageRef: StorageReference,
        user: FirebaseUser,
        file: Uri,
        key: String,
        pre: FirebasePhotoTicket
    ) {
        suspendCancellableCoroutine<Unit> { continuation ->
            //metadata 지정
            var metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            storageRef.putFile(file, metadata)
                .addOnSuccessListener { taskSnapshot ->
                    Log.i(TAG, "taskSnapshot.metadata!!.reference!!.downloadUrl")
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { url ->

                            val new = FirebasePhotoTicket(
                                key = pre.key,
                                url = url.toString(),
                                frontText = pre.frontText,
                                backText = pre.backText,
                                date = pre.date,
                                favorite = pre.favorite,
                                albumId = pre.albumId,
                                albumName = pre.albumName,
                                albumKey = pre.albumKey
                            )

                            fbDatabase.reference.child("photoTicket")
                                .child(new.albumId)
                                .child(new.albumKey)
                                .child(new.key)
                                .setValue(new)
                            Log.i(
                                TAG,
                                "Before dataSource.insert(new.asDatabaseModel(user.email!!))"
                            )
                            val async = CoroutineScope(Dispatchers.IO).async {
                                dataSource.insert(new.asDatabaseModel(user.email!!))
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                val unit = async.await()
                                continuation.resume(unit, null)
                            }


                        }

                        .addOnFailureListener { error ->
                            Log.d(TAG, "taskSnapShot error ${error.message}")
                            continuation.resume(Unit, null)
                        }
                }
                .addOnFailureListener { error ->
                    Log.d(TAG, "sotrageRef.putfile error ${error.message}")
                    continuation.resume(Unit, null)
                }

        }


    }

    fun removeListener(albumId:String, albumKey:String) {
        val ref = fbDatabase.reference.child("photoTicket").child(albumId).child(albumKey)
        ref.removeEventListener(listener!!)
    }

    companion object {
        const val TAG = "포토티켓리포지터리"
    }


}