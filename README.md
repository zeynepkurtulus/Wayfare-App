# Wayfare - Android Travel Planning App

<div align="center">
  <h3>Your Personal Travel Companion</h3>
  <p>Plan, discover, and share amazing travel experiences with Wayfare</p>
</div>

## Overview

Wayfare is a comprehensive Android travel planning application that helps users discover destinations, plan personalized trips, and share travel experiences. Built with modern Android development practices using MVVM architecture, Kotlin, and Android Jetpack components.

## Key Features

### **Home & Discovery**
- Top destinations with detailed information and images
- Personalized recommendations based on user preferences
- Quick access to recent and favorite trips
- Trending community-generated travel routes

### **Advanced Search System**
- Dual search tabs for routes and places
- Smart filtering by category, budget, travel style, season, and location
- Real-time search results with debounced input
- Comprehensive details for each search result

###  **Trip Maker**
- Multi-step guided trip creation process
- Interest selection (cultural, adventure, nature, beach, wellness, city break)
- Budget planning with smart recommendations
- Must-visit places management
- Unsaved changes protection with warning dialogs

###  **Calendar & Trip Management**
- Interactive calendar with visual trip timeline
- Trip filtering (upcoming, ongoing, past)
- Quick trip details on date selection
- Trip statistics and travel patterns

###  **Community Feedback System**
- Route and place reviews with star ratings
- Visit date tracking
- Community insights from other travelers
- Feedback management and editing

### **User Profile & Preferences**
- Personalized travel preferences and categories
- Budget preference management
- Travel history and experiences
- Account settings and profile editing

##  Technical Architecture

### **Architecture Pattern**
- **MVVM (Model-View-ViewModel)**: Clean separation of concerns
- **Repository Pattern**: Centralized data management
- **Single Activity Architecture**: MainActivity with fragment-based navigation

### **Core Technologies**
- **Kotlin**: Primary programming language
- **Android Jetpack**: LiveData, ViewModel, ViewBinding
- **Coroutines**: Asynchronous programming
- **Retrofit**: Network API communication
- **Glide**: Image loading and caching
- **Material Design 3**: Modern UI components

### **Key Libraries**
```gradle
implementation 'com.google.android.material:material:1.9.0'
implementation 'androidx.viewpager2:viewpager2:1.0.0'
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.github.bumptech.glide:glide:4.15.1'
implementation 'de.hdodenhof:circleimageview:3.1.0'
```

## 📁 Project Structure

```
app/src/main/java/com/zeynekurtulus/wayfare/
├── data/
│   ├── api/              # API services and DTOs
│   ├── mappers/          # Data mapping utilities
│   └── repository/       # Repository implementations
├── domain/
│   ├── model/           # Domain models
│   └── repository/      # Repository interfaces
├── presentation/
│   ├── activities/      # Android Activities
│   ├── adapters/        # RecyclerView adapters
│   ├── fragments/       # UI Fragments
│   ├── navigation/      # Navigation components
│   ├── viewmodels/      # ViewModels
│   └── utils/          # UI utilities
└── utils/              # General utilities
```

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 24+ (Android 7.0)
- Kotlin 1.8+
- Gradle 7.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/zeynepkurtulus/wayfare-android.git](https://github.com/zeynepkurtulus/Wayfare-App.git
   cd Wayfare-App
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Configure API Endpoints**
   - Update API base URLs in `NetworkUtils.kt`
   - Configure authentication tokens if required

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## Configuration

### API Configuration
Update `NetworkUtils.kt`:
```kotlin
object NetworkUtils {
    const val BASE_URL = "https://your-api-endpoint.com/"
    const val API_TIMEOUT = 30L
}
```

## UI/UX Features

### **Material Design 3**
- Modern Material Design components
- Consistent color scheme and typography
- Responsive layouts for different screen sizes
- Smooth animations and transitions

### **Custom Components**
- **CustomCalendarView**: Interactive calendar for trip planning
- **SearchPagerAdapter**: Tabbed search interface
- **FeedbackAdapter**: Community review display
- **TripMakerFragment**: Multi-step form wizard

### **Navigation System**
- Bottom navigation with 5 main tabs
- Fragment-based navigation with back stack
- Smooth transitions between screens
- Unsaved changes protection

## Security & Data Management

### **Authentication**
- JWT token-based authentication
- Secure token storage
- Automatic token refresh
- Session management

### **Error Handling**
- Comprehensive error states
- User-friendly error messages
- Automatic retry mechanisms
- Offline mode support

## Performance Optimizations

### **Memory Management**
- Efficient fragment lifecycle management
- ViewPager2 state optimization
- Image loading with Glide
- RecyclerView view recycling

### **Network Optimization**
- Request caching
- Image compression
- Debounced search input
- Pagination for large datasets

##  Deployment

### **Release Build**
```bash
./gradlew assembleRelease
```

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### **Code Style**
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comprehensive comments for complex logic
- Maintain consistent formatting


## Support

For support and questions:
- **Email**: zeynep.kurtulus@alumni.sabanciuniv.edu
- **Issues**: [GitHub Issues](https://github.com/zeynepkurtulus/Wayfare-App/issues)

## Version History

### v1.0.0 (Current)
- Initial release with core features
- Trip planning and discovery
- Community feedback system
- Advanced search capabilities
- Calendar integration


---

<div align="center">
  <p>Built for travelers, by travelers</p>
</div>
