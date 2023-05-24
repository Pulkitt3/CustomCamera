package com.example.assignment.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.assignment.roomDB.MyAppDatabase
import com.example.assignment.databinding.FragmentHomeBinding
import com.example.assignment.ui.database.DatabaseActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var database: MyAppDatabase

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
//        database = MyAppDatabase.getDatabase(requireContext())


//        binding.buttonSave.setOnClickListener {
//            val key = binding.editTextKey.text.toString()
//            val value = binding.editTextValue.text.toString()
//            val keyValuePair = KeyValuePair(key,value)
//            lifecycleScope.launch {
//                database.keyValuePairDao().insert(keyValuePair)
//            }
//        }
//
//        binding.buttonFetch.setOnClickListener {
//            val key = binding.editTextFetchValue.text.toString()
//            lifecycleScope.launch {
//                val value = database.keyValuePairDao().getValueByKey(key)
//                binding.textViewValue.text = value
//            }
//
        binding.textHome.setOnClickListener {
            val intent = Intent (requireContext(),DatabaseActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}