package com.ranseo.solaroid.ui.home.fragment.add

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.R
import com.ranseo.solaroid.adapter.OnChoiceClickListener
import com.ranseo.solaroid.adapter.SolaroidChoiceAdapter
import com.ranseo.solaroid.room.SolaroidDatabase
import com.ranseo.solaroid.databinding.FragmentSolaroidAddChoiceBinding
import com.ranseo.solaroid.dialog.ChoiceDialogFragment

class SolaroidAddChoiceFragment() : Fragment(), ChoiceDialogFragment.ChoiceDialogListener {

    private lateinit var viewModel: SolaroidAddViewModel
    private lateinit var viewModelFactory: SolaroidAddViewModelFactory

    private lateinit var backPressCallback :OnBackPressedCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidAddChoiceBinding>(
            inflater,
            R.layout.fragment_solaroid_add_choice,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)
        viewModelFactory = SolaroidAddViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(
            requireParentFragment(),
            viewModelFactory
        )[SolaroidAddViewModel::class.java]

        val adapter = SolaroidChoiceAdapter(OnChoiceClickListener {
            it?.let{
                viewModel.setUriChoiceFromMediaStore(it)
            }
        })

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.adapter = adapter



        viewModel.imagesFromMediaStore.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        viewModel.uriChoiceFromMediaStore.observe(viewLifecycleOwner, Observer{
            it.getContentIfNotHandled()?.let{ uri ->
                uri?.let { notNullUri ->
                    showDialog(notNullUri.toString())
                }

            }
        })

        return binding.root
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressedInChoice()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,backPressCallback)
    }

    override fun onDetach() {
        super.onDetach()
        backPressCallback.remove()
    }

    fun showDialog(uri:String) {
        val choiceDialog = ChoiceDialogFragment(this, uri)
        choiceDialog.show(parentFragmentManager, "choiceDialog")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, uri:String) {
        viewModel.setImageValue(uri)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    companion object {
        const val TAG = "애드초이스프래그먼트"
    }
}