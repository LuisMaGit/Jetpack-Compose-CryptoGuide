package com.luisma.cryptocurrency.ui.views.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.luisma.cryptocurrency.domain.app.repositories.AppThemes
import com.luisma.cryptocurrency.domain.app.repositories.NavigationRepo
import com.luisma.cryptocurrency.domain.app.repositories.ThemeRepo
import com.luisma.cryptocurrency.domain.data.models.cryptoModels.Crypto
import com.luisma.cryptocurrency.domain.data.models.cryptoModels.CryptoDomain
import com.luisma.cryptocurrency.domain.data.repositories.cryptoRepo.ICryptoRepo
import com.luisma.cryptocurrency.domain.data.utils.models.ResponseDomain
import com.luisma.cryptocurrency.router.Routes
import com.luisma.cryptocurrency.ui.BaseViewModel
import com.luisma.cryptocurrency.ui.SimpleViewModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeDataState(
    val cryptos: List<Crypto>,
    val lastUpdate: String,
    var filteredCryptos: List<Crypto> = listOf(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cryptoRepo: ICryptoRepo,
    private val themeRepo: ThemeRepo,
    private val navigationRepo: NavigationRepo,
) : BaseViewModel<HomeDataState>() {

    init {
        resolveCryptos(
            fetchCallBack = {
                cryptoRepo.getCyptos()
            }
        )
    }

    //STATES
    private lateinit var _homeDataState: HomeDataState
    fun getCryptos(): List<Crypto> {
        return if (_searchValue.value.isEmpty()) _homeDataState.cryptos else _homeDataState.filteredCryptos
    }

    fun getLastUpdate(): String = _homeDataState.lastUpdate

    private val _searchValue = mutableStateOf("")
    val searchValue: State<String> = _searchValue
    fun setSearch(search: String) {
        _searchValue.value = search
        findCrypto()
    }

    fun isDarkTheme() = themeRepo.isDarkMode

    private fun findCrypto() {
        val filtered = mutableListOf<Crypto>()
        _homeDataState.cryptos.forEach {
            if (_searchValue.value.lowercase(Locale.getDefault()) in
                it.name.lowercase(Locale.getDefault())
            ) {
                filtered.add(it)
            }
        }
        _homeDataState.filteredCryptos = filtered
    }

    private fun resolveCryptos(
        fetchCallBack: suspend () -> ResponseDomain<CryptoDomain>,
    ) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = fetchCallBack()) {
            is ResponseDomain.Success -> {
                setCryptos(result.domain?.cryptos, result.domain?.lastUpdate)
            }
            is ResponseDomain.Error -> {

                if (result.domain != null) {
                    setCryptos(
                        result.domain.cryptos,
                        result.domain.lastUpdate
                    )
                    return@launch
                }

                setState(SimpleViewModelState.Error(result.message))
            }
        }
    }

    private fun setCryptos(cryptos: List<Crypto>?, lastUpdate: String?) {
        _homeDataState = HomeDataState(cryptos!!, lastUpdate ?: "")
        setState(SimpleViewModelState.Iddle(_homeDataState))
    }


    fun setDarkMode(darkMode: Boolean) {
        if (darkMode) {
            viewModelScope.launch {
                themeRepo.setTheme(AppThemes.Light)
            }
            return
        }

        viewModelScope.launch {
            themeRepo.setTheme(AppThemes.Dark)
        }
    }

    fun goToDetails(cryptoId: String) {
        viewModelScope.launch {
            navigationRepo.goTo(
                Routes.CryptoDetails.goToCrypto(cryptoId)
            )
        }
    }

    fun tryAgain() {
        setState(SimpleViewModelState.Loading())
        resolveCryptos(
            fetchCallBack = {
                cryptoRepo.getCyptos()
            }
        )
    }

    fun onTapRefresh() {
        setState(SimpleViewModelState.Loading())
        resolveCryptos(
            fetchCallBack = {
                cryptoRepo.fetchAndCacheCryptos()
            }
        )
    }
}