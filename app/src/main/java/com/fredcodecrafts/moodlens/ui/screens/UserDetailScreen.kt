package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.User
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    database: AppDatabase
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }

    // Fetch the first user from database
    LaunchedEffect(Unit) {
        scope.launch {
            val users = database.userDao().getAllUsers()
            if (users.isNotEmpty()) {
                user = users.first()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (user != null) {
                Text(
                    text = "Google ID: ${user!!.googleId}",
                    fontSize = 20.sp
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
