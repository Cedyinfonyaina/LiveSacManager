package com.livesac.manager

import android.app.Application
import com.livesac.manager.data.AppDatabase
import com.livesac.manager.data.Repository

class LiveSacApp : Application() {
    val repository: Repository by lazy { Repository(AppDatabase.get(this)) }
}
