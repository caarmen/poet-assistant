# Main Activity Shell — Step 1: Compose Scaffold + TabRow — Migration Spec

Spec for the first of three iterations migrating the MainActivity app shell
from the legacy View system (XML `CoordinatorLayout`/`AppBarLayout`/`ViewPager`/`TabLayout`)
to Jetpack Compose (`Scaffold`/`TabRow`), following the conventions
established by the already-migrated tab screens.

Author: Donna (architecture) — for implementation review by Dana.

---

## 0. Context: the 3-iteration plan

The remaining ~15% legacy code is concentrated in the app shell. The plan
migrates it in three small iterations:

| Iteration | Scope | Deletes |
|---|---|---|
| **Step 1 (this spec)** | Replace `activity_main.xml` with `setContent` + `Scaffold` + `TabRow` + `MainSearchBar`. The legacy `ViewPager` is hosted via a `content` slot lambda (no new code references legacy types). | `activity_main.xml`, ViewPager/TabLayout/AppBarLayout references in MainActivity |
| **Step 2** | Replace `PagerAdapter` + `Search` glue with a `MainScreenViewModel` exposing `StateFlow` for current tab, extra tab, and search routing. Host pages via `HorizontalPager`. | `PagerAdapter.kt`, `Search.kt`, `ViewShownScheduler.kt`, the `AndroidView` bridge |
| **Step 3** | Replace the 8 Fragment wrappers with direct Compose screen calls inside `HorizontalPager`. Migrate XML menus + MenuHost-on-Toolbar to Compose menus. Replace `WarningNoSpaceDialogFragment` with a Compose `AlertDialog`. | All 8 `*Fragment.kt`, `menu_main.xml`, `menu_tts.xml`, `AppBarLayoutHelper.kt`, `MenuHostProvider.kt`, `TabMenuActions.kt`, `WarningNoSpaceDialogFragment.kt` |

Step 1 is the keystone: once the shell's app bar is Compose, the fragments
and glue become dead weight that Steps 2 and 3 remove cleanly.

---

## 1. Scope and constraints

**In scope:**
- Delete `res/layout/activity_main.xml`.
- New `MainScreen.kt` composable in `main/ui/composables/` (following the
  project convention `<feature>/ui/composables/<Screen>.kt`). Previews in
  `main/ui/composables/previews/MainScreenPreviews.kt`.
- `MainScreen` builds a `Scaffold` with:
  - `topBar` slot: `MainSearchBar` (already Compose) + `TabRow` (replaces
    `TabLayout` + `PagerTitleStrip`).
  - content slot: a caller-provided `content: @Composable (PaddingValues) -> Unit`
    lambda. In Step 1, `MainActivity` supplies a lambda that hosts the legacy
    `ViewPager` via `AndroidView`. This keeps **all legacy type references
    inside `MainActivity` (existing code)** — `MainScreen.kt` imports no
    legacy View/Fragment classes.
- Preserve all existing behavior: edge-to-edge insets, tab icons vs. text
  (driven by `R.bool.tab_icons` / `R.bool.tab_text`), saved-tab restore,
  deep-link/ACTION_SEND tab selection, extra-tab (WOTD/pattern), keyboard
  hiding on tab change.
- Keep the existing XML menus (`menu_main.xml`, `menu_tts.xml`) and the
  `MenuHost`-on-Toolbar mechanism unchanged — menu migration is Step 3.
- Keep the 8 Fragments and `PagerAdapter` unchanged — they are still driven
  from `MainActivity`.

**Out of scope (deferred to Steps 2 and 3):**
- `PagerAdapter.kt` / `Search.kt` / `ViewShownScheduler.kt` — still used.
- `MainScreenViewModel` — introduced in Step 2.
- `HorizontalPager` — introduced in Step 2; Step 1 keeps ViewPager as content.
- Fragment removal — Step 3.
- Menu XML → Compose — Step 3.
- `WarningNoSpaceDialogFragment` → Compose dialog — Step 3.

