package com.example.assignment.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Click on me to redirect on page with recycler view with 2 EditText(Key and Value)"
    }
    val text: LiveData<String> = _text
}