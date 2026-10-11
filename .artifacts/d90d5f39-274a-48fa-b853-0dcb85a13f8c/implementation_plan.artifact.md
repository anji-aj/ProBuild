# Implementation Plan - Milestone 5.3: Site Photos & Evidence

Add professional site-photo evidence to Site Diary entries, allowing users to capture and attach photos from the camera or gallery with captions and full-screen previews.

## User Review Required

> [!IMPORTANT]
> - **Photo Storage**: Photos will be copied to the application's internal storage (`filesDir/diary_photos`) to ensure persistent access and lifecycle management. They will not be stored in the public gallery.
> - **Permissions**: The `CAMERA` permission will be requested only when the user clicks "Take Photo". Gallery access will use the modern Photo Picker (Android 13+) or `GET_CONTENT` which doesn't require storage permissions.
> - **Isolation**: Photos are strictly tied to the project and diary entry they were created for.

- [ ] **Dependency Update**: I will add `io.coil-kt:coil-compose:2.6.0` to the project to handle efficient image loading and thumbnail generation. Is this acceptable?

## Proposed Changes

### Data Layer (`data/local`)

#### [NEW] [DiaryPhotoEntity.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/data/local/DiaryPhotoEntity.kt)
Room entity for storing photo metadata: `id`, `diaryEntryId`, `projectId`, `uri` (internal path), `caption`, `createdAt`.

#### [NEW] [DiaryPhotoDao.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/data/local/DiaryPhotoDao.kt)
DAO for photo CRUD operations and observation.

#### [MODIFY] [ProjectDatabase.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/data/local/ProjectDatabase.kt)
- Register `DiaryPhotoEntity`.
- Provide `diaryPhotoDao()`.
- Increment database version to 5.

---

### Domain Layer (`features/diary/domain`)

#### [NEW] [DiaryPhoto.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/diary/domain/model/DiaryPhoto.kt)
Domain model for diary photos.

#### [NEW] [DiaryPhotoRepository.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/diary/domain/DiaryPhotoRepository.kt)
Interface for photo data and file operations.

---

### Data Implementation (`features/diary/data`)

#### [NEW] [DiaryPhotoRepositoryImpl.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/diary/data/DiaryPhotoRepositoryImpl.kt)
- Implementation using Room and the internal File system.
- Handles copying source images to the app's internal directory.
- Manages file cleanup during deletion.

---

### Presentation Layer (`features/diary/presentation`)

#### [MODIFY] [DiaryUiState.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/diary/presentation/DiaryUiState.kt)
Add `photos: List<DiaryPhoto>` and photo-related UI state (loading, error).

#### [MODIFY] [DiaryViewModel.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/diary/presentation/DiaryViewModel.kt)
- Inject `DiaryPhotoRepository`.
- Load photos when an entry is loaded.
- Handle photo capture/selection results.
- Handle caption updates and photo deletion.

#### [MODIFY] [DiaryEntryScreen.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/diary/presentation/DiaryEntryScreen.kt)
- Add "Site Photos" section below the notes field.
- Implement a 2-column photo grid.
- Integrate Camera and Gallery picker contracts.

#### [NEW] [PhotoPreviewScreen.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/diary/presentation/PhotoPreviewScreen.kt)
Full-screen photo viewer with caption editing and delete functionality.

---

### Infrastructure & Navigation

#### [MODIFY] [ServiceLocator.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/di/ServiceLocator.kt)
Provide `DiaryPhotoRepository` and update `ProBuildViewModelFactory`.

#### [MODIFY] [Routes.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/navigation/Routes.kt) and [ProBuildNavHost.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/navigation/ProBuildNavHost.kt)
Add and wire the `PhotoPreview(photoId: String)` route.

---

## Verification Plan

### Automated Tests
- **DiaryPhotoRepositoryTest**: Verify Room operations and file cleanup logic.
- **DiaryViewModelTest**: Verify photo state management and permission logic flows.

### Manual Verification
1. **Camera**: Capture a photo, add a caption, and save. Verify thumbnail appears.
2. **Gallery**: Select an image from the gallery and verify it attaches correctly.
3. **Preview**: Tap a thumbnail to open full-screen. Edit the caption and verify persistence.
4. **Lifecycle**: Delete a diary entry and verify the `diary_photos` table and associated physical files are cleared.
5. **Isolation**: Confirm photos are project-specific and do not appear in other projects or unrelated diary entries.
6. **Error Handling**: Deny camera permission and verify a friendly message is shown instead of a crash.
