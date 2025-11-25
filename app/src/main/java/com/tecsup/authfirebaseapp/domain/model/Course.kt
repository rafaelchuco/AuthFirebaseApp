package com.tecsup.authfirebaseapp.domain.model

data class Course(
    val id: String = "",
    val name: String = "",
    val teacher: String = "",
    val credits: Int = 0,
    val userId: String = ""
)
