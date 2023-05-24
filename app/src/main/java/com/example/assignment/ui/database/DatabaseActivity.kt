package com.example.assignment.ui.database

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.assignment.KeyValuePair
import com.example.assignment.MyAppDatabase
import com.example.assignment.R
import com.example.assignment.databinding.ActivityDatabaseBinding
import kotlinx.coroutines.launch

class DatabaseActivity : AppCompatActivity() {
    lateinit var binding: ActivityDatabaseBinding
    private lateinit var database: MyAppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = MyAppDatabase.getDatabase(this)

        binding.buttonSave.setOnClickListener {
            val key = binding.editTextKey.text.toString()
            val value = binding.editTextValue.text.toString()
            if (TextUtils.isEmpty(key)) {
                Toast.makeText(this, "Please enter key", Toast.LENGTH_SHORT).show()

            } else if (TextUtils.isEmpty(value)) {
                Toast.makeText(this, "Please enter value", Toast.LENGTH_SHORT).show()

            } else {
                val keyValuePair = KeyValuePair(key, value)

                lifecycleScope.launch {
                    database.keyValuePairDao().insert(keyValuePair)
                    Toast.makeText(this@DatabaseActivity, "Data saved successfully", Toast.LENGTH_SHORT).show()
                    binding.editTextKey.setText("")
                    binding.editTextValue.setText("")
                }

            }


        }

        binding.buttonFetch.setOnClickListener {
            val key = binding.editTextFetchValue.text.toString()
            if (TextUtils.isEmpty(key)) {
                Toast.makeText(this, "Please enter key", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val value = database.keyValuePairDao().getValueByKey(key)
                    binding.textViewValue.text = value.toString()
                }
            }

        }
    }
}