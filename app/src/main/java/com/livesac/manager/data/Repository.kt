package com.livesac.manager.data

import androidx.room.withTransaction
import com.livesac.manager.domain.Bag
import com.livesac.manager.domain.ClientSummary
import com.livesac.manager.domain.LiveStats
import com.livesac.manager.domain.buildClientSummaries
import com.livesac.manager.domain.computeStats
import com.livesac.manager.domain.normalizeName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Carte d'un Live dans la liste d'accueil. */
data class LiveSummary(val id: Long, val title: String, val dateMillis: Long, val stats: LiveStats)

/** Récapitulatif d'un Live : chiffres + fiches clients. */
data class LiveDetail(
    val id: Long,
    val title: String,
    val dateMillis: Long,
    val stats: LiveStats,
    val clients: List<ClientSummary>
)

/** Détail d'un client : ses sacs et son total. */
data class ClientDetail(
    val id: Long,
    val liveId: Long,
    val name: String,
    val jpCodes: List<String>,
    val bags: List<Bag>,
    val bagCount: Int,
    val total: Long
)

/** Sac saisi dans le formulaire (pas encore enregistré). */
data class NewBag(val jpCode: String, val price: Long)

class Repository(private val db: AppDatabase) {

    private val dao = db.dao()

    // ---------------- Lecture (les écrans se mettent à jour tout seuls) ----------------

    fun observeLiveSummaries(): Flow<List<LiveSummary>> =
        combine(dao.observeLives(), dao.observeAllClients(), dao.observeAllBags()) { lives, clients, bags ->
            val clientsByLive = clients.groupBy { it.liveId }
            val bagsByClient = bags.groupBy { it.clientId }
            lives.map { live ->
                val liveClients = clientsByLive[live.id].orEmpty().map { it.toModel() }
                val liveBags = liveClients.flatMap { c -> bagsByClient[c.id].orEmpty().map { it.toModel() } }
                val summaries = buildClientSummaries(liveClients, liveBags)
                LiveSummary(live.id, live.title, live.dateMillis, computeStats(summaries))
            }
        }

    fun observeLive(liveId: Long): Flow<LiveDetail?> =
        combine(
            dao.observeLive(liveId),
            dao.observeClientsOfLive(liveId),
            dao.observeBagsOfLive(liveId)
        ) { live, clients, bags ->
            if (live == null) {
                null
            } else {
                val summaries = buildClientSummaries(clients.map { it.toModel() }, bags.map { it.toModel() })
                LiveDetail(live.id, live.title, live.dateMillis, computeStats(summaries), summaries)
            }
        }

    fun observeClient(clientId: Long): Flow<ClientDetail?> =
        combine(dao.observeClient(clientId), dao.observeBagsOfClient(clientId)) { client, bags ->
            if (client == null) {
                null
            } else {
                val models = bags.map { it.toModel() }
                val summary = buildClientSummaries(listOf(client.toModel()), models).first()
                ClientDetail(
                    id = client.id,
                    liveId = client.liveId,
                    name = client.name,
                    jpCodes = summary.jpCodes,
                    bags = models,
                    bagCount = summary.bagCount,
                    total = summary.total
                )
            }
        }

    // ---------------- Écriture ----------------

    suspend fun createLive(title: String, dateMillis: Long): Long =
        dao.insertLive(LiveEntity(title = title.trim(), dateMillis = dateMillis))

    /** Nom (orthographe d'origine) du client déjà présent dans ce Live, ou null. */
    suspend fun findClientName(liveId: Long, name: String): String? =
        dao.findClient(liveId, normalizeName(name))?.name

    /**
     * Ajoute des sacs à un client du Live. Si le nom existe déjà (sans tenir compte
     * des majuscules), les sacs sont ajoutés à la fiche existante : jamais de doublon.
     */
    suspend fun addBags(liveId: Long, name: String, bags: List<NewBag>): Long =
        db.withTransaction {
            val key = normalizeName(name)
            val existing = dao.findClient(liveId, key)
            val clientId = existing?.id
                ?: dao.insertClient(ClientEntity(liveId = liveId, name = name.trim(), nameKey = key))
            bags.forEach { bag ->
                dao.insertBag(BagEntity(clientId = clientId, jpCode = bag.jpCode.trim(), price = bag.price))
            }
            clientId
        }

    suspend fun addBag(clientId: Long, jpCode: String, price: Long) {
        dao.insertBag(BagEntity(clientId = clientId, jpCode = jpCode.trim(), price = price))
    }

    suspend fun updateBag(bag: Bag) {
        dao.updateBag(
            BagEntity(id = bag.id, clientId = bag.clientId, jpCode = bag.jpCode.trim(), price = bag.price)
        )
    }

    /** Supprime un sac. Si c'était le dernier du client, la fiche client est supprimée aussi (renvoie true). */
    suspend fun deleteBag(bagId: Long): Boolean =
        db.withTransaction {
            val bag = dao.getBag(bagId)
            if (bag == null) {
                false
            } else {
                dao.deleteBag(bagId)
                if (dao.countBags(bag.clientId) == 0) {
                    dao.deleteClient(bag.clientId)
                    true
                } else {
                    false
                }
            }
        }
}
