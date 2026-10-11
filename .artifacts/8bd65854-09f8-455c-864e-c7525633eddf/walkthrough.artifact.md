# Milestone 4 – Engineering Workspace (Sprint 4.1 Walkthrough)

I have successfully established the foundation for the **Engineering Workspace**, transitioning the project from a simple calculator list to a robust, project-linked engineering environment with a normalized data schema.

## Key Accomplishments

### 1. Global Rebranding & Navigation
- **Renamed Module**: Refactored `features.calculators` to `features.engineering` throughout the codebase.
- **Iconography**: Updated the primary workspace icon to `Icons.Default.Engineering` for a more professional technical feel.
- **Navigation**: Updated `Routes.kt` and the Bottom Navigation bar to reflect "Engineering Workspace".

### 2. Normalized Data Architecture
- **Schema Design**: Moved from unstructured data to a **Normalized Relational Schema**:
    - **`calculations` (Table)**: Stores core metadata (ID, Project ID, Type, Result, Timestamp, Version).
    - **`calculation_inputs` (Table)**: Stores atomic inputs linked to a calculation (Key, Value, Unit).
- **Relational Integrity**: Implemented `ForeignKey` with `CASCADE` delete. If a project is deleted, all associated engineering records are automatically purged.
- **Transactions**: Configured `CalculationDao` to handle parent/child insertions within a single database transaction.

### 3. Project-Context Awareness
- **Strict Linkage**: Every calculation is now tied to a specific project.
- **Context Header**: Added a dropdown selector in the Workspace screen to switch between projects.
- **Smart Logic**: The workspace automatically selects the most recent project on entry or prompts for project creation if none exist.

### 4. Categorized Workspace UI
- **Organization**: Grouped tools into three clear categories:
    - **Quantity Calculators** (Concrete, Brick, Steel, etc.)
    - **Structural Tools** (Beam, Column, Slab, etc.)
    - **Utilities** (Unit Converter)
- **History Tracking**: The workspace screen now displays a live, reactive history list filtered by the currently selected project.

## Technical Decisions
- **POJO Relations**: Used Room's `@Relation` and `@Embedded` annotations to fetch calculations with their inputs in a single `Flow`.
- **Constructor Injection**: Refactored the `EngineeringRepository` to be constructor-injected, following the "DI-Ready" pattern established in Milestone 1.

## Package Tree Changes
```text
com.example.probuild
├── data.local
│   ├── CalculationEntity.kt (New)
│   ├── CalculationInputEntity.kt (New)
│   └── CalculationWithInputs.kt (New)
├── features.engineering
│   ├── domain
│   │   ├── model (Calculation, Type, Category)
│   │   └── EngineeringRepository.kt
│   └── presentation
│       ├── EngineeringWorkspaceScreen.kt
│       └── EngineeringWorkspaceViewModel.kt
└── ...
```

## Verification
- [x] **Gradle Build**: Successful (`app:assembleDebug`).
- [x] **Data Integrity**: Verified normalized relationship mapping in Room.
- [x] **Project Switching**: Confirmed that switching projects instantly updates the calculation history list.

> [!TIP]
> The foundation is now ready. In **Sprint 4.2**, I will implement the **Concrete Calculator** as the reference blueprint for all future engineering tools.
