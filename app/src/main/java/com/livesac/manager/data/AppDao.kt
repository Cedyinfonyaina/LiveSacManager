package com.livesac.manager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ---- Lives ----
    @Insert
    suspend fun insertLive(live: LiveEntity): Long

    @Query("SELECT * FROM lives ORDER BY dateMillis DESC, id DESC")
    fun observeLives(): Flow<List<LiveEntity>>

    @Query("SELECT * FROM lives WHERE id = :id")
    fun observeLive(id: Long): Flow<LiveEntity?>

    // ---- Lecture globale (liste des Lives) ----
    @Query("SELECT * FROM clients")
    fun observeAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM bags")
    fun observeAllBags(): Flow<List<BagEntity>>

    // ---- Un Live ----
    @Query("SELECT * FROM clients WHERE liveId = :liveId")
    fun observeClientsOfLive(liveId: Long): Flow<List<ClientEntity>>

    @Query(
        "SELECT bags.* FROM bags " +
            "INNER JOIN clients ON bags.clientId = clients.id " +
            "WHERE clients.liveId = :liveId ORDER BY bags.id"
    )
    fun observeBagsOfLive(liveId: Long): Flow<List<BagEntity>>

    // ---- Un client ----
    @Query("SELECT * FROM clients WHERE id = :id")
    fun observeClient(id: Long): Flow<ClientEntity?>

    @Query("SELECT * FROM bags WHERE clientId = :clientId ORDER BY id")
    fun observeBagsOfClient(clientId: Long): Flow<List<BagEntity>>

    @Query("SELECT * FROM clients WHERE liveId = :liveId AND nameKey = :nameKey LIMIT 1")
    suspend fun findClient(liveId: Long, nameKey: String): ClientEntity?

    @Insert
    suspend fun insertClient(client: ClientEntity): Long

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClient(id: Long)

    // ---- Sacs ----
    @Insert
    suspend fun insertBag(bag: BagEntity): Long

    @Update
    suspend fun updateBag(bag: BagEntity)

    @Query("SELECT * FROM bags WHERE id = :id")
    suspend fun getBag(id: Long): BagEntity?

    @Query("DELETE FROM bags WHERE id = :id")
    suspend fun deleteBag(id: Long)

    @Query("SELECT COUNT(*) FROM bags WHERE clientId = :clientId")
    suspend fun countBags(clientId: Long): Int
}
