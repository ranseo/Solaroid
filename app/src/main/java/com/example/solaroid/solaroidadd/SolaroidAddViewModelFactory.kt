import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.database.PhotoTicketDao
import com.example.solaroid.solaroidadd.SolaroidAddViewModel
import java.lang.IllegalArgumentException

class SolaroidAddViewModelFactory(val dataSource: PhotoTicketDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(SolaroidAddViewModel::class.java)) {
            return SolaroidAddViewModel(dataSource) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}