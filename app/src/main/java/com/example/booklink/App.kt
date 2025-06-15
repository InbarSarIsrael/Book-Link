package com.example.booklink

import android.app.Application
import com.example.booklink.utilities.ImageLoader

class App:Application() {
    override fun onCreate() {
        super.onCreate()
        ImageLoader.init(this)
    }
}