package com.example.paybook

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserCreate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_create)

        var user_id: Long = 0
        val user_database = FirebaseDatabase.getInstance().reference.child("User")
        var userList = ArrayList<User>()
        val userpostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    user_id = maxOf(user_id,dataSnapshot.childrenCount)
                    //userList.clear()
                    val nuserList = ArrayList<User>()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        val ok = userSnapshot.getValue(User::class.java) ?: continue
                        nuserList.add(ok)
                        user_id = maxOf(user_id,ok.id)
                    }
                    userList = nuserList
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
        findViewById<Button>(R.id.create_user_button).setOnClickListener {
            val name = findViewById<EditText>(R.id.create_user_name).text.toString()
            val userPresent = checkUserPresent(userList,name)
            if (userPresent == 0)
            {
                user_database.child((user_id+ 1).toString()).setValue(User(name,0,user_id+ 1))
                Toast.makeText(this, "User $name saved", Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(this, "User $name ALREADY PRESENT", Toast.LENGTH_SHORT).show()
            }

        }

        val datetextView: TextView  = findViewById(R.id.editTextDateaccount)
        datetextView.text = SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis())

        var cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            datetextView.text = sdf.format(cal.time)

        }

        datetextView.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        var account_id: Long = 0
        val account_database = FirebaseDatabase.getInstance().reference.child("Accounts")
        findViewById<Button>(R.id.create_account_button).setOnClickListener {
            val username = findViewById<Spinner>(R.id.userspinner).getSelectedItem().toString()
            val price = findViewById<EditText>(R.id.account_price).text.toString().toInt()
            val date = findViewById<EditText>(R.id.editTextDateaccount).text.toString()
            val duration = findViewById<EditText>(R.id.account_duration).text.toString().toInt()
            val accountNumber = findViewById<EditText>(R.id.accountNumber).text.toString().toLong()
            val rduration = findViewById<EditText>(R.id.account_recieved).text.toString().toInt()
            account_database.child((account_id+ 1).toString()).setValue(Account(account_id+1,username,price,date,duration, rduration, "New", accountNumber))
            Toast.makeText(this, "Account $username saved", Toast.LENGTH_SHORT).show()
        }

        val accountpostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    //account_id = (dataSnapshot.childrenCount)
                    account_id = maxOf(account_id,dataSnapshot.childrenCount)
                    for(userSnapshot in dataSnapshot.getChildren()){
                        val ok = userSnapshot.getValue(Account::class.java) ?: continue
                        account_id = maxOf(account_id,ok.accountId)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Database", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        account_database.addValueEventListener(accountpostListener)


    }

    private fun checkUserPresent(userList: ArrayList<User>, name: String): Int {
        for(user in userList)
        {
            if(user.name == name)
            {
                return 1
            }
        }
        return 0
    }

    private fun updateSpinner(userList: ArrayList<User>) {
        var languages = ArrayList<String>()
        for(user in userList)
        {
            languages.add(user.name)
        }
        languages.sort()
        val spinner: Spinner = findViewById(R.id.userspinner)
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