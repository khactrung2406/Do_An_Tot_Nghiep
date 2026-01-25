package com.example.app_yolo11

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_yolo11.ui.Screens.Camera.CameraScreen
import com.example.app_yolo11.ui.Screens.Camera.CameraViewModel
import com.example.app_yolo11.ui.Screens.ChatBot.ChatBotScreen
import com.example.app_yolo11.ui.Screens.Community.PostDetailScreen
import com.example.app_yolo11.ui.Screens.Create.CreatePostScreen
import com.example.app_yolo11.ui.Screens.Create.CreatePostViewModel
import com.example.app_yolo11.ui.Screens.EditPost.EditPostScreen
import com.example.app_yolo11.ui.Screens.ForgotPass.ForgotPassScreen
import com.example.app_yolo11.ui.Screens.ForgotPass.ForgotPassViewModel
import com.example.app_yolo11.ui.Screens.History.HistoryScreen
import com.example.app_yolo11.ui.Screens.History.HistoryViewModel
import com.example.app_yolo11.ui.Screens.Home.HomeScreenMain
import com.example.app_yolo11.ui.Screens.Home.HomeViewModel
import com.example.app_yolo11.ui.Screens.Information.DetailScreen
import com.example.app_yolo11.ui.Screens.Information.InformationViewModel
import com.example.app_yolo11.ui.Screens.Login.LoginScreen
import com.example.app_yolo11.ui.Screens.Login.LoginViewModel
import com.example.app_yolo11.ui.Screens.ManagePosts.ManagePostsScreen
import com.example.app_yolo11.ui.Screens.Profile.ProfileScreen
import com.example.app_yolo11.ui.Screens.Profile.ProfileViewModel
import com.example.app_yolo11.ui.Screens.Search.SearchScreen
import com.example.app_yolo11.ui.Screens.Search.SearchViewModel
import com.example.app_yolo11.ui.Screens.SignUp.SignUpScreen
import com.example.app_yolo11.ui.Screens.SignUp.SignupViewModel
import com.example.app_yolo11.ui.Screens.Collection.CollectionScreen
import com.example.app_yolo11.ui.Screens.Collection.AddCollectionScreen
import com.example.app_yolo11.ui.Screens.Collection.CollectionDetailScreen
import com.example.app_yolo11.ui.Screens.Collection.EditCollectionScreen


sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Signup : Screens("signup")
    object HomeScreen : Screens("home")
    object ForgotPass : Screens("forgot_password")
    object CameraScreen : Screens("camera")
    object Profile : Screens("profile")
    object Detail : Screens("detail_screen")
    object Search : Screens("search")
    object History : Screens("history")
    object CreatePost : Screens("create_post")
    object PostDetail : Screens("post_detail")
    object ManagePosts : Screens("manage_posts")
    object EditPost : Screens("edit_post")

    object Collection : Screens("collection")
    object AddCollection : Screens("add_collection")

    object CollectionDetail : Screens("detail_collection")

    object EditCollection : Screens("edit_collection")

    object ChatBot : Screens("chat_bot")
}

@Composable
fun Navigation(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {


        composable(Screens.Login.route) {
            val viewModel = hiltViewModel<LoginViewModel>()
            LoginScreen(navController, viewModel)
        }

        composable(route = Screens.Signup.route) {
            val viewModel = hiltViewModel<SignupViewModel>()
            SignUpScreen(navController, viewModel)
        }

        composable(Screens.ForgotPass.route) {
            val viewModel = hiltViewModel<ForgotPassViewModel>()
            ForgotPassScreen(navController, viewModel)
        }


        composable(Screens.HomeScreen.route) {
            val viewModel = hiltViewModel<HomeViewModel>()
            HomeScreenMain(navController, viewModel)
        }

        composable(Screens.CameraScreen.route) {
            val viewModel = hiltViewModel<CameraViewModel>()
            CameraScreen(navController, viewModel)
        }

        composable(Screens.Profile.route) {
            val viewModel = hiltViewModel<ProfileViewModel>()
            ProfileScreen(navController, viewModel)
        }

        composable(Screens.Search.route) {
            val viewModel = hiltViewModel<SearchViewModel>()
            SearchScreen(navController, viewModel)
        }

        composable(Screens.History.route) {
            val viewModel = hiltViewModel<HistoryViewModel>()
            HistoryScreen(navController, viewModel)
        }


        composable(
            route = "detail_screen/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val viewModel: InformationViewModel = hiltViewModel()

            LaunchedEffect(id) {
                if (id.isNotEmpty()) {
                    viewModel.loadSnailById(id)
                }
            }

            val snail by viewModel.snail.collectAsState()
            val loading by viewModel.loading.collectAsState()
            val error by viewModel.error.collectAsState()

            Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                when {
                    loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    snail != null -> {
                        DetailScreen(
                            navController = navController,
                            snail = snail!!,
                            viewModel = viewModel
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Không tìm thấy dữ liệu cho ID: $id", color = Color.Red)
                            Text(text = error ?: "Lỗi không xác định", color = Color.Gray)
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Quay lại")
                            }
                        }
                    }
                }
            }
        }

        composable(Screens.CreatePost.route) {
            val viewModel = hiltViewModel<CreatePostViewModel>()
            CreatePostScreen(navController, viewModel)
        }

        composable(
            route = "post_detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")!!
            PostDetailScreen(navController = navController, postId = postId)
        }

        composable("manage_posts") {
            ManagePostsScreen(navController = navController)
        }

        composable(
            route = "edit_post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                EditPostScreen(navController = navController, postId = postId)
            }
        }

        composable(Screens.Collection.route) {
            CollectionScreen(navController = navController)
        }

        composable(Screens.AddCollection.route) {
            AddCollectionScreen(navController = navController)
        }

        composable(
            route = "detail_collection/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            if (itemId != null) {
                CollectionDetailScreen(itemId = itemId, navController = navController)
            }
        }

        composable(
            route = "edit_collection/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            if (itemId != null) {
                EditCollectionScreen(itemId = itemId, navController = navController)
            }
        }
        composable(Screens.ChatBot.route) {
            ChatBotScreen(navController)
        }
    }
}