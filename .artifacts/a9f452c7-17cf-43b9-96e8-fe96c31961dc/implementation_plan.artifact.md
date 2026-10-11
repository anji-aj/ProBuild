# Implementation Plan - Milestone 4.2: Concrete Volume Calculator

This milestone focuses on implementing the first fully functional engineering calculator: **Concrete Volume**. It will serve as a reference implementation for UI, validation, unit conversion, and persistence patterns.

## Proposed Changes

### 1. Domain Layer Refinements
#### [NEW] [DimensionUnit.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/engineering/domain/model/DimensionUnit.kt)
- Enum for `METER`, `CENTIMETER`, `FEET`, `INCH`.
- Conversion factors to base unit (Meters).

#### [NEW] [UnitConverter.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/engineering/util/UnitConverter.kt)
- Centralized logic for dimension and volume conversions.
- Volume results in `m³` and `ft³`.

#### [MODIFY] [EngineeringRepository.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/engineering/domain/EngineeringRepository.kt) & [EngineeringRepositoryImpl.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/engineering/data/EngineeringRepositoryImpl.kt)
- Add `suspend fun getCalculationById(id: String): Calculation?`.

### 2. Data Layer Refinements
#### [MODIFY] [CalculationDao.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/data/local/CalculationDao.kt)
- Add `@Transaction @Query` to fetch a specific calculation by ID with its inputs.

### 3. Shared Presentation Components
#### [NEW] [NumericInputField.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/ui/components/NumericInputField.kt)
- Material 3 `OutlinedTextField` specialized for numeric input with validation support.

#### [NEW] [UnitSelector.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/ui/components/UnitSelector.kt)
- Reusable dropdown or segmented button for selecting units.

#### [NEW] [CalculationResultCard.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/ui/components/CalculationResultCard.kt)
- Consistent card for displaying primary and secondary results with input summaries.

### 4. Concrete Calculator Feature
#### [NEW] [ConcreteCalculatorViewModel.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/engineering/presentation/concrete/ConcreteCalculatorViewModel.kt)
- Logic for handling Length, Width, Depth inputs.
- Live calculation state (pre-persistence).
- Validation logic.
- "Repeat" mode logic: pre-filling inputs from an existing `Calculation` record.

#### [NEW] [ConcreteCalculatorScreen.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/engineering/presentation/concrete/ConcreteCalculatorScreen.kt)
- Professional Material 3 form layout.
- Integrates reusable components.
- Responsive design for different screen sizes.

### 5. Navigation
#### [MODIFY] [Routes.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/navigation/Routes.kt)
- Add `ConcreteCalculator(calculationId: String? = null) : Screen`.

#### [MODIFY] [ProBuildNavHost.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/navigation/ProBuildNavHost.kt)
- Register the `ConcreteCalculator` route.

#### [MODIFY] [EngineeringWorkspaceScreen.kt](file:///D:/AndroidStudioProjects/ProBuild/app/src/main/java/com/example/probuild/features/engineering/presentation/EngineeringWorkspaceScreen.kt)
- Handle clicks on "Concrete" card and "Repeat" history action to navigate to the calculator.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `UnitConverterTest`: Test accuracy of m, cm, ft, in conversions.
    - `ConcreteVolumeCalculationTest`: Test formula logic with various unit combinations.
    - `ConcreteCalculatorViewModelTest`: Test validation states and live result updates.

### Manual Verification
1. **Selection**: Select a project, navigate to Engineering Workspace.
2. **Launch**: Open Concrete Calculator. Verify correct project context is displayed.
3. **Live Calc**: Enter 5m, 2m, 0.2m. Verify result shows 2.00 m³ and 70.63 ft³.
4. **Validation**: Clear a field. Verify button disables and error message appears.
5. **Persist**: Click "Calculate". Go back. Verify item appears in Recent Calculations.
6. **Repeat**: Click "Repeat" on history item. Verify all inputs and units are restored perfectly.
7. **Switch Project**: Switch project. Verify history item disappears (project-specific history).
