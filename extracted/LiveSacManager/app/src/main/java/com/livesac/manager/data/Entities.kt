package com.livesac.manager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.livesac.manager.domain.Bag
import com.livesac.manager.domain.Client

/** Une session de vente. */
@Entity(tableName = "lives")
data class LiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** Date choisie, en millisecondes UTC à minuit. */
    val dateMillis: Long
)

/** Un client d'un Live. `nameKey` = nom en minuscules, pour détecter les doublons. */
@Entity(
    tableName = "clients",
    foreignKeys = [
        ForeignKey(
            entity = LiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["liveId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("liveId"), Index(value = ["liveId", "nameKey"])]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val liveId: Long,
    val name: String,
    val nameKey: String
)

/** Un sac acheté par un client, avec son code JP et son prix en Ariary. */
@Entity(
    tableName = "bags",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clientId")]
)
data class BagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val jpCode: String,
    val price: Long
)

fun ClientEntity.toModel() = Client(id = id, name = name)

fun BagEntity.toModel() = Bag(id = id, clientId = clientId, jpCode = jpCode, price = price)
