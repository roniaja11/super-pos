package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.database.AppDatabase
import com.example.data.PosRepository
import com.example.ui.SuperPosApp
import com.example.ui.SuperPosViewModel
import com.example.ui.SuperPosViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = PosRepository(database.posDao())
        val factory = SuperPosViewModelFactory(repository)

        setContent {
            val posViewModel: SuperPosViewModel = viewModel(factory = factory)
            SuperPosApp(viewModel = posViewModel)
        }
    }
}
