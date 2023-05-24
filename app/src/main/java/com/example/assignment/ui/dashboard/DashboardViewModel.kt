package com.example.assignment.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Click on me to redirect Custom Camera using Surface View"
    }
    val text: LiveData<String> = _text
}