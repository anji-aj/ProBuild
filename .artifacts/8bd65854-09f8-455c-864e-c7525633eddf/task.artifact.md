# Milestone 4 – Engineering Workspace Task List

## `[x]` Sprint 4.1: Foundation & Normalized Schema
- `[x]` **Global Rebranding**
    - `[x]` Rename `features.calculators` -> `features.engineering`.
    - `[x]` Update `Routes.kt` & NavHost.
    - `[x]` Update Bottom Navigation icons and strings.
- `[x]` **Normalized Persistence**
    - `[x]` Create `CalculationEntity` (Parent).
    - `[x]` Create `CalculationInputEntity` (Child).
    - `[x]` Create `CalculationWithInputs` Relation model.
    - `[x]` Implement `CalculationDao` with Transaction for saving both.
- `[x]` **Workspace Presentation**
    - `[x]` Build `EngineeringWorkspaceViewModel` with project context observer.
    - `[x]` Build `EngineeringWorkspaceScreen` (Categorized Grid).
- `[x]` **Verification**: `./gradlew assembleDebug`

## `[ ]` Sprint 4.2: Concrete Calculator (The Blueprint)
- `[ ]` **Shared Infrastructure**
    - `[ ]` Build `BaseCalculatorLayout` (M3 reusable UI).
    - `[ ]` Implement Unit conversion utilities (Metric <-> Imperial).
- `[ ]` **Concrete Module**
    - `[ ]` `ConcreteViewModel`: Formula V1.0, Live result state.
    - `[ ]` `ConcreteScreen`: Implementation using `BaseCalculatorLayout`.
- `[ ]` **History & Dashboard**
    - `[ ]` Implement History Card with Repeat/Favorite/Delete.
    - `[ ]` Update Dashboard "Resume" card.
- `[ ]` **Verification**: Math check, DB check, assembleDebug.

## `[ ]` Sprint 4.3: Quantity Rollout
- `[ ]` Implement Brick, Steel, Area, Volume modules.

## `[ ]` Sprint 4.4: Structural Rollout
- `[ ]` Implement Beam, Column, Slab, Foundation modules.

## `[ ]` Sprint 4.5: Utilities & Final Polish
- `[ ]` Implement Unit Converter.
- `[ ]` Final UI/UX review & Accessibility audit.
