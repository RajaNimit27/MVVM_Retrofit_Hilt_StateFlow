package com.app.mvvmhiltretrofitflow.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.app.mvvmhiltretrofitflow.R
import com.app.mvvmhiltretrofitflow.data.ApiResultHandler
import com.app.mvvmhiltretrofitflow.data.Post
import com.app.mvvmhiltretrofitflow.databinding.ActivityPostListBinding
import com.app.mvvmhiltretrofitflow.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PostListActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    lateinit var activityMainBinding: ActivityPostListBinding
    lateinit var postListAdapter: PostListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this@PostListActivity, R.layout.activity_post_list)
        init()
        getPosts()
        observePostData()
    }

    private fun init() {
        try {
            postListAdapter = PostListAdapter()
            activityMainBinding.list.apply { adapter= postListAdapter }
            activityMainBinding.swipeRefreshLayout.setOnRefreshListener { getPosts() }
        } catch (e: Exception) {
            e.stackTrace
        }
    }


    private fun observePostData() {

    }

    private fun getPosts() {
        mainViewModel.getPostsList()
        try {
            lifecycleScope.launch {
                mainViewModel.uiStatePostList.collect{ response ->
                    val apiResultHandler = ApiResultHandler<List<Post>>(this@PostListActivity,
                        onLoading = {
                            activityMainBinding.progress.visibility = View.VISIBLE
                        },
                        onSuccess = { data ->
                            activityMainBinding.progress.visibility = View.GONE
                            data?.let { postListAdapter.setPosts(it) }
                            activityMainBinding.swipeRefreshLayout.isRefreshing = false
                        },
                        onFailure = {
                            activityMainBinding.progress.visibility = View.GONE
                        })
                    apiResultHandler.handleApiResult(response)
                }
            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}