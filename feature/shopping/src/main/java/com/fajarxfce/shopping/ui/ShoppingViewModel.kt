package com.fajarxfce.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fajarxfce.core.domain.usecase.product.GetAllProductUseCase
import com.fajarxfce.core.domain.usecase.product.GetPagingProductUseCase
import com.fajarxfce.core.model.data.product.Product
import com.fajarxfce.core.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val getPagingProductUseCase: GetPagingProductUseCase
) : ViewModel() {

    private val _shoppingUiState = MutableStateFlow<ShoppingUiState<PagingData<Product>>>(ShoppingUiState.Loading)
    val shoppingUiState: StateFlow<ShoppingUiState<PagingData<Product>>> = _shoppingUiState.asStateFlow()

    // Keep reference to the current flow for refreshing purposes
    private var currentProductFlow: Flow<PagingData<Product>>? = null
    private var currentOrderBy: String = "products.id"
    private var currentAscending: Boolean = true

    init {
        loadProducts()
    }
    private fun loadProducts(orderBy: String = currentOrderBy, ascending: Boolean = currentAscending) {
        viewModelScope.launch {
            _shoppingUiState.update { ShoppingUiState.Loading }

            try {
                currentOrderBy = orderBy
                currentAscending = ascending

                // Create and cache the paging flow
                val pagingFlow = getPagingProductUseCase(orderBy, ascending)
                    .cachedIn(viewModelScope)

                currentProductFlow = pagingFlow

                // Emit success with the initial empty PagingData
                // The UI will collect the actual paging data from the flow
                _shoppingUiState.update { ShoppingUiState.Success(PagingData.empty()) }

                // Collect the first value to update the state with actual data
                pagingFlow.collect { pagingData ->
                    _shoppingUiState.update { ShoppingUiState.Success(pagingData) }
                }
            } catch (e: Exception) {
                _shoppingUiState.update { ShoppingUiState.Error(e) }
            }
        }
    }

    fun getProductPagingFlow(): Flow<PagingData<Product>>? {
        return currentProductFlow
    }

    fun refreshProducts() {
        loadProducts()
    }

    fun sortProducts(orderBy: String, ascending: Boolean) {
        if (orderBy != currentOrderBy || ascending != currentAscending) {
            loadProducts(orderBy, ascending)
        }
    }
}