**Constraints:**
- **No new code references legacy code.** `MainScreen.kt` must not import
  `ViewPager`, `PagerAdapter`, `Fragment`, `AppBarLayout`, `TabLayout`, etc.
  Legacy references live only in `MainActivity` (existing code being modified).
  This is achieved via the `content` slot lambda: `MainScreen` exposes a
  `@Composable (PaddingValues) -> Unit` parameter; the caller decides what
  to render there.
- Match the DI, theming, and ViewModel conventions of the already-migrated
  85% (Hilt `@AndroidEntryPoint`, `AppTheme`, `by viewModels()`, StateFlow).
- Tests: the agent developer makes **minimum changes so tests compile**.
  Adapting test assertions to Compose semantics is done by the product owner.

---

## 2. Current legacy implementation (what we are replacing)

### 2.1 `activity_main.xml`
```xml
CoordinatorLayout
├── ViewPager (@id/view_pager)
│   └── PagerTitleStrip (@id/pager_title_strip)  ← shown when tab_text=false
└── AppBarLayout (@id/app_bar_layout)
    ├── ComposeView (@id/search_bar)               ← MainSearchBar
    └── TabLayout (@id/tabs)
```

### 2.2 `MainActivity.onCreate` wiring
- `setContentView(R.layout.activity_main)`
- `findViewById` for `viewPager`, `tabs`, `appBarLayout`, `pagerTitleStrip`, `searchBar`
- `PagerAdapter` attached to `viewPager`, `tabs.setupWithViewPager(viewPager)`
- `DataSetObserver` syncs tab icons/text from `R.bool` resources
- `OnPageChangeListener` persists tab to `SettingsRepository`, hides keyboard
- `getInsets()` applies edge-to-edge padding to `appBarLayout` and `searchBar`
- `searchBar.setContent { MainSearchBar(…) }`

### 2.3 Bool resources controlling tab appearance
| Resource | Phone portrait | Phone landscape | Tablet |
|---|---|---|---|
| `tab_icons` | `true` | `false` | `true` |
| `tab_text` | `false` | `true` | `true` |

When `tab_text=false`, the `PagerTitleStrip` is shown (icon-only tabs with
title strip). When `tab_text=true`, `TabLayout` shows text labels and the
title strip is hidden.

---

## 3. Target architecture for Step 1

### 3.1 New file: `main/ui/composables/MainScreen.kt`

```kotlin
@Composable
fun MainScreen(
    tabs: List<Tab>,                          // 5 or 6 entries (extra tab appended)
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    showTabText: Boolean,                     // from R.bool.tab_text
    showTabIcons: Boolean,                    // from R.bool.tab_icons
    isLargeScreen: Boolean,
    suggestions: List<SearchSuggestion>,
    menuItems: () -> List<SearchBarMenuItem>,
    onMenuItemClick: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable (PaddingValues) -> Unit,  // provided by caller
    modifier: Modifier = Modifier,
)
```

**Key design decision — the `content` slot:** `MainScreen` does not know
or care what is rendered in the content area. It exposes a
`content: @Composable (PaddingValues) -> Unit` parameter. In Step 1,
`MainActivity` passes a lambda that wraps the `ViewPager` in an `AndroidView`:

```kotlin
// Inside MainActivity (existing code — legacy refs allowed here):
setContent {
    AppTheme {
        MainScreen(
            …,
            content = { padding ->
                AndroidView(
                    factory = { ctx -> viewPager },
                    modifier = Modifier.padding(padding).fillMaxSize()
                )
            }
        )
    }
}
```

This way `MainScreen.kt` has **zero imports of legacy View/Fragment types**.
All `ViewPager`/`PagerAdapter`/`AndroidView` references stay in `MainActivity`.

Internally, `MainScreen` renders:

