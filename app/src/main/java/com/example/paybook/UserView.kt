package com.example.paybook

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserView : AppCompatActivity() {
    var userList = ArrayList<User>()
    var AccountList = ArrayList<Account>()
    private var userFocus: String = ""
    private fun changeDate(date: String) : String{
        val strs = date.split(".").toTypedArray()
        return strs[2] + strs[1] + strs[0]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_view)
        addUserListSpinner()

        val userfocusSpinner = findViewById<Spinner>(R.id.user_view_name_select)

        userfocusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                userFocus = userfocusSpinner.getSelectedItem().toString()
                val userObj:User = fetUserByName(userFocus)
                findViewById<TextView>(R.id.user_view_name).text = userObj.name
                findViewById<TextView>(R.id.user_view_extra).text = userObj.extra.toString()
                findViewById<TextView>(R.id.user_view_total_salary).text = getTotalSararyUser(userObj.name).toString()
                var AccountList_user = userListonFocus(AccountList)
                val accountListview = findViewById<ListView>(R.id.account_list_view)
                AccountList_user = ArrayList(AccountList_user.sortedWith(compareBy { changeDate(it.createdOn) }))
                accountListview.adapter = UserView.CustomAdaptor(this@UserView, AccountList_user)
            }

        }
        val account_database = FirebaseDatabase.getInstance().reference.child("Accounts")

        val accountpostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists())
                {
                    val newAccount = ArrayList<Account>()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        println("start")
                        val ok = userSnapshot.getValue(Account::class.java) ?: continue
                        newAccount.add(ok)
                    }
                    AccountList = newAccount
                    println("DOne and dusted")
                    var AccountListFocus = userListonFocus(AccountList)
                    val accountListview = findViewById<ListView>(R.id.account_list_view)
                    AccountListFocus = ArrayList(AccountListFocus.sortedWith(compareBy { changeDate(it.createdOn) }))
                    accountListview.adapter = UserView.CustomAdaptor(this@UserView, AccountListFocus)
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

    private fun getTotalSararyUser(name: String): Int {
        var tot = 0
        for(account in AccountList)
        {
            if(account.username == name && account.status!= "closed")
            {
                tot += account.salary
            }
        }
        return tot
    }

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

    fun userListonFocus(userList: ArrayList<Account>):  ArrayList<Account> {
        val user_list_focus = ArrayList<Account>()
        for(user in userList)
        {
            if(user.username == userFocus || userFocus == "")
            {
                user_list_focus.add(user)
            }
        }
        return user_list_focus
    }

    private class CustomAdaptor(context: Context, arrayList: ArrayList<Account>): BaseAdapter(){

        private val mcontext: Context = context
        private var mlist: ArrayList<Account> = arrayList
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutinflator = LayoutInflater.from(mcontext)
            val rowMain = layoutinflator.inflate(R.layout.accout_item_row, parent, false)
            //rowMain.findViewById<TextView>(R.id.account_user_name).text = mlist[position].username
            rowMain.findViewById<TextView>(R.id.account_user_price).text = mlist[position].salary.toString()
            rowMain.findViewById<TextView>(R.id.account_user_date).text = mlist[position].createdOn
            rowMain.findViewById<TextView>(R.id.account_user_total_months).text = mlist[position].durationInMonths.toString()
            rowMain.findViewById<TextView>(R.id.account_user_recieved).text = mlist[position].receivedMonths.toString()
            rowMain.findViewById<TextView>(R.id.account_user_status).text = mlist[position].status
            rowMain.findViewById<TextView>(R.id.account_user_acNo).text = mlist[position].accountNumber.toString()
            rowMain.findViewById<TextView>(R.id.textViewrecieveDate).text = getDatesubmitted(mlist[position].createdOn,  mlist[position].receivedMonths)

            return rowMain
        }
        private fun getDatesubmitted(createdOn:String, receivedMonths:Int): String
        {
            val strs = createdOn.split(".").toTypedArray()
            var mon = strs[1].toInt() - 1
            var year = strs[2].toInt()
            mon += receivedMonths
            year += (mon/12).toInt()
            mon %= 12
            mon += 1

            mon -= 1
            if(mon == 0)
            {
                year-=1
                mon = 12
            }
            return mon.toString() + "/" + year.toString()

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
                if(dataSnapshot.exists())
                {
                    val userListnew = ArrayList<User>()
                    for(userSnapshot in dataSnapshot.getChildren()){
                        val ok = userSnapshot.getValue(User::class.java) ?: continue
                        userListnew.add(ok)
                    }
                    userList = userListnew
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