# Main Activity Shell — Step 1: Compose Scaffold + HorizontalPager + TabRow — Migration Spec

Spec for the first of three iterations migrating the MainActivity app shell
from the legacy View system (XML `CoordinatorLayout`/`AppBarLayout`/`ViewPager`/`TabLayout`)
to Jetpack Compose (`Scaffold`/`HorizontalPager`/`TabRow`), following the conventions
established by the already-migrated tab screens.

Author: Donna (architecture) — for implementation review by Dana.

---

## 0. Context: the 3-iteration plan

The remaining ~15% legacy code is concentrated in the app shell. The plan
migrates it in three small iterations:

| Iteration | Scope | Deletes |
|---|---|---|
| **Step 1 (this spec)** | Replace `activity_main.xml` with `setContent` + `Scaffold` + `HorizontalPager` + `TabRow`. Fragments stay as pager pages via a thin `AndroidView` bridge. | `activity_main.xml`, ViewPager/TabLayout/AppBarLayout references in MainActivity |
| **Step 2** | Replace `PagerAdapter` + `Search` glue with a `MainScreenViewModel` exposing `StateFlow` for current tab, extra tab, and search routing. | `PagerAdapter.kt`, `Search.kt`, `ViewShownScheduler.kt` |
| **Step 3** | Replace the 8 Fragment wrappers with direct Compose screen calls inside `HorizontalPager`. Migrate XML menus + MenuHost-on-Toolbar to Compose menus. Replace `WarningNoSpaceDialogFragment` with a Compose `AlertDialog`. | All 8 `*Fragment.kt`, `menu_main.xml`, `menu_tts.xml`, `AppBarLayoutHelper.kt`, `MenuHostProvider.kt`, `TabMenuActions.kt`, `WarningNoSpaceDialogFragment.kt` |

Step 1 is the keystone: once the shell is Compose, the fragments and glue
become pure dead weight that Steps 2 and 3 remove cleanly. Doing smaller
targets first (e.g. just the dialog or just the menus) would introduce
throwaway bridging code that must be reverted when the shell migrates anyway.

---

## 1. Scope and constraints

**In scope:**
- Replace `res/layout/activity_main.xml` with `setContent { … }` in `MainActivity`.
- Build a Compose `Scaffold` with:
  - `topBar` slot: `MainSearchBar` (already Compose) + `TabRow` (replaces `TabLayout` + `PagerTitleStrip`).
  - content slot: `HorizontalPager` hosting the 8 tab fragments via `AndroidView` interop.
- Preserve all existing behavior: edge-to-edge insets, tab icons vs. text
  (driven by `R.bool.tab_icons` / `R.bool.tab_text`), saved-tab restore,
  deep-link/ACTION_SEND tab selection, extra-tab (WOTD/pattern) dynamic tab,
  keyboard hiding on tab change, app bar auto-hide on reader tab.
- Keep the existing XML menus (`menu_main.xml`, `menu_tts.xml`) and the
  `MenuHost`-on-Toolbar mechanism unchanged — menu migration is Step 3.
- Keep the 8 Fragments unchanged — they still return `ComposeView` from
  `onCreateView`. Only the way they are hosted changes.

**Out of scope (deferred to Steps 2 and 3):**
- `PagerAdapter.kt` / `Search.kt` / `ViewShownScheduler.kt` — still used in
  Step 1 to drive fragment lookup and search routing.
- `MainScreenViewModel` — introduced in Step 2.
- Fragment removal — Step 3.
- Menu XML → Compose — Step 3.
- `WarningNoSpaceDialogFragment` → Compose dialog — Step 3.

**Constraints:**
- Match the DI, theming, and ViewModel conventions of the already-migrated
  85% (Hilt `@AndroidEntryPoint`, `AppTheme`, `by viewModels()`, StateFlow).
- No new code may refer to legacy View classes beyond the temporary
  `AndroidView` bridge for fragments.
- Tests that reference `R.id.view_pager`, `R.id.pager_title_strip`, `R.id.tabs`
  must be updated to use Compose test tags or semantics.

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
- `OnPageChangeListener` persists tab to `SettingsRepository`, hides keyboard,
  toggles app bar auto-hide via `AppBarLayoutHelper`
- `getInsets()` applies edge-to-edge padding to `appBarLayout` and `searchBar`
- `searchBar.setContent { MainSearchBar(…) }`

### 2.3 Bool resources controlling tab appearance
| Resource | Phone portrait | Phone landscape | Tablet |
|---|---|---|---|
| `tab_icons` | `true` | `false` | `true` |
| `tab_text` | `false` | `true` | `true` |
| `toolbar_auto_hide` | `true` | `true` | `false` |

