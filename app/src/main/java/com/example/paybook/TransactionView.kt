package com.example.paybook

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TransactionView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_view)
        val database = FirebaseDatabase.getInstance().reference.child("Transaction")
        val arrayList = ArrayList<Transaction>()
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    arrayList.clear()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        println("start")
                        val ok = userSnapshot.getValue(Transaction::class.java) ?: continue
                        arrayList.add(ok)
                    }
                    println("DOne and dusted")
                    println(arrayList)
                }
                val transactionListview = findViewById<ListView>(R.id.transaction_list_view)
                transactionListview.adapter = CustomAdaptor(this@TransactionView, arrayList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Database", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        database.addValueEventListener(postListener)
    }

    private class CustomAdaptor(context: Context, arrayList: ArrayList<Transaction>): BaseAdapter(){
        private val mcontext: Context = context
        private val mlist: ArrayList<Transaction> = arrayList
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutinflator = LayoutInflater.from(mcontext)
            val rowMain = layoutinflator.inflate(R.layout.transaction_item_row, parent, false)
            rowMain.findViewById<TextView>(R.id.transaction_rown).text = mlist[position].id.toString()
            rowMain.findViewById<TextView>(R.id.transaction_name).text = mlist[position].user
            rowMain.findViewById<TextView>(R.id.transaction_price).text = mlist[position].price.toString()
            rowMain.findViewById<TextView>(R.id.transaction_date).text = mlist[position].date
            return rowMain
        }

        override fun getItem(position: Int): Any {
           return "Test String"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mlist.size
        }


    }
}