```kotlin
Scaffold(
    topBar = {
        Column {
            MainSearchBar(
                isLargeScreen = isLargeScreen,
                suggestions = suggestions,
                menuItems = menuItems,
                onMenuItemClick = onMenuItemClick,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onExpandedChange = onExpandedChange,
            )
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = { onTabSelected(index) },
                        text = if (showTabText) { { Text(tabName(tab)) } } else null,
                        icon = if (showTabIcons) { { Icon(tabIcon(tab), null) } } else null,
                    )
                }
            }
        }
    },
    modifier = modifier,
) { contentPadding ->
    content(contentPadding)
}
```

### 3.2 TabRow vs PagerTitleStrip

The legacy layout uses two mutually exclusive mechanisms:
- `tab_text=true` → `TabLayout` with text labels (title strip hidden)
- `tab_text=false` → icon-only `TabLayout` + `PagerTitleStrip` showing the title

In Compose, `TabRow` covers both:
- `showTabText=true`: `Tab(text = { Text(...) }, icon = … if showTabIcons)`
- `showTabText=false`: `Tab(icon = { Icon(...) })` — the `TabRow` indicator
  highlights the selected tab. The `PagerTitleStrip` is dropped.

### 3.3 MainActivity changes

- Remove `setContentView(R.layout.activity_main)`.
- Call `enableEdgeToEdge()` (already used in SettingsActivity, AboutActivity).
- Call `setContent { AppTheme { MainScreen(…, content = { … AndroidView … }) } }`.
- Keep `PagerAdapter`, `Search`, menu providers, deep-link handling — all
  still work; only the view hierarchy changes.
- Remove `findViewById` for `appBarLayout`, `tabs`, `pagerTitleStrip`, `searchBar`.
- Create `viewPager` programmatically (since XML is gone) or keep a minimal
  container; capture the reference for `Search` and page-change listener.
- Replace `getInsets()` calls with Compose `WindowInsets` handling inside
  `MainScreen`.

### 3.4 Edge-to-edge insets

Legacy: `getInsets(appBarLayout) { … updatePadding(top = insets.top) }` and
`getInsets(searchBar) { … updatePadding(left, right) }`.

Compose: inside `MainScreen`, the top bar column gets
`Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))`.
The `Scaffold` content padding accounts for the top bar height automatically.

### 3.5 App bar auto-hide (reader tab)

Legacy: `AppBarLayoutHelper.enableAutoHide()` sets scroll flags on the
search bar and tabs inside `AppBarLayout` when the reader tab is selected.

In Step 1, there is no `AppBarLayout`. The auto-hide behavior (collapsing
the app bar on scroll in the reader tab) is **temporarily disabled** and
restored in Step 3 using `TopAppBarScrollBehavior` on the Compose app bar.
`AppBarLayoutHelper` is retained — its `forceExpandAppBarLayout` calls from
`MainActivity.onResume` and `ReaderFragment` become harmless no-ops.

### 3.6 Tab persistence and deep links

- `SettingsRepository.tab` get/set unchanged.
- On init: `val initialTab = settingsRepository.tab`, map to page index via
  `PagerAdapter.getPositionForTab`.
- Deep link / ACTION_SEND: `handleNewIntent` and `handleDeepLink` still call
  `viewPager.setCurrentItem(…)` — the `viewPager` reference is held by
  `MainActivity`.
- TabRow ↔ ViewPager sync: `onTabSelected` callback calls
  `viewPager.setCurrentItem(index, true)`; the existing
  `OnPageChangeListener` calls back to update `selectedTabIndex` state
  (a `MutableState<Int>` in MainActivity, passed to MainScreen).

### 3.7 Keyboard hiding on tab change

Legacy: `OnPageChangeListener.onPageSelected` hides the keyboard when
switching away from the reader tab.

Unchanged: the page-change listener still runs in `MainActivity` and hides
the keyboard via `InputMethodManager`.

---

## 4. Test impact

The agent developer makes **minimum changes so tests compile**. Adapting
test assertions is done by the product owner.