When `tab_text=false`, the `PagerTitleStrip` is shown (icon-only tabs with
title strip). When `tab_text=true`, `TabLayout` shows text labels and the
title strip is hidden.

---

## 3. Target architecture for Step 1

### 3.1 New file: `MainScreen.kt` (Compose shell)

Location: `app/src/main/kotlin/ca/rmen/android/poetassistant/main/ui/MainScreen.kt`

```kotlin
@Composable
fun MainScreen(
    tabs: List<Tab>,                       // 5 or 6 entries (extra tab appended)
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    showTabText: Boolean,                  // from R.bool.tab_text
    showTabIcons: Boolean,                 // from R.bool.tab_icons
    isLargeScreen: Boolean,
    suggestions: List<SearchSuggestion>,
    menuItems: () -> List<SearchBarMenuItem>,
    onMenuItemClick: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    pageContent: @Composable (Int) -> Unit, // renders the fragment for page index
    modifier: Modifier = Modifier,
)
```

Internally:
- `val pagerState = rememberPagerState(initialPage = selectedTabIndex) { tabs.size }`
- `val coroutineScope = rememberCoroutineScope()`
- Sync: `LaunchedEffect(pagerState.settledPage) { onTabSelected(pagerState.settledPage) }`
- Sync: `LaunchedEffect(selectedTabIndex) { if (pagerState.currentPage != selectedTabIndex) coroutineScope.launch { pagerState.animateScrollToPage(selectedTabIndex) } }`
- `Scaffold(topBar = { Column { MainSearchBar(…); TabRow(selectedTabIndex = pagerState.currentPage, …) { tabs.forEachIndexed { index, tab -> Tab(…) } } } }) { contentPadding -> HorizontalPager(state = pagerState, modifier = Modifier.padding(contentPadding)) { page -> pageContent(page) } }`

### 3.2 TabRow vs PagerTitleStrip

The legacy layout uses two mutually exclusive mechanisms:
- `tab_text=true` → `TabLayout` with text labels (title strip hidden)
- `tab_text=false` → icon-only `TabLayout` + `PagerTitleStrip` showing the title

In Compose, `TabRow` with `Tab(text = …)` or `Tab(icon = …)` covers both cases:
- When `showTabText=true`: `Tab(text = { Text(tabName) }, icon = if (showTabIcons) … else …)`
- When `showTabText=false`: `Tab(icon = { Icon(…) }, text = null)` — the `TabRow`
  itself displays the selected tab; no separate title strip needed because
  `TabRow` highlights the selected tab. The `PagerTitleStrip` is dropped.

### 3.3 Fragment hosting via AndroidView

Each pager page hosts a fragment's `ComposeView` via `AndroidView`:

```kotlin
pageContent = { page ->
    val tab = tabs[page]
    AndroidView(
        factory = { ctx ->
            // Create the fragment, let it create its ComposeView, return the view
            val fragment = mPagerAdapter.getItem(page)  // or a factory
            fragment.view ?: …
        },
    )
}
```

**Simpler approach (recommended):** Since each fragment already returns a
`ComposeView` from `onCreateView`, we can use `AndroidView` with a factory
that creates the fragment, manually drives its lifecycle minimally, and
returns its view. But this is fragile.

**Better approach for Step 1:** Keep the `ViewPager` + `PagerAdapter` alive
*inside* an `AndroidView` in the `HorizontalPager` content. I.e., the
`HorizontalPager` has a single page (or we use `AndroidView` to host the
entire legacy `ViewPager`). But that defeats the purpose.

**Recommended Step 1 approach:** Use `AndroidView` per page with a factory
that inflates a `FrameLayout`, then the activity's `supportFragmentManager`
is used to add the fragment into that container. The `PagerAdapter.getItem()`
creates the fragment; we add it via a `FragmentTransaction` with the
`FrameLayout` id as container. This keeps fragments alive but hosts them
inside Compose.

Actually, the **cleanest Step 1 bridge**: keep `ViewPager` as an `AndroidView`
inside the `Scaffold` content (not inside `HorizontalPager`), and replace
only the `AppBarLayout` (search bar + tabs) with Compose. This is a smaller,
safer change:

```kotlin
Scaffold(
    topBar = {
        Column {
            MainSearchBar(…)
            TabRow(…) { … }   // drives viewPager via callback
        }
    }
) { padding ->
    AndroidView(
        factory = { ctx -> ViewPager(ctx).apply { … } },
        update = { vp -> /* sync adapter, current item */ },
        modifier = Modifier.padding(padding).fillMaxSize()
    )
}
```

