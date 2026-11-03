package com.livesac.manager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.livesac.manager.data.ClientDetail
import com.livesac.manager.data.LiveDetail
import com.livesac.manager.data.LiveSummary
import com.livesac.manager.data.NewBag
import com.livesac.manager.data.Repository
import com.livesac.manager.domain.Bag
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Fabrique simple pour créer un ViewModel avec ses paramètres. */
class VmFactory(private val builder: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = builder() as T
}

/** Écran 1 : liste des Lives. `null` = chargement en cours. */
class LivesViewModel(private val repo: Repository) : ViewModel() {

    val lives: StateFlow<List<LiveSummary>?> =
        repo.observeLiveSummaries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun createLive(title: String, dateMillis: Long, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.createLive(title, dateMillis)
            onCreated(id)
        }
    }
}

/** Écran 2 : récapitulatif d'un Live. */
class LiveViewModel(repo: Repository, liveId: Long) : ViewModel() {

    val live: StateFlow<LiveDetail?> =
        repo.observeLive(liveId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** Une ligne « sac » du formulaire (code JP + prix saisi en texte). */
class BagInput(val key: Int) {
    var code by mutableStateOf("")
    var price by mutableStateOf("")
}

/** Écran 3 : formulaire « Ajouter un JP ». L'état du formulaire survit à la rotation de l'écran. */
class AddJpViewModel(private val repo: Repository, private val liveId: Long) : ViewModel() {

    var name by mutableStateOf("")
    val rows = mutableStateListOf(BagInput(0))
    private var nextKey = 1

    fun addRow() {
        rows.add(BagInput(nextKey++))
    }

    fun removeRow(row: BagInput) {
        if (rows.size > 1) rows.remove(row)
    }

    /** Renvoie (via onResult) le nom du client déjà présent dans ce Live, ou null. */
    fun checkDuplicate(onResult: (String?) -> Unit) {
        val currentName = name
        viewModelScope.launch {
            onResult(repo.findClientName(liveId, currentName))
        }
    }

    fun save(onSaved: () -> Unit) {
        val currentName = name
        val bags = rows.map { NewBag(it.code.trim(), it.price.toLongOrNull() ?: 0L) }
        viewModelScope.launch {
            repo.addBags(liveId, currentName, bags)
            onSaved()
        }
    }
}

/** Écran 4 : détail d'un client. */
class ClientViewModel(private val repo: Repository, private val clientId: Long) : ViewModel() {

    val client: StateFlow<ClientDetail?> =
        repo.observeClient(clientId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addBag(code: String, price: Long) {
        viewModelScope.launch { repo.addBag(clientId, code, price) }
    }

    fun updateBag(bag: Bag, code: String, price: Long) {
        viewModelScope.launch { repo.updateBag(bag.copy(jpCode = code, price = price)) }
    }

    /** Supprime un sac ; `onClientRemoved` est appelé si la fiche client a disparu (dernier sac). */
    fun deleteBag(bagId: Long, onClientRemoved: () -> Unit) {
        viewModelScope.launch {
            if (repo.deleteBag(bagId)) onClientRemoved()
        }
    }
}
