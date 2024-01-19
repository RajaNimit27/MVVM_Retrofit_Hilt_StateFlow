package com.app.mvvmhiltretrofitflow.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.app.mvvmhiltretrofitflow.R
import com.app.mvvmhiltretrofitflow.data.ApiResponseData
import com.app.mvvmhiltretrofitflow.data.ApiResultHandler
import com.app.mvvmhiltretrofitflow.databinding.ActivityProductListBinding
import com.app.mvvmhiltretrofitflow.viewmodels.MainViewModel
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProductListActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    lateinit var activityMainBinding: ActivityProductListBinding
    lateinit var productListAdapter: ProductListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding =
            DataBindingUtil.setContentView(this@ProductListActivity, R.layout.activity_product_list)
        init()
        getProducts()
    }

    private fun init() {
        try {
            productListAdapter = ProductListAdapter()
            activityMainBinding.list.apply { adapter = productListAdapter }
            activityMainBinding.swipeRefreshLayout.setOnRefreshListener { getProducts() }
        } catch (e: Exception) {
            e.stackTrace
        }
    }


    private fun getProducts() {
        try {
            /*create your json for post request*/
            var jsonObject = JsonObject().apply {
            }
            mainViewModel.getProductsList(jsonObject)
            lifecycleScope.launch {
                mainViewModel.uiStateApiResponse.collect{
                    val apiResultHandler = ApiResultHandler<ApiResponseData>(this@ProductListActivity,
                        onLoading = {
                            activityMainBinding.progress.visibility = View.VISIBLE
                        },
                        onSuccess = { data ->
                            activityMainBinding.progress.visibility = View.GONE
                            data?.Data?.marketList?.let { productListAdapter.setProducts(it) }
                            activityMainBinding.swipeRefreshLayout.isRefreshing = false
                        },
                        onFailure = {
                            activityMainBinding.progress.visibility = View.GONE
                        })
                    apiResultHandler.handleApiResult(it)
                }
            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }

}