This replaces the XML shell (CoordinatorLayout + AppBarLayout) with Compose
Scaffold + TabRow + MainSearchBar, while keeping ViewPager as the content.
HorizontalPager migration is then Step 2.

**Decision:** For Step 1, use the `Scaffold` + `TabRow` + `MainSearchBar`
top bar and keep `ViewPager` as an `AndroidView` in the content slot. This
is the smallest change that deletes `activity_main.xml` and moves the app bar
to Compose, without yet tackling fragment hosting inside `HorizontalPager`.

### 3.4 MainActivity changes

- Remove `setContentView(R.layout.activity_main)`.
- Call `enableEdgeToEdge()` (already used in SettingsActivity, AboutActivity).
- Call `setContent { AppTheme { MainScreen(…) } }`.
- Keep `PagerAdapter`, `Search`, menu providers, deep-link handling — all
  still work; only the view hierarchy changes.
- Remove `findViewById` for `appBarLayout`, `tabs`, `pagerTitleStrip`, `searchBar`.
- Keep `viewPager` reference obtained from the `AndroidView` factory callback
  (store in a field for `Search` and page-change listener).
- Replace `getInsets()` calls with Compose `WindowInsets` handling inside
  `MainScreen` (use `WindowInsets.systemBars` / `displayCutout` with
  `padding`/`imePadding` as appropriate).

### 3.5 Edge-to-edge insets

Legacy: `getInsets(appBarLayout) { … updatePadding(top = insets.top) }` and
`getInsets(searchBar) { … updatePadding(left, right) }`.

Compose: inside `MainScreen`, wrap the `Scaffold` with
`Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))`
on the top bar column, and handle horizontal insets via
`WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)`.
The `Scaffold` content padding already accounts for the top bar height.

### 3.6 App bar auto-hide (reader tab)

Legacy: `AppBarLayoutHelper.enableAutoHide()` sets scroll flags on the
search bar and tabs inside `AppBarLayout` when the reader tab is selected.

In Step 1, the `ViewPager` is still a View inside `AndroidView`, so
`AppBarLayoutHelper` can still operate if we keep an `AppBarLayout`.
But we are removing `AppBarLayout` from the XML.

**Decision for Step 1:** Since the app bar is now Compose (`Scaffold` topBar),
the auto-hide behavior (collapsing the app bar on scroll in the reader tab)
cannot be preserved with `AppBarLayout` scroll flags. Options:
1. Defer auto-hide to Step 3 when the reader screen is directly in Compose
   and can use `TopAppBarScrollBehavior`.
2. Keep a thin `AppBarLayout` wrapper around the Compose top bar.

**Recommended:** Accept a temporary behavior change — the app bar no longer
auto-hides on the reader tab in Step 1. This is restored in Step 3 using
`TopAppBarScrollBehavior` (enterAlways) on the Compose `TopAppBar`/`Scaffold`.
Document this as an accepted temporary regression, consistent with how the
reader-tab migration handled the word-lookup popup regression.

`AppBarLayoutHelper` is retained (still called from `ReaderFragment` and
`MainActivity.onResume` for `forceExpandAppBarLayout`) but its
`enableAutoHide`/`disableAutoHide` become no-ops when there is no
`AppBarLayout`. The `forceExpandAppBarLayout` calls remain harmless no-ops.

### 3.7 Tab persistence and deep links

- `SettingsRepository.tab` get/set unchanged.
- On `MainScreen` init: `val initialTab = settingsRepository.tab`, map to
  page index via `PagerAdapter.getPositionForTab`.
- Deep link / ACTION_SEND: `handleNewIntent` and `handleDeepLink` still call
  `viewPager.setCurrentItem(…)` — the `viewPager` reference is captured from
  the `AndroidView` factory.

### 3.8 Keyboard hiding on tab change

Legacy: `OnPageChangeListener.onPageSelected` hides the keyboard when
switching away from the reader tab.

Compose: the `onTabSelected` callback in `MainScreen` receives the new page;
`MainActivity` hides the keyboard via `InputMethodManager` as before.

---

## 4. Test impact

### 4.1 Tests referencing removed view IDs

`TestUiUtils.kt` (sharedTest) references:
- `R.id.pager_title_strip` — used in `checkTitleStripOrTab`
- `R.id.view_pager` — used in `swipeViewPagerLeft`/`swipeViewPagerRight`
- `R.id.tabs` — used in `checkTitleStripOrTab`

