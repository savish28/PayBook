package com.example.paybook

data class Account(
    val accountId:Long = 0,
    val username:String = "",
    val salary:Int = 0,
    val createdOn:String = "",
    val durationInMonths:Int = 0,
    var receivedMonths:Int = 0,
    var status:String = "",
    val accountNumber:Long = 0

)