package com.example.paybook

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.paybook.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TransactionView2 : AppCompatActivity() {
    private var userFocus: String = ""
    var userList = ArrayList<User>()
    private fun fetUserByName(userFocus: String): User {
        var userObj:User = User()
        for(user in userList)
        {
            if(user.name == userFocus)
            {
                userObj = user
                break
            }
        }
        return userObj
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_view)
        addUserListSpinner()
        var AccountList = ArrayList<Transaction>()
        val userfocusSpinner = findViewById<Spinner>(R.id.user_view_name_select)

        userfocusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                userFocus = userfocusSpinner.getSelectedItem().toString()
                val userObj:User = fetUserByName(userFocus)
                findViewById<TextView>(R.id.user_view_name).text = userObj.name
                findViewById<TextView>(R.id.user_view_extra).text = userObj.extra.toString()
                val AccountList_user = userListonFocus(AccountList)
                val accountListview = findViewById<ListView>(R.id.account_list_view)
                accountListview.adapter = CustomAdaptor(this@TransactionView2, AccountList_user)
            }

        }
        val account_database = FirebaseDatabase.getInstance().reference.child("Transaction")

        val accountpostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    AccountList.clear()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        println("start")
                        val ok = userSnapshot.getValue(Transaction::class.java) ?: continue
                        AccountList.add(ok)
                    }
                    println("DOne and dusted")
                    val AccountListFocus = userListonFocus(AccountList)
                    val accountListview = findViewById<ListView>(R.id.account_list_view)
                    accountListview.adapter = CustomAdaptor(this@TransactionView2, AccountListFocus)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Database", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        account_database.addValueEventListener(accountpostListener)

        addUserListSpinner()
    }

    fun userListonFocus(userList: ArrayList<Transaction>):  ArrayList<Transaction> {
        val user_list_focus = ArrayList<Transaction>()
        for(user in userList)
        {
            if(user.user == userFocus || userFocus == "")
            {
                user_list_focus.add(user)
            }
        }
        return user_list_focus
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

    private fun addUserListSpinner()
    {
        val user_database = FirebaseDatabase.getInstance().reference.child("User")

        val userpostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newUserlist = ArrayList<User>()
                if(dataSnapshot.exists())
                {
                    newUserlist.clear()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        val ok = userSnapshot.getValue(User::class.java) ?: continue
                        newUserlist.add(ok)
                    }
                    userList = newUserlist
                    updateSpinner(userList)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Database", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }

        user_database.addValueEventListener(userpostListener)
    }
    private fun updateSpinner(userList: ArrayList<User>) {
        val languages = ArrayList<String>()
        for(user in userList)
        {
            languages.add(user.name)
        }
        languages.sort()
        val spinner: Spinner = findViewById(R.id.user_view_name_select)
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }
}