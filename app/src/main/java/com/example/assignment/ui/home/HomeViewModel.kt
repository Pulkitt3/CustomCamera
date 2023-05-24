package com.example.assignment.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Click on me  test to redirect on page that has two fields to enter data into database with key value pair data, also make a page where I can get the value in database if I insert the Key into an Edit Text"
    }
    val text: LiveData<String> = _text
}