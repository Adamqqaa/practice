package ci.nsu.moble.main.data.repository

import ci.nsu.moble.main.data.db.CatDao
import ci.nsu.moble.main.data.db.CatEntity
import kotlinx.coroutines.flow.Flow

class CatRepository(private val catDao: CatDao) {
    fun getAllCatsFlow(): Flow<List<CatEntity>> = catDao.getAllCatsFlow()

    suspend fun getAllCats(): List<CatEntity> = catDao.getAllCats()

    suspend fun insertCat(cat: CatEntity): Long = catDao.insertCat(cat)

    suspend fun deleteCat(cat: CatEntity) = catDao.deleteCat(cat)

    suspend fun deleteCatsByIds(ids: List<Long>) = catDao.deleteCatsByIds(ids)
}