These must be migrated to Compose test semantics:
- `checkTitleStripOrTab` → use `composeTestRule.onNodeWithText(tabName).assertIsDisplayed()`
  (the `TabRow` tab label is a Compose text node).
- `swipeViewPagerLeft/Right` → use `composeTestRule.onNodeWithTag("pager").performTouchInput { swipeLeft() }`
  with a `Modifier.testTag("pager")` on the `AndroidView` hosting the ViewPager.

Affected test files (all in `sharedTest/`):
- `TestUiUtils.kt`
- `SelectedTabTest.kt`, `IntentTest.kt`, `ThesaurusSettingsTest.kt`,
  `ThesaurusRouterTest.kt`, `RhymerRouterTest.kt`, `DictionaryRouterTest.kt`,
  `IntegrationTest.kt` — any test using `swipeViewPager*` or `checkTitleStripOrTab`.

### 4.2 New test tags

Add `Modifier.testTag(…)` to:
- The `AndroidView` hosting `ViewPager`: `"view_pager"`
- Each `Tab` in `TabRow`: `"tab_${tab.name}"`

---

## 5. Edge cases and lifecycle

| Edge case | Legacy behavior | Step 1 behavior |
|---|---|---|
| Config change (rotation) | ViewPager saves state via `PagerAdapter.saveState()`; fragments restored by FragmentManager | `rememberPagerState` + `rememberSaveable` preserves page; fragments restored via AndroidView factory re-adding to FragmentManager |
| Process death | FragmentManager restores fragments; `PagerAdapter.restoreState` restores extra tab | Same — AndroidView factory re-creates fragments on process death |
| Deep link to specific tab | `viewPager.setCurrentItem()` in `onCreate` | Same — captured `viewPager` reference |
| ACTION_SEND (share text) | Switch to reader tab, call `ReaderFragment.setPoem()` | Same — `mPagerAdapter.getFragment(viewPager, READER)` still works |
| Extra tab (WOTD/Pattern) | `PagerAdapter.setExtraTab()` + `notifyDataSetChanged()` | Same — adapter still attached to ViewPager |
| Keyboard on tab switch | Hidden when leaving reader tab | Same — `onTabSelected` callback |
| App bar auto-hide on reader tab | `AppBarLayoutHelper.enableAutoHide()` | **Temporarily disabled** — restored in Step 3 with `TopAppBarScrollBehavior` |
| Edge-to-edge | `getInsets()` View-based padding | Compose `WindowInsets` padding on Scaffold top bar |
| Monkey runner fullscreen | `requestWindowFeature(FEATURE_NO_TITLE)` + fullscreen flags | Unchanged (still in `onCreate` before `setContent`) |

---

## 6. Files changed

| File | Action |
|---|---|
| `res/layout/activity_main.xml` | **Deleted** |
| `main/MainActivity.kt` | Modified: remove `setContentView`, add `enableEdgeToEdge()`, `setContent { MainScreen(…) }`, remove `findViewById` for app bar views, capture `viewPager` from AndroidView factory |
| `main/ui/MainScreen.kt` | **New**: Compose shell with Scaffold + TabRow + MainSearchBar + AndroidView(ViewPager) |
| `sharedTest/.../TestUiUtils.kt` | Modified: migrate `checkTitleStripOrTab`, `swipeViewPager*` to Compose test APIs |
| Various `sharedTest` router/intent tests | Modified: if they use the above helpers, they may need minor adjustments |
| `main/AppBarLayoutHelper.kt` | Unchanged (calls become no-ops without AppBarLayout; retained for Step 3) |
| `main/PagerAdapter.kt` | Unchanged (still used) |
| `main/dictionaries/search/Search.kt` | Unchanged (still uses viewPager reference) |

---

## 7. Open questions for Dana

1. **AndroidView vs. direct HorizontalPager for fragments:** Step 1 keeps
   ViewPager as an AndroidView inside Scaffold. An alternative is to use
   `HorizontalPager` directly and host fragments via `AndroidView` per page.
   The latter is cleaner long-term but riskier in one step. Confirm the
   AndroidView-ViewPager approach is acceptable for Step 1.

2. **App bar auto-hide regression:** Accept losing auto-hide on the reader
   tab until Step 3, or keep a thin AppBarLayout wrapper for Step 1?

3. **Test migration scope:** Should `TestUiUtils` helpers be migrated in this
   step, or can tests continue using Espresso `onView` on the AndroidView-hosted
   ViewPager (since it still exists as a View)? The `checkTitleStripOrTab` helper
   specifically checks `PagerTitleStrip` vs `TabLayout`, which both change.
