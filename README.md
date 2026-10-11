# 🏗️ ProBuild – Construction Management & Engineering Workspace

ProBuild is an Android application designed to simplify construction project management, engineering quantity estimation, unit conversion, and daily site record-keeping. It provides a centralized workspace for civil engineers, students, and construction professionals to manage projects and perform essential engineering calculations directly from their mobile devices.

## ✨ Features

### 📊 Project Dashboard
- Overview of total, active, and completed projects.
- Quick access to frequently used tools.
- Site Diary summary and project activity overview.

### 🧮 Engineering Workspace
- Concrete quantity calculator.
- Brick quantity estimation.
- Steel quantity calculation.
- Area and volume calculations.
- Beam, column, slab, and foundation calculations.

### 📐 Unit Converter
Convert commonly used engineering measurements, including:
- Length
- Area
- Volume
- Mass
- Density
- Temperature

### 📝 Site Diary
- Record daily construction activities.
- Track labour, materials, and equipment.
- Record weather conditions, site issues, and notes.
- Search, filter, and sort site records.

### 📁 Project Management
- Create and manage construction projects.
- View project information and activity history.
- Organize engineering calculations and site records.

## 🛠️ Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Design:** Material 3
- **Platform:** Android
- **Build System:** Gradle with Kotlin DSL
- **Backend Integration:** Supabase (integration in progress)

## ⚙️ Requirements

- Android Studio
- Android SDK 35 or compatible SDK setup
- JDK compatible with the project's Gradle configuration
- Internet connection for dependency downloads and backend features

## 🚀 Getting Started

1. Clone the repository:

   ```bash
   git clone https://github.com/anji-aj/ProBuild.git
   ```

2. Open Android Studio.

3. Select **Open** and choose the cloned `ProBuild` project folder.

4. Allow Gradle Sync to complete. Install any required Android SDK components if prompted.

5. Configure any required backend settings locally.

6. Run the application on an Android emulator or a physical Android device.

## 🏛️ Project Structure

```text
ProBuild/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           ├── res/
│           └── AndroidManifest.xml
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

*The exact package and source-file organization may vary as development continues.*

## 🔐 Security

- Do not commit passwords, private keys, or service-role credentials.
- Keep sensitive backend configuration out of public source control.
- Apply appropriate authentication and database access policies when connecting backend services.

## 🗺️ Future Enhancements

- Complete Supabase authentication and database integration.
- Cloud synchronization of projects and site records.
- Export project reports and engineering estimates.
- Enhanced project tracking and reporting.
- Additional engineering calculators and productivity tools.

## 🎓 Project Information

**Project Name:** ProBuild  
**Category:** Android Application / Civil Engineering  
**Purpose:** Construction project management and engineering quantity estimation

## 👨‍💻 Developer

**GitHub:** [@anji-aj](https://github.com/anji-aj)

---

⭐ If you find this project useful, consider starring the repository!

*ProBuild – Building smarter, one project at a time.*
