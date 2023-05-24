package com.example.assignment

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignment.databinding.ActivityRecyclerBinding
import com.example.assignment.roomDB.KeyValueDao
import com.example.assignment.roomDB.KeyValueEntity
import com.example.assignment.roomDB.KeyValueItem
import com.example.assignment.roomDB.MyAppDatabase
import com.example.assignment.ui.KeyValueAdapter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class RecyclerActivity : AppCompatActivity(), KeyValueAdapter.OnClickData {
    private lateinit var adapter: KeyValueAdapter
    private lateinit var binding: ActivityRecyclerBinding
    private lateinit var db: MyAppDatabase
    private lateinit var keyValueDao: KeyValueDao
    var weightRangesItems: ArrayList<KeyValueItem> = ArrayList<KeyValueItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        db = MyAppDatabase.getDatabase(this)
        keyValueDao = db.keyValueDao()
        setContentView(binding.root)
        weightRangesItems.add(KeyValueItem("", ""));
        adapter = KeyValueAdapter(weightRangesItems, this)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.btnSave.setOnClickListener {
            val jsonObject = getJsonObjectFromData()
            if (validateUniqueKeys()) {
                saveToDatabase(jsonObject)
            } else {
                Toast.makeText(this, "Duplicate keys are not allowed", Toast.LENGTH_SHORT).show()
            }
        }



        binding.btnFetch.setOnClickListener {
            lifecycleScope.launch {
                val value = keyValueDao.getAll()
                Toast.makeText(this@RecyclerActivity, "" + Gson().toJson(value), Toast.LENGTH_SHORT)
                    .show()
                Log.d("value", "onCreate: " + Gson().toJson(value))
            }
        }

    }

    private fun getJsonObjectFromData(): JSONObject {
        val keyValueList = adapter.getData()
        val jsonObject = JSONObject()
        for (item in keyValueList) {
            jsonObject.put(item.key, item.value)
        }
        return jsonObject
    }


    private fun validateUniqueKeys(): Boolean {
        val keyValueList = adapter.getData()
        val keySet = mutableSetOf<String>()
        for (item in keyValueList) {
            val key = item.key.trim()
            if (key.isNotEmpty() && !keySet.add(key)) {
                return false
            }
        }
        return true
    }

    private fun saveToDatabase(jsonObject: JSONObject) {
        // Convert JSONObject to a list of KeyValueEntity objects
        val entityList = mutableListOf<KeyValueEntity>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.getString(key)
            val entity = KeyValueEntity(key = key, value = value)
            entityList.add(entity)
        }

        // Save the entityList to the Room database
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                keyValueDao.insert(entityList)
            }
            Toast.makeText(this@RecyclerActivity, "Data saved to database", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onButtonClick(pos: Int, weightRangeDataRequest: KeyValueItem) {
        addEditTextItem()
    }

    private fun addEditTextItem() {
        val newItem = KeyValueItem("", "")
        weightRangesItems.add(newItem)
        adapter.notifyItemInserted(weightRangesItems.size - 1)
    }


    override fun onButtonDelete(weightRangeItem: KeyValueItem, pos: Int) {
        weightRangesItems.removeAt(pos)
        adapter.notifyItemRemoved(pos)
        adapter.notifyItemRangeChanged(pos, weightRangesItems.size - 1)
        if (pos == weightRangesItems.size) {
            adapter.notifyItemChanged(pos - 1) // Notify the previous item
        } else {
            adapter.notifyItemChanged(pos) // Notify the current item
        }

    }
}