# Implementation Plan - ProBuild Enterprise Architecture & Dashboard

This plan outlines the implementation of a professional, feature-first Clean Architecture for ProBuild, including a premium dashboard for civil engineers.

## User Review Required

> [!IMPORTANT]
> - **Package Restructuring**: I will migrate from a layer-first (`ui`, `data`, `repository`) to a **feature-first** structure (`features/dashboard/...`).
> - **Clean Architecture Layers**: Each feature will be divided into `presentation`, `domain`, and `data`.
> - **Root Navigation**: I will implement a `RootNavGraph` that supports nested navigation (Auth, Main, Onboarding) even if some parts are currently empty.

## Proposed Changes

### 1. Core Design System (Material 3)
- [MODIFY] [Color.kt](file:///D:/ProBuild/app/src/main/java/com/example/probuild/ui/theme/Color.kt): Refine palette.
- [MODIFY] [Theme.kt](file:///D:/ProBuild/app/src/main/java/com/example/probuild/ui/theme/Theme.kt): Fully implement Dark/Light/Dynamic modes.
- [NEW] `ui/theme/Shape.kt`: Define 20dp rounded corners and other Material 3 shapes.
- [NEW] `ui/theme/Dimens.kt`: Centralize all dimensions (8dp grid).
- [MODIFY] [Type.kt](file:///D:/ProBuild/app/src/main/java/com/example/probuild/ui/theme/Type.kt): Premium typography selection.

### 2. Feature-First Architecture
I will create the following structure for each feature:
- `features/<feature>/presentation`: UI/Screens, ViewModels.
- `features/<feature>/domain`: Repository Interfaces, Use Cases (as needed), Immutable Domain Models.
- `features/<feature>/data`: Repository Implementations, Mappers, Data Sources (Mock/Room placeholders).

Features to initialize:
- `dashboard` (Full implementation)
- `projects`, `calculators`, `diary`, `reports`, `settings` (Skeleton/Placeholders)

### 3. Advanced Navigation (RootNavGraph)
- [NEW] `navigation/RootNavGraph.kt`: Top-level graph handling `Auth`, `Main`, and `Onboarding` routes.
- [NEW] `navigation/MainNavGraph.kt`: Handles dashboard and bottom-nav destinations.
- [NEW] Placeholders for future modules: `Equipment`, `Attendance`, `AI Assistant`, etc.

### 4. Dashboard Implementation
- **DashboardViewModel**: Managing greeting logic, quick actions list, and project StateFlow.
- **UI States**: Implementing `UiState<T>` (Loading, Success, Empty, Error).
- **Dashboard Components**:
    - `DashboardTopBar`: Dynamic greeting, search, notification, profile.
    - `QuickActionGrid`: Responsive grid with 20dp cards.
    - `RecentProjectList`: List of project cards with progress bars and status chips.

### 5. Accessibility & Quality
- **Touch Targets**: Guaranteed 48dp+ for all clickable elements.
- **Content Descriptions**: Required for every icon and non-text element.
- **Strings**: 100% string resource usage (no hardcoded text).
- **Performance**: Optimized recomposition using `remember`, `derivedStateOf`, and `key`.

## Verification Plan

### Automated Tests
- `./gradlew assembleDebug` to ensure compilation.

### Manual Verification
- **Architecture Audit**: Verify package structure against the Clean Architecture plan.
- **Design System Audit**: Check colors, shapes, and accessibility (talkback readiness).
- **Navigation Flow**: Verify nested graphs and bottom-bar integration.
- **State Handling**: Toggle mock states (loading/error) to verify UI responsiveness.

## Deliverables Post-Execution
- Architecture diagram (Mermaid)
- Package structure tree
- Component hierarchy map
- Documentation of architectural decisions
