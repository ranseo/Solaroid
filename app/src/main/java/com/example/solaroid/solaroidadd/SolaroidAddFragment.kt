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
import com.example.solaroid.solaroidadd.SolaroidAddViewModel


/**
 * mediaCollection을 열어 사진을 골라 새로운 포토티켓을 만드는 프래그먼트.
 * */
class SolaroidAddFragment : Fragment(), SaveDialogFragment.EditSaveDialogListener {

    private lateinit var viewModel: SolaroidAddViewModel
    private lateinit var viewModelFactory: SolaroidAddViewModelFactory

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

        viewModelFactory = SolaroidAddViewModelFactory(dataSource.photoTicketDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidAddViewModel::class.java]

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel.naviToAddChoice.observe(viewLifecycleOwner, Observer {
            if (it) {
                childFragmentManager.commit {
                    add<SolaroidAddChoiceFragment>(R.id.fragment_add_container_view, TAG_ADD_CHOICE)
                }
                viewModel.doneNavigateToAddChoice()
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
                viewModel.onBackPressedInChoice()
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
        requireActivity().onBackPressed()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    companion object {
        const val TAG_ADD_CHOICE = "TAG_ADD_CHOICE"
    }
}