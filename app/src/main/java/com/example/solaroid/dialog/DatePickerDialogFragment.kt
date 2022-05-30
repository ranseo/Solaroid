package com.example.solaroid.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerDialogFragment(_listener:OnDatePickerDialogListener) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val listener = _listener

    interface OnDatePickerDialogListener {
        fun onDateSet(year:Int, month:Int, day:Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireActivity(),this, year, month, day)
    }


    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        listener.onDateSet(p1,p2,p3)
    }
}