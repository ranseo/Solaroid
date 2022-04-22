package com.example.solaroid.repositery

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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class PhotoTicketRepositery(private val dataSource: DatabasePhotoTicketDao) {

    val photoTickets : LiveData<List<PhotoTicket>> = Transformations.map(dataSource.getAllDatabasePhotoTicket()) {
        it?.let{
            it.asDomainModel()
        }
    }

    /**
     * 어플리케이션을 처음 실행할 때 또는 UI를 전환할 때(프레임컨테이너 <-> 갤러리프래그먼트) 포토티켓 리스트를
     * 화면에 띄우기 위해 firebase의 실시간 데이버테이스로 부터 FirebasePhotoTicket을 불러오고 이를 다시 room database에 insert하는
     * ValueEventListener를 등록하여 refresh하는 함수.
     * */
    suspend fun refreshPhotoTickets(user:FirebaseUser, fbDatabase:FirebaseDatabase) {
        withContext(Dispatchers.IO) {
            val ref = fbDatabase.reference.child("photoTicket").child(user.uid)
            ref.addListenerForSingleValueEvent(setPhotoTicketList(){
                launch(Dispatchers.IO) { dataSource.insert(it.asDatabaseModel()) }
            })
        }
    }

    /**
     * 포토티켓의 업데이트 기능을 수행하는 리포지터리 함수
     * 해당 함수에서는 업데이트할 포토티켓(Domaain Model)을 매개변수로 전달 받아 RoomDatabase와 Firebase의 실시간데이터베이스 및 storaage(url업데이트 시) 내에
     * 포토티켓 정보를 업데이트 한다.
     * */
    suspend fun updatePhotoTickets(user:FirebaseUser, fbDatabase:FirebaseDatabase, photoTicket:PhotoTicket ) {
        withContext(Dispatchers.IO) {
            val key = dataSource.getDatabasePhotoTicket(photoTicket.id).firebaseKey
            val new = photoTicket.asDatabaseModel(key)

            //room update
            dataSource.update(new)

            //firebase update
            val ref = fbDatabase.reference.child(user.uid).child("photoTicket").child(key)
            ref.setValue(new.asFirebaseModel())

        }
    }



}