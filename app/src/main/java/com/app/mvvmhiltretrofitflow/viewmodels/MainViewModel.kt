package com.app.mvvmhiltretrofitflow.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.app.mvvmhiltretrofitflow.data.ApiResponseData
import com.app.mvvmhiltretrofitflow.data.Post
import com.app.mvvmhiltretrofitflow.data.Repository
import com.app.mvvmhiltretrofitflow.utils.UiState
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository, application: Application): BaseViewModel(application) {

    val _uiStateApiResponse = MutableStateFlow<UiState<ApiResponseData>>(UiState.Loading)
    val uiStateApiResponse: StateFlow<UiState<ApiResponseData>> = _uiStateApiResponse

    val _uiStatePostList = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val uiStatePostList: StateFlow<UiState<List<Post>>> = _uiStatePostList

    fun getProductsList(jsonObject: JsonObject) = viewModelScope.launch {
        repository.getProductList(context, jsonObject).collect{
            when (it) {
                is UiState.Success -> {
                    _uiStateApiResponse.value = UiState.Success(it.data)
                }
                is UiState.Loading -> {
                    _uiStateApiResponse.value = UiState.Loading
                }
                is UiState.Error -> {
                    //Handle Error
                    _uiStateApiResponse.value = UiState.Error(it.data,it.message)
                }
            }
        }
    }

    fun getPostsList() = viewModelScope.launch {
        repository.getPostList(context).collect {
            when (it) {
                is UiState.Success -> {
                    _uiStatePostList.value = UiState.Success(it.data)
                }
                is UiState.Loading -> {
                    _uiStatePostList.value = UiState.Loading
                }
                is UiState.Error -> {
                    //Handle Error
                    _uiStatePostList.value = UiState.Error(it.data,it.message)
                }
            }
        }
    }
}