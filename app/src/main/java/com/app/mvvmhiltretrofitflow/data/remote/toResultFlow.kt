package com.app.mvvmhiltretrofitflow.data.remote

import android.content.Context
import com.app.mvvmhiltretrofitflow.utils.Constants
import com.app.mvvmhiltretrofitflow.utils.UiState
import com.app.mvvmhiltretrofitflow.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties


inline fun <reified T> toResultFlow(context: Context, crossinline call: suspend () -> Response<T>?): Flow<UiState<T>> {
    return flow {
        val isInternetConnected = Utils.hasInternetConnection(context)
        if (isInternetConnected) {
            emit(UiState.Loading)
            val c = call()
            c?.let { response ->
                try {
                    if (c.isSuccessful && c.body()!=null) {
                        c.body()?.let {
                            emit(UiState.Success(it))
                        }
                    } else {
                        val model = setResponseStatus<T>(T::class.java.getDeclaredConstructor().newInstance(), response.code().toString(), response.message())
                        emit(UiState.Error(model, response.message()))
                    }
                } catch (e: Exception) {
                    val model = setResponseStatus<T>(T::class.java.getDeclaredConstructor().newInstance(),
                        Constants.API_FAILED_CODE, e.message)
                    emit(UiState.Error(model, e.toString()))
                }
            }
        } else {
            val model = setResponseStatus<T>(T::class.java.getDeclaredConstructor().newInstance(),
                Constants.API_INTERNET_CODE, Constants.API_INTERNET_MESSAGE
            )
            emit(UiState.Error(model, Constants.API_INTERNET_MESSAGE))
        }
    }.flowOn(Dispatchers.IO)
}

inline fun <reified T> setResponseStatus(instance: T?, errorCode: String?, message: String?):T? {
    return try {
        instance?.let {
            val properties = it::class.memberProperties
            for (property in properties) {
                if (property is KMutableProperty<*>) {
                    when (property.name) {
                        "ErrorCode" -> (property as KMutableProperty<*>).setter.call(instance, errorCode)
                        "Message" -> (property as KMutableProperty<*>).setter.call(instance, message)
                    }
                }
            }
        }
        instance
    } catch (e: Exception) {
        null
    }
}