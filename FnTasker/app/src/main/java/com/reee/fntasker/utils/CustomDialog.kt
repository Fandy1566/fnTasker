package com.reee.fntasker.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater

class CustomDialog(context: Context, private val layoutResId: Int) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(layoutResId, null)
        setContentView(view)
    }
}