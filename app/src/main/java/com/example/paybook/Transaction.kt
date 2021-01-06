package com.example.paybook

data class Transaction(
    val user: String = "",
    val price:Int = 0,
    val date:String = "",
    val id:Long=0
)