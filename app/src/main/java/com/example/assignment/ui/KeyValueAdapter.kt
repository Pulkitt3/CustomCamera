package com.example.assignment.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.roomDB.KeyValueItem
import com.example.assignment.R


class KeyValueAdapter(
    private var keyValueList: ArrayList<KeyValueItem>,
    private val onClickData: OnClickData,
) : RecyclerView.Adapter<KeyValueAdapter.KeyValueViewHolder>() {


    interface OnClickData {
        fun onButtonClick(pos: Int, weightRangeDataRequest: KeyValueItem)
        fun onButtonDelete(weightRangeItem: KeyValueItem, pos: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyValueViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_edit_text, parent, false)
        return KeyValueViewHolder(view)
    }

    override fun onBindViewHolder(holder: KeyValueViewHolder, position: Int) {
        val item = keyValueList[position]
        holder.bind(item, holder, position)
    }


    override fun getItemCount(): Int {
        return keyValueList.size
    }

    /* fun addItem(weightRangesItems: ArrayList<KeyValueItem>, position: Int) {
         this.keyValueList = weightRangesItems
         keyValueList.add(weightRangesItems[position])
         notifyItemRangeChanged(position, weightRangesItems.size)
     }*/
    fun clearData() {
        keyValueList.clear()
    }

    fun removeData(position: Int) {
        keyValueList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, keyValueList.size - 1)
    }

    fun addItem(item: KeyValueItem) {
        val newItem = KeyValueItem(item.key, item.value) // Create a new instance of KeyValueItem
        keyValueList.add(newItem)
        notifyDataSetChanged()
    }

    fun getData(): List<KeyValueItem> {
        return keyValueList.toList()
    }

    inner class KeyValueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val editTextKey: EditText = itemView.findViewById(R.id.et_startValue)
        private val editTextValue: EditText = itemView.findViewById(R.id.et_endValue)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteicon)
        private val ivAddIcon: ImageView = itemView.findViewById(R.id.iv_addIcon)

        fun bind(item: KeyValueItem, holder: KeyValueViewHolder, position: Int) {
            editTextKey.setText(item.key)
            editTextValue.setText(item.value)

            if (position == 0 && itemCount <= 1) {
                holder.deleteIcon.visibility = View.GONE
                holder.ivAddIcon.visibility = View.VISIBLE
            } else if (position == itemCount - 1) {
                holder.deleteIcon.visibility = View.VISIBLE
                holder.ivAddIcon.visibility = View.VISIBLE
                holder.editTextKey.requestFocus()
            } else {
                holder.deleteIcon.visibility = View.GONE
                holder.ivAddIcon.visibility = View.GONE
            }

            holder.ivAddIcon.setOnClickListener {
                onClickData.onButtonClick(
                    position,
                    KeyValueItem("", "")
                )
            }
            holder.deleteIcon.setOnClickListener {
                onClickData.onButtonDelete(item, position)
            }
            editTextKey.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    item.key = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            editTextValue.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    item.value = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}