package com.example.firechat.di

import com.example.firechat.data.repository.AuthRepositoryImpl
import com.example.firechat.data.repository.ChatRepositoryImpl
import com.example.firechat.data.repository.MessageRepositoryImpl
import com.example.firechat.data.repository.UserRepositoryImpl
import com.example.firechat.domain.repository.AuthRepository
import com.example.firechat.domain.repository.ChatRepository
import com.example.firechat.domain.repository.MessageRepository
import com.example.firechat.domain.repository.UserRepository
import com.example.firechat.domain.usecase.auth.LoginUseCase
import com.example.firechat.domain.usecase.auth.LogoutUseCase
import com.example.firechat.domain.usecase.auth.RegisterUseCase
import com.example.firechat.domain.usecase.chat.GetChatsUseCase
import com.example.firechat.domain.usecase.chat.CreateChatUseCase
import com.example.firechat.domain.usecase.message.SendMessageUseCase
import com.example.firechat.domain.usecase.message.GetMessagesUseCase
import com.example.firechat.presentation.viewmodel.AuthViewModel
import com.example.firechat.presentation.viewmodel.ChatListViewModel
import com.example.firechat.presentation.viewmodel.ChatViewModel
import com.example.firechat.presentation.viewmodel.UserListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<ChatRepository> { ChatRepositoryImpl(get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    
    // Use Cases - Auth
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get()) }
    
    // Use Cases - Chat
    factory { GetChatsUseCase(get()) }
    factory { CreateChatUseCase(get()) }
    
    // Use Cases - Message
    factory { SendMessageUseCase(get(), get()) }
    factory { GetMessagesUseCase(get()) }
    
    // ViewModels
    viewModel { AuthViewModel(get(), get(), get(), get()) }
    viewModel { ChatListViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get()) }
    viewModel { UserListViewModel(get(), get(), get()) }
}