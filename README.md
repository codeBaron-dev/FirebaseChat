# 🔥 FireChat - Real-Time Chat App

A modern Android chat application built with **Jetpack Compose**, **Firebase**, and **Koin** dependency injection, featuring real-time messaging and a sleek Material Design 3 UI.

## ✨ Features

- **Real-time messaging** with Firebase Firestore
- **User authentication** with Firebase Auth
- **Material Design 3** UI with dark/light theme support
- **Clean Architecture** with MVVM pattern
- **Dependency Injection** using Koin
- **User presence** indicators (online/offline status)
- **Message status** indicators (sent/delivered/read)
- **Empty state** handling for better UX
- **Avatar** support with initials fallback

## 📱 Screenshots

The app includes the following screens matching the provided UI design:
- **Chat List** - Shows all conversations with last message and time
- **Empty Chat List** - Placeholder when no conversations exist
- **Chat Screen** - Real-time messaging with message bubbles
- **Empty Chat** - Initial state for new conversations
- **Login/Register** - Firebase authentication screens

## 🛠 Tech Stack

### Core
- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material Design 3** - Design system

### Architecture
- **MVVM** - Architectural pattern
- **Clean Architecture** - Separation of concerns
- **Use Cases** - Business logic encapsulation
- **Repository Pattern** - Data abstraction

### Dependencies
- **Firebase Auth** - User authentication
- **Firebase Firestore** - Real-time database
- **Firebase Storage** - File uploads
- **Koin** - Dependency injection
- **Navigation Compose** - App navigation
- **Coil** - Image loading
- **Room** - Local caching (ready for implementation)

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 23+
- Google account for Firebase setup

### Firebase Setup

1. **Create Firebase Project**
   ```
   1. Go to Firebase Console (https://console.firebase.google.com/)
   2. Create a new project called "FireChat" or your preferred name
   3. Enable Google Analytics (optional)
   ```

2. **Add Android App**
   ```
   1. Click "Add app" → Android
   2. Package name: com.example.firechat
   3. App nickname: FireChat (optional)
   4. SHA-1: Generate using: ./gradlew signingReport
   ```

3. **Download Configuration**
   ```
   1. Download google-services.json
   2. Place it in app/ directory (replace the placeholder file)
   ```

4. **Enable Firebase Services**
   ```
   Authentication:
   1. Go to Authentication → Sign-in method
   2. Enable Email/Password authentication
   
   Firestore:
   1. Go to Firestore Database → Create database
   2. Choose "Start in test mode" 
   3. Select a location close to your users
   4. Go to Rules tab
   5. For testing: Use firestore-test.rules (more permissive)
   6. For production: Use firestore.rules (secure)
   7. Click "Publish"
   
   Optional - For better performance with large datasets:
   7. Go to Indexes tab
   8. Import firestore.indexes.json or create indexes manually if needed
   ```

### Installation

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd FireChat
   ```

2. **Open in Android Studio**
   ```
   1. Open Android Studio
   2. File → Open → Select the FireChat folder
   3. Wait for Gradle sync to complete
   ```

3. **Replace Firebase Configuration**
   ```
   Replace google-services.json with your actual Firebase config file
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Run button

## 📂 Project Structure

```
app/src/main/java/com/example/firechat/
├── data/
│   ├── model/              # Data models (User, Chat, Message)
│   └── repository/         # Repository implementations
├── domain/
│   ├── repository/         # Repository interfaces
│   └── usecase/           # Business logic use cases
├── di/                    # Dependency injection modules
├── presentation/
│   ├── screen/            # Compose UI screens
│   └── viewmodel/         # ViewModels
├── navigation/            # Navigation setup
├── ui/theme/             # Material Design theme
├── FireChatApplication.kt # Application class
└── MainActivity.kt       # Main activity
```

## 🏗 Architecture

The app follows **Clean Architecture** principles:

### Data Layer
- **Models**: User, Chat, Message data classes
- **Repositories**: Firebase implementation for data operations
- **Remote Data Source**: Firebase Auth & Firestore

### Domain Layer
- **Use Cases**: Business logic (Login, SendMessage, GetChats, etc.)
- **Repository Interfaces**: Abstraction for data operations

### Presentation Layer
- **ViewModels**: UI state management with StateFlow
- **Compose Screens**: UI components and screens
- **Navigation**: Navigation between screens

## 🔥 Firebase Collections Structure

```
/users/{userId}
├── id: String
├── email: String
├── displayName: String
├── avatarUrl: String
├── isOnline: Boolean
└── lastSeen: Long

/chats/{chatId}
├── participants: Array<String>
├── lastMessage: String
├── lastMessageType: String
├── lastMessageTime: Long
├── lastMessageSender: String
└── /messages/{messageId}
    ├── senderId: String
    ├── receiverId: String
    ├── content: String
    ├── type: String
    ├── timestamp: Long
    ├── status: String
    ├── senderName: String
    └── senderAvatar: String
```

## 🎨 UI Design

The app matches the provided UI design with:
- **Chat List**: User avatars, last messages, timestamps, unread badges
- **Chat Screen**: Message bubbles, status indicators, input bar
- **Empty States**: Friendly messages for empty conversations
- **Material 3**: Modern design with proper theming

## 🔧 Configuration

### Koin Modules
All dependencies are configured in `AppModule.kt`:
- Firebase instances
- Repositories
- Use Cases  
- ViewModels

### Theme
Material Design 3 theme configured in `ui/theme/` with:
- Color schemes (light/dark)
- Typography
- Shapes

## 🧪 Testing

The project is set up for testing with:
- **Unit Tests**: Business logic testing
- **UI Tests**: Compose UI testing
- **Integration Tests**: Firebase integration testing

Run tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 📱 APK Build

To build a release APK:

1. **Generate signed APK**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Location**: `app/build/outputs/apk/release/app-release.apk`

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🙏 Acknowledgments

- **Firebase** - Backend as a Service
- **Jetpack Compose** - Modern Android UI
- **Material Design** - Design system
- **Koin** - Dependency injection framework

---

**Built with ❤️ for modern Android development**# FireChat
