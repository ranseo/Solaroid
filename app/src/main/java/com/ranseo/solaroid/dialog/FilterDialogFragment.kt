package com.ranseo.solaroid.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentFilterDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterDialogFragment(_listener:OnFilterDialogListener) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFilterDialogBinding

    val listener : OnFilterDialogListener = _listener

    interface OnFilterDialogListener {
        fun onFilterDesc()
        fun onFilterAsc()
        fun onFilterFavorite()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter_dialog, container, false)

        val radioChangedLister = RadioGroup.OnCheckedChangeListener { p0, p1 ->
            when(p1) {
                R.id.radio_date_desc -> {
                    listener.onFilterDesc()
                }
                R.id.radio_date_asc -> {
                    listener.onFilterAsc()
                }
                R.id.radio_favorite -> {
                    listener.onFilterFavorite()
                }
            }
        }

        binding.radioGroup.setOnCheckedChangeListener(radioChangedLister)
        binding.radioDateDesc.isChecked=true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = binding.layoutBottomSheet
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}