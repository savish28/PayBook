package com.example.paybook
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.paybook.TransactionView
import com.example.paybook.TransactionView2
import com.example.paybook.UserCreate


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<Button>(R.id.transaction_button).setOnClickListener {
            intent = Intent(applicationContext, TransactionView2::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.transaction_button2).setOnClickListener {
            intent = Intent(applicationContext, TransactionView::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.createuser).setOnClickListener {
            intent = Intent(applicationContext, UserCreate::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.viewuser).setOnClickListener {
            intent = Intent(applicationContext, UserView::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.addtransaction).setOnClickListener {
            intent = Intent(applicationContext, TransactionCreate::class.java)
            startActivity(intent)
        }



    }



}