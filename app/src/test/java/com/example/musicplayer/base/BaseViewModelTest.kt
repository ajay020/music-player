package com.example.musicplayer.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    protected val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun baseSetUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun baseTearDown() {
        Dispatchers.resetMain()
    }
}
