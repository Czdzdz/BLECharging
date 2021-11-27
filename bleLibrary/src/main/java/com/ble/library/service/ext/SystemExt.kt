package com.ble.library.service.ext

import android.content.Context
import android.os.Looper
import android.widget.Toast

fun Context.showToast(msg: String?) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}