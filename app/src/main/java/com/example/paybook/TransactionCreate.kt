package com.example.paybook

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import com.example.paybook.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TransactionCreate : AppCompatActivity() {
    var userList = ArrayList<User>()
    var accountList = ArrayList<Account>()
    var transactionList = ArrayList<Transaction>()
    var idFocus:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_create)

        var transaction_id: Long = 0
        val database = FirebaseDatabase.getInstance().reference.child("Transaction")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    transaction_id = maxOf(transaction_id,dataSnapshot.childrenCount)
                    val newTransactionList = ArrayList<Transaction>()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        val ok = userSnapshot.getValue(Transaction::class.java) ?: continue
                        newTransactionList.add(ok)
                        transaction_id = maxOf(transaction_id,ok.id)
                    }
                    transactionList = newTransactionList
                    updateSpinnerTransaction(transactionList)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Database", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        database.addValueEventListener(postListener)

        val userfocusSpinner = findViewById<Spinner>(R.id.transactionId)
        val decimaltr = findViewById<EditText>(R.id.editTextNumberDecimal)
        val datetr =  findViewById<EditText>(R.id.editTextDate)
        userfocusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                idFocus = userfocusSpinner.getSelectedItem().toString()
                if(idFocus != "")
                {
                    val trObj:Transaction = fetTransactionById(idFocus)
                    if(trObj != Transaction())
                    {
                        findViewById<Spinner>(R.id.editTextTextPersonName).setSelection(languages.indexOf(trObj.user))
                        decimaltr.setText(trObj.price.toString())
                        datetr.setText(trObj.date)
                    }
                }
                else{
                    decimaltr.setText("")
                    datetr.setText(SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis()))
                }

            }

        }

        findViewById<Button>(R.id.button).setOnClickListener {
            val name = findViewById<Spinner>(R.id.editTextTextPersonName).getSelectedItem().toString()
            val prices = findViewById<EditText>(R.id.editTextNumberDecimal).text.toString()
            if(prices == "" ||  name == "")
            {
                Toast.makeText(this, "PRICE SHOULD BE NON EMPTY", Toast.LENGTH_SHORT).show()
            }
            else if(idFocus == "")
            {
                val price = prices.toInt()
                val date = findViewById<EditText>(R.id.editTextDate).text.toString()
                database.child((transaction_id + 1).toString()).setValue(Transaction(name,price,date,transaction_id + 1))
                updateExtraAndAccount(name,price)
                Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val trObj:Transaction = fetTransactionById(idFocus)
                val price = prices.toInt()

                if(trObj.user != name)
                {
                    updateExtraAndAccount(trObj.user,-trObj.price)
                    updateExtraAndAccount(name,price)
                }
                else
                {
                    updateExtraAndAccount(trObj.user,price-trObj.price)
                }
                //updateExtraAndAccount(trObj.user,0)

                val date = findViewById<EditText>(R.id.editTextDate).text.toString()
                database.child((trObj.id).toString()).setValue(Transaction(name,price,date,trObj.id))

                Toast.makeText(this, "Transaction Edited Succesfully", Toast.LENGTH_SHORT).show()
            }

        }
        val datetextView: TextView = findViewById(R.id.editTextDate)
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

        addUserListSpinner()
        addAccount()
    }

    private fun fetTransactionById(idFocus: String): Transaction {
        var trobj = Transaction()
        for(tr in transactionList)
        {
            if(tr.id.toString() == idFocus)
            {
                trobj = tr
                break
            }
        }
        return trobj
    }

    private fun updateExtraAndAccount(name: String, price:Int) {
        var userId:Long = -1
        var userFount:User = User()
        for(user in userList)
        {
            if(user.name == name)
            {
                userId = user.id
                userFount = user
            }
        }
        if(userId == (-1).toLong())
        {
            Toast.makeText(this, "Updation Some error occured", Toast.LENGTH_SHORT).show()
            return
        }
        val user_database = FirebaseDatabase.getInstance().reference.child("User")
        userFount.extra += price
        println("Price updation at the time of " + name + price.toString() + " is " + userFount.extra.toString())
        user_database.child((userId).toString()).child("extra").setValue(userFount.extra)
        if(price!= 0)
        {
            updateAccountOfUser(userFount)
        }

        Toast.makeText(this, "Account Updated", Toast.LENGTH_SHORT).show()
    }

    private fun updateAccountOfUser(userFount: User) {
        val account_database = FirebaseDatabase.getInstance().reference.child("Accounts")
        val user_database = FirebaseDatabase.getInstance().reference.child("User")

        var extra = userFount.extra
        var it:Int = 1
        while(extra > 0 && it != 0)
        {
            it = 0
            for(account in accountList)
            {
                if(account.status != "closed" && account.username == userFount.name)
                {
                    if(account.salary <= extra)
                    {
                        it = 1
                        extra -= account.salary
                        account.receivedMonths = account.receivedMonths + 1
                        if(account.receivedMonths == account.durationInMonths)
                        {
                            account.status = "closed"
                            account_database.child((account.accountId).toString()).child("status").setValue("closed")
                        }
                        account_database.child((account.accountId).toString()).child("receivedMonths").setValue(account.receivedMonths)
                    }
                }
            }
        }

        var it2:Int = 1
        while(extra < 0 && it2 != 0)
        {
            it2 = 0
            for(account in accountList)
            {
                if(account.status != "closed" && account.username == userFount.name)
                {
                    if(extra<0 && account.receivedMonths != 0)
                    {
                        it2 = 1
                        extra += account.salary
                        account.receivedMonths = account.receivedMonths - 1
                        if(account.receivedMonths != account.durationInMonths)
                        {
                            account.status = "New"
                            account_database.child((account.accountId).toString()).child("status").setValue(account.status)
                        }
                        account_database.child((account.accountId).toString()).child("receivedMonths").setValue(account.receivedMonths)
                    }
                }
            }
        }
        it2 = 1
        while(extra < 0 && it2 != 0)
        {
            it2 = 0
            for(account in accountList)
            {
                if(account.username == userFount.name)
                {
                    if(extra<0 && account.receivedMonths != 0)
                    {
                        it2 = 1
                        extra += account.salary
                        account.receivedMonths = account.receivedMonths - 1
                        if(account.receivedMonths != account.durationInMonths)
                        {
                            account.status = "New"
                            account_database.child((account.accountId).toString()).child("status").setValue(account.status)
                        }
                        account_database.child((account.accountId).toString()).child("receivedMonths").setValue(account.receivedMonths)
                    }
                }
            }
        }
        user_database.child((userFount.id).toString()).child("extra").setValue(extra)
    }

    private fun addAccount()
    {
        val account_database = FirebaseDatabase.getInstance().reference.child("Accounts")

        val userpostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    val newUserList = ArrayList<Account>()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        val ok = userSnapshot.getValue(Account::class.java) ?: continue
                        newUserList.add(ok)
                    }
                    accountList = newUserList
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Database", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }

        account_database.addValueEventListener(userpostListener)
    }

    private fun addUserListSpinner()
    {
        val user_database = FirebaseDatabase.getInstance().reference.child("User")

        val userpostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    val newUserList = ArrayList<User>()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        val ok = userSnapshot.getValue(User::class.java) ?: continue
                        newUserList.add(ok)
                    }
                    userList = newUserList
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
    private var languages = ArrayList<String>()
    private fun updateSpinner(userList: ArrayList<User>) {
        val newlanguages = ArrayList<String>()
        for(user in userList)
        {
            newlanguages.add(user.name)
        }
        languages = newlanguages
        languages.sort()
        val spinner: Spinner = findViewById(R.id.editTextTextPersonName)
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

    private fun updateSpinnerTransaction(transactionList: ArrayList<Transaction>) {
        val newids = ArrayList<String>()
        newids.add("")
        for(user in transactionList)
        {
            newids.add(user.id.toString())
        }
        val spinner: Spinner = findViewById(R.id.transactionId)
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            newids
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }
}