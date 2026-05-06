package ci.nsu.moble.main

import android.app.Application
import ci.nsu.moble.main.data.db.CatDatabase
import ci.nsu.moble.main.data.repository.CatRepository

class CatApplication : Application() {
    val database by lazy { CatDatabase.getDatabase(this) }
    val repository by lazy { CatRepository(database.catDao()) }
}