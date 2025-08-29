package com.example.firechat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.example.firechat.data.model.User
import com.example.firechat.presentation.screen.auth.LoginScreen
import com.example.firechat.presentation.screen.auth.RegisterScreen
import com.example.firechat.presentation.screen.chat.ChatListScreen
import com.example.firechat.presentation.screen.chat.ChatScreen
import com.example.firechat.presentation.screen.user.UserListScreen
import com.example.firechat.presentation.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function that defines the navigation graph for the FireChat application.
 * It uses a [NavHostController] to manage navigation between different screens.
 * The starting destination is determined by the user's login status, obtained from the [AuthViewModel].
 *
 * The navigation graph includes the following screens:
 * - **LoginScreen**: Allows users to log in. Navigates to "chat_list" on success.
 * - **RegisterScreen**: Allows new users to register. Navigates to "chat_list" on success.
 * - **ChatListScreen**: Displays a list of active chats. Allows navigation to individual chat screens,
 *   the user list screen, and handles logout.
 * - **UserListScreen**: Displays a list of users to start a new chat with. Navigates to a new chat
 *   screen when a user is selected.
 * - **ChatScreen**: Displays the messages in a specific chat and allows sending new messages.
 *   Requires `chatId`, `userId`, `userName`, `userAvatar`, and `isOnline` as navigation arguments.
 *
 * @param navController The [NavHostController] to manage navigation. Defaults to a remembered NavController.
 * @param authViewModel The [AuthViewModel] to observe the user's login status. Defaults to a Koin-injected instance.
 */
@Composable
fun FireChatNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle(false)
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "chat_list" else "login"
    ) {
        // Auth screens
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("chat_list") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("chat_list") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        // Chat screens
        composable("chat_list") {
            ChatListScreen(
                onChatClick = { chatId, otherUser ->
                    val userName = URLEncoder.encode(otherUser.displayName.ifEmpty { "User" }, StandardCharsets.UTF_8.toString())
                    val userAvatar = URLEncoder.encode(otherUser.avatarUrl.ifEmpty { "none" }, StandardCharsets.UTF_8.toString())
                    navController.navigate("chat/$chatId/${otherUser.id}/$userName/$userAvatar/${otherUser.isOnline}")
                },
                onStartChatClick = {
                    navController.navigate("user_list")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("chat_list") { inclusive = true }
                    }
                }
            )
        }
        
        composable("user_list") {
            UserListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = { otherUser ->
                    // Create a new chat with this user and navigate to it
                    val userName = URLEncoder.encode(otherUser.displayName.ifEmpty { "User" }, StandardCharsets.UTF_8.toString())
                    val userAvatar = URLEncoder.encode(otherUser.avatarUrl.ifEmpty { "none" }, StandardCharsets.UTF_8.toString())
                    navController.navigate("chat/new_${otherUser.id}/${otherUser.id}/$userName/$userAvatar/${otherUser.isOnline}")
                }
            )
        }
        
        composable(
            "chat/{chatId}/{userId}/{userName}/{userAvatar}/{isOnline}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType },
                navArgument("userAvatar") { type = NavType.StringType },
                navArgument("isOnline") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val userName = URLDecoder.decode(backStackEntry.arguments?.getString("userName") ?: "User", StandardCharsets.UTF_8.toString())
            val userAvatar = URLDecoder.decode(backStackEntry.arguments?.getString("userAvatar") ?: "none", StandardCharsets.UTF_8.toString())
            val isOnline = backStackEntry.arguments?.getBoolean("isOnline") ?: false
            
            val otherUser = User(
                id = userId,
                displayName = userName,
                avatarUrl = if (userAvatar == "none") "" else userAvatar,
                isOnline = isOnline
            )
            
            ChatScreen(
                chatId = chatId,
                otherUser = otherUser,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}