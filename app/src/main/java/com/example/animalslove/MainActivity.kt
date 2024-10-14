package com.example.animalslove

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.animalslove.ui.theme.AnimalsloveTheme
import com.example.animalslove.ui.theme.screens.HomeScreen
import com.example.animalslove.ui.theme.screens.LoginScreen
import com.example.animalslove.ui.theme.screens.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            AnimalsloveTheme  {
                navController = rememberNavController()
                AppNavigator(navController)
            }
        }
    }

    @Composable
    fun AppNavigator(navController: NavHostController) {
        NavHost(navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onEmailLogin = { email, password -> signInWithEmail(email, password) },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegister = { email, password, petExperience, interests, services ->
                        registerWithEmail(email, password, petExperience, interests, services)
                    }
                )
            }
            composable("home") {
                HomeScreen()
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(
                        baseContext,
                        "Inicio de sesión fallido: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun registerWithEmail(
        email: String,
        password: String,
        petExperience: String,
        interests: String,
        services: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Guardar información adicional en Firestore
                        val userInfo = hashMapOf(
                            "email" to email,
                            "petExperience" to petExperience,
                            "interests" to interests,
                            "services" to services
                        )
                        db.collection("users").document(user.uid).set(userInfo)
                            .addOnSuccessListener {
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    baseContext,
                                    "Error al guardar la información: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(
                        baseContext,
                        "Registro fallido: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
