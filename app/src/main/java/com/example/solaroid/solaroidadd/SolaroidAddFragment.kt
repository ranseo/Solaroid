package com.example.solaroid.solaroidadd

import SolaroidAddViewModelFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidAddBinding
import com.example.solaroid.dialog.SaveDialogFragment
import com.example.solaroid.firebase.RealTimeDatabaseViewModel
import com.example.solaroid.firebase.RealTimeDatabaseViewModelFactory
import com.example.solaroid.solaroidadd.SolaroidAddViewModel
import com.google.firebase.auth.FirebaseAuth


/**
 * mediaCollection을 열어 사진을 골라 새로운 포토티켓을 만드는 프래그먼트.
 * */
class SolaroidAddFragment : Fragment(), SaveDialogFragment.EditSaveDialogListener {

    private lateinit var viewModel: SolaroidAddViewModel
    private lateinit var viewModelFactory: SolaroidAddViewModelFactory
    private lateinit var firebaseDBViewModel: RealTimeDatabaseViewModel

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidAddBinding>(
            inflater,
            R.layout.fragment_solaroid_add,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        auth = FirebaseAuth.getInstance()

        viewModelFactory = SolaroidAddViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidAddViewModel::class.java]

        firebaseDBViewModel = ViewModelProvider(requireActivity(), RealTimeDatabaseViewModelFactory(auth.currentUser!!,application))[RealTimeDatabaseViewModel::class.java]

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.photoTicket.observe(viewLifecycleOwner, Observer {
            it?.let{ photo ->
                firebaseDBViewModel.setValueInPhotoTicket(photo)
                requireActivity().onBackPressed()
            }
        })

        viewModel.naviToAddChoice.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.setUriNull()
                childFragmentManager.commit {
                    add<SolaroidAddChoiceFragment>(R.id.fragment_add_container_view, TAG_ADD_CHOICE)
                }
            }
        })

        viewModel.backPressed.observe(viewLifecycleOwner, Observer {
            if (it) {
                val addChoiceFragment = childFragmentManager.findFragmentByTag(TAG_ADD_CHOICE)
                childFragmentManager.commit {
                    if (addChoiceFragment != null) {
                        remove(addChoiceFragment)
                    }
                }
                viewModel.doneNavigateToAddChoice()
                viewModel.onBackPressedInChoice()
            }
        })

        viewModel.image.observe(viewLifecycleOwner, Observer {
            it?.let{
                val addChoiceFragment = childFragmentManager.findFragmentByTag(TAG_ADD_CHOICE)
                childFragmentManager.commit {
                    if (addChoiceFragment != null) {
                        remove(addChoiceFragment)
                    }
                }
                viewModel.doneNavigateToAddChoice()
            }
        })

        binding.saveBtn.setOnClickListener {
            showDialog()
        }

        return binding.root
    }


    private fun showDialog() {
        val dialog = SaveDialogFragment(this)
        dialog.show(parentFragmentManager, "addSave")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        viewModel.onInsertPhotoTicket()

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    companion object {
        const val TAG_ADD_CHOICE = "TAG_ADD_CHOICE"
    }
}