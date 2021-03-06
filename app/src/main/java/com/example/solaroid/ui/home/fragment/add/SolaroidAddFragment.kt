package com.example.solaroid.ui.home.fragment.add

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.solaroid.R
import com.example.solaroid.convertDateToLong
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidAddBinding
import com.example.solaroid.dialog.DatePickerDialogFragment
import com.example.solaroid.dialog.SaveDialogFragment
import com.example.solaroid.ui.home.fragment.gallery.AIAK
import com.google.firebase.auth.FirebaseAuth


/**
 * mediaCollection을 열어 사진을 골라 새로운 포토티켓을 만드는 프래그먼트.
 * */
class SolaroidAddFragment : Fragment(),
    SaveDialogFragment.EditSaveDialogListener,
    DatePickerDialogFragment.OnDatePickerDialogListener,
    AdapterView.OnItemSelectedListener {

    private lateinit var viewModel: SolaroidAddViewModel
    private lateinit var viewModelFactory: SolaroidAddViewModelFactory

    private lateinit var auth: FirebaseAuth

    private lateinit var backPressCallback : OnBackPressedCallback

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

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.albumNameList.observe(viewLifecycleOwner) { list ->
            if(!list.isNullOrEmpty()) {
                val defaultIdx=  list.indexOf(viewModel.albumName)
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    list
                ).also{ adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerAlbum.adapter = adapter
                    binding.spinnerAlbum.onItemSelectedListener = this
                    binding.spinnerAlbum.setSelection(defaultIdx)
                }
            }
        }

        viewModel.naviToAddChoice.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {

                childFragmentManager.commit {
                    add<SolaroidAddChoiceFragment>(R.id.fragment_add_container_view, TAG_ADD_CHOICE)
                }

                binding.addChoiceFragmentLayout.visibility = VISIBLE
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
                binding.addChoiceFragmentLayout.visibility = INVISIBLE
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
                binding.addChoiceFragmentLayout.visibility = INVISIBLE
            }
        })

//        viewModel.naviToFrameFrag.observe(viewLifecycleOwner, Observer{
//            it.getContentIfNotHandled()?.let{
//                this.findNavController().navigate(
//                    SolaroidAddFragmentDirections.actionAddToFrame()
//                )
//            }
//        } )

        binding.saveBtn.setOnClickListener {
            showDialog()
        }

        binding.todayDate.setOnClickListener {
            showDatePickerDialog()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun showDialog() {
        val dialog = SaveDialogFragment(this)
        dialog.show(parentFragmentManager, TAG_ADD_SAVE)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        viewModel.insertPhotoTicket()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    fun showDatePickerDialog() {
        val newFragment = DatePickerDialogFragment(this)
        newFragment.show(parentFragmentManager, "DatePicker")
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        viewModel.setDate(convertTodayToFormatted(convertDateToLong(year,month,day)))
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        backPressCallback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                viewModel.navigateToFrame()
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(this,backPressCallback)
//    }

//    override fun onDetach() {
//        super.onDetach()
//        backPressCallback.remove()
//    }

    companion object {
        const val TAG ="애드프래그먼트"
        const val TAG_ADD_CHOICE = "TAG_ADD_CHOICE"
        const val TAG_ADD_SAVE = "ADD_SAVE"
    }

    //Spinner ItemSelected
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        viewModel.setWhichAlbum(p2)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}