package com.example.solaroid.solaroidadd

import SolaroidAddViewModelFactory
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidAddBinding
import com.example.solaroid.dialog.SaveDialogFragment
import com.example.solaroid.solaroidframe.SolaroidFrameFragment
import com.example.solaroid.solaroidframe.SolaroidFrameFragmentContainer
import com.google.firebase.auth.FirebaseAuth


/**
 * mediaCollection을 열어 사진을 골라 새로운 포토티켓을 만드는 프래그먼트.
 * */
class SolaroidAddFragment : Fragment(), SaveDialogFragment.EditSaveDialogListener {

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

        viewModel.naviToFrameFrag.observe(viewLifecycleOwner, Observer{
            it.getContentIfNotHandled()?.let{
                this.findNavController().navigate(
                    SolaroidAddFragmentDirections.actionAddFragmentToFrameFragmentContainer()
                )
            }
        } )

        binding.saveBtn.setOnClickListener {
            showDialog()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.navigateToFrame()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,backPressCallback)
    }

    override fun onDetach() {
        super.onDetach()
        backPressCallback.remove()
    }

    companion object {
        const val TAG ="애드프래그먼트"
        const val TAG_ADD_CHOICE = "TAG_ADD_CHOICE"
        const val TAG_ADD_SAVE = "ADD_SAVE"
    }
}