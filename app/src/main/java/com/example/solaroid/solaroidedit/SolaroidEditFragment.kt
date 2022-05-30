package com.example.solaroid.solaroidedit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.convertDateToLong
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidEditBinding
import com.example.solaroid.dialog.DatePickerDialogFragment
import com.example.solaroid.dialog.SaveDialogFragment

class SolaroidEditFragment : Fragment(), SaveDialogFragment.EditSaveDialogListener, DatePickerDialogFragment.OnDatePickerDialogListener {

    private lateinit var viewModel : SolaroidEditFragmentViewModel
    private lateinit var viewModelFactory: SolaroidEditFragmentViewModelFactory

    private lateinit var backPressCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidEditBinding>(inflater, R.layout.fragment_solaroid_edit, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        val key = SolaroidEditFragmentArgs.fromBundle(requireArguments()).photoTicketKey

        viewModelFactory = SolaroidEditFragmentViewModelFactory(key, dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidEditFragmentViewModel::class.java]

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.saveBtn.setOnClickListener {
            showDialog()
        }

        viewModel.naviToFrameFrag.observe(viewLifecycleOwner, Observer {
           it.getContentIfNotHandled()?.let{
               findNavController().navigate(
                   SolaroidEditFragmentDirections.actionEditFragmentToFrameFragmentContainer()
               )
           }
        })

        binding.todayDate.setOnClickListener {
            showDatePickerDialog()
        }

        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.navigateToFrame()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,backPressCallback)
    }


    override fun onDialogPositiveClick(dialog: DialogFragment) {
        viewModel.onUpdatePhotoTicket()
        viewModel.navigateToFrame()
    }


    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    fun showDialog() {
        val editSaveDialog = SaveDialogFragment(this)
        editSaveDialog.show(parentFragmentManager, "editSave")
    }

    fun showDatePickerDialog() {
        val newFragment = DatePickerDialogFragment(this)
        newFragment.show(parentFragmentManager, "datePicker")
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        viewModel.setDate(convertTodayToFormatted(convertDateToLong(year,month,day)))
    }
}