Tests that reference removed view IDs (`R.id.pager_title_strip`, `R.id.tabs`,
`R.id.app_bar_layout`, `R.id.search_bar`) in `TestUiUtils.kt` and shared test
files may need stub replacements or `@Suppress` to compile. Full migration
of `checkTitleStripOrTab`, `swipeViewPagerLeft/Right` to Compose test APIs
is the product owner's task.

New test tags to add (for the product owner's test migration):
- `Modifier.testTag("tab_${tab.name}")` on each `Tab` in `TabRow`.
- `Modifier.testTag("view_pager")` on the `AndroidView` hosting the ViewPager.

---

## 5. Edge cases and lifecycle

| Edge case | Legacy behavior | Step 1 behavior |
|---|---|---|
| Config change (rotation) | ViewPager saves state; fragments restored by FragmentManager | `rememberSaveable` for selectedTabIndex; fragments restored via AndroidView factory + FragmentManager |
| Process death | FragmentManager restores fragments; `PagerAdapter.restoreState` restores extra tab | Same — ViewPager recreated in AndroidView factory |
| Deep link to specific tab | `viewPager.setCurrentItem()` in `onCreate` | Same — captured `viewPager` reference in MainActivity |
| ACTION_SEND (share text) | Switch to reader tab, call `ReaderFragment.setPoem()` | Same — `PagerAdapter.getFragment(viewPager, READER)` still works |
| Extra tab (WOTD/Pattern) | `PagerAdapter.setExtraTab()` + `notifyDataSetChanged()` | Same — adapter still attached to ViewPager; `tabs` list passed to MainScreen updated |
| Keyboard on tab switch | Hidden when leaving reader tab | Same — `OnPageChangeListener` in MainActivity |
| App bar auto-hide on reader tab | `AppBarLayoutHelper.enableAutoHide()` | **Temporarily disabled** — restored in Step 3 |
| Edge-to-edge | `getInsets()` View-based padding | Compose `WindowInsets` padding on top bar |
| Monkey runner fullscreen | `requestWindowFeature(FEATURE_NO_TITLE)` + fullscreen flags | Unchanged (still in `onCreate` before `setContent`) |

---

## 6. Files changed

| File | Action |
|---|---|
| `res/layout/activity_main.xml` | **Deleted** |
| `main/ui/composables/MainScreen.kt` | **New** — pure Compose shell, no legacy imports |
| `main/ui/composables/previews/MainScreenPreviews.kt` | **New** — `@Preview` functions |
| `main/MainActivity.kt` | Modified: `setContent` + `enableEdgeToEdge()`, hosts ViewPager via `AndroidView` in the `content` lambda; removes `findViewById` for app bar views |
| `sharedTest/.../TestUiUtils.kt` | Minimum changes to compile only |
| `main/AppBarLayoutHelper.kt` | Unchanged (no-op without AppBarLayout) |
| `main/PagerAdapter.kt` | Unchanged |
| `main/dictionaries/search/Search.kt` | Unchanged |

---

## 7. Open questions for Dana

1. **Content slot design:** `MainScreen` takes a
   `content: @Composable (PaddingValues) -> Unit` so no new code imports
   legacy types. `MainActivity` supplies the `AndroidView`-wrapped ViewPager.
   Confirm this indirection is acceptable vs. having MainScreen directly host
   the ViewPager (which would violate the no-legacy-refs-in-new-code rule).

2. **App bar auto-hide regression:** Accept losing auto-hide on the reader
   tab until Step 3, or keep a thin AppBarLayout wrapper for Step 1?

3. **TabRow sync direction:** In Step 1, tab taps call `onTabSelected` →
   `viewPager.setCurrentItem()`, and ViewPager swipes call back via
   `OnPageChangeListener` → `MutableState<Int>`. This two-way sync works but
   is imperative. Step 2 replaces it with `HorizontalPager` +
   `pagerState.settledPage`. Confirm the imperative bridge is OK for Step 1.
