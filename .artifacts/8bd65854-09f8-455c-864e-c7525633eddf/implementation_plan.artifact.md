# Milestone 4 – Engineering Workspace (Staged & Normalized)

This plan implements a professional Engineering Workspace with a normalized Room schema, live calculations, and full unit support.

## User Review Required

> [!IMPORTANT]
> **Data Normalization**: Instead of a JSON string, I am implementing a **Normalized Schema** (`Calculation` table and a child `CalculationInput` table). This ensures maximum searchability, reporting accuracy, and PDF export support.

> [!WARNING]
> **Global Rebrand**: All references to "Calculators" will be replaced with "Engineering Workspace".

---

## Sprint 4.1: Workspace Foundation & Normalized Schema

### [Component Name] Data Layer (Normalized Room)
- **`CalculationEntity`**: Core record (id, projectId, type, displayName, formulaVersion, resultVal, resultUnit, timestamp, isFavorite).
- **`CalculationInputEntity`**: Normalized inputs (id, calculationId, inputKey, val, unit).
- **`CalculationWithInputs`**: A Room `Relation` POJO to fetch the full record in one flow.
- **`CalculationDao`**: CRUD for both tables with Transaction support.

### [Component Name] Domain Layer
- **`Calculation` Domain Model**: Includes a list of `CalculationInput` objects.
- **`EngineeringRepository`**: Exposes `Flow<List<Calculation>>`.

### [Component Name] Workspace Skeleton
- **`EngineeringWorkspaceScreen`**:
    - Project context selector (Dropdown).
    - "No Project" redirect logic.
    - Category Grid: **Structural**, **Quantity**, **Utilities**.

---

## Sprint 4.2: Concrete Calculator (The Pattern Reference)

### [Component Name] Reusable UI Layout
- **`BaseCalculatorLayout`**: A shared M3 layout providing:
    - Unit Toggle (Metric vs Imperial).
    - Live Result Header (updates while typing).
    - Input Grid.
    - "Save to Project" / "Reset" actions.

### [Component Name] Concrete Reference Implementation
- **Logic**: Volume math (L x W x D) with dynamic unit conversion.
- **Formula Versioning**: Tagged as `V1.0`.
- **Live State**: ViewModel uses `derivedStateOf` or `combine` to show the result instantly.
- **Auto-Save**: Persists to the normalized database upon explicit "Save" or "Calculate" trigger.

---

## Future Sprints
- **Sprint 4.3: Quantity Rollout**: Brick, Steel, Area, Volume (following the Concrete pattern).
- **Sprint 4.4: Structural Rollout**: Beam, Column, Slab, Foundation.
- **Sprint 4.5: Utility & Dashboard**: Unit Converter, Dashboard "Quick Resume" card.

## Verification Plan

### Automated Tests
- `./gradlew assembleDebug` after every sprint.
- Room DAO tests for One-to-Many insertion accuracy.

### Manual Verification
- **Normalization**: Verify that deleting a project CASCADE deletes its calculations and inputs.
- **Live Math**: Verify results update instantly as numbers are keyed in.
- **Units**: Switch between Metric (m) and Imperial (ft) and verify values convert correctly.
- **Dashboard**: Verify the "Last Calculation" card shows the actual result and project name.
