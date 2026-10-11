# Task: Implement Site Photos & Evidence (Milestone 5.3)

- `[ ]` Infrastructure Setup
    - `[ ]` Add Coil dependency to `libs.versions.toml` and `build.gradle.kts`
    - `[ ]` Update `ProjectDatabase.kt` (v5) and add `DiaryPhotoEntity`/`DiaryPhotoDao`
- `[ ]` Data & Domain Implementation
    - `[ ]` Create `DiaryPhoto.kt` domain model
    - `[ ]` Create `DiaryPhotoRepository` and `DiaryPhotoRepositoryImpl`
- `[ ]` Presentation Layer - Photos
    - `[ ]` Update `DiaryUiState` and `DiaryViewModel` for photo handling
    - `[ ]` Implement photo section in `DiaryEntryScreen.kt`
    - `[ ]` Create `PhotoPreviewScreen.kt`
- `[ ]` Integration & Navigation
    - `[ ]` Update `ServiceLocator.kt`
    - `[ ]` Wire `PhotoPreview` route in `Routes.kt` and `ProBuildNavHost.kt`
- `[ ]` Testing & Verification
    - `[ ]` Add unit tests for photo repository and viewmodel
    - `[ ]` Perform manual verification on device
