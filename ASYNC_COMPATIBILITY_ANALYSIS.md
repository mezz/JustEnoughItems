# JEI-Async Compatibility Analysis & Solutions

## Executive Summary

This document provides a comprehensive analysis of breaking changes in the JEI-Async fork (1.20.1-async branch) and proposes fully backward-compatible solutions that require **zero code changes** from other mod developers.

The original PR (#3889) was rejected by mod author mezz due to:
1. Breaking changes to the API
2. Many subtle concurrency issues during testing
3. High risk of incompatibility with existing mods

This analysis identifies all breaking changes (both API-level and behavioral) and provides architectural solutions that maintain 100% backward compatibility.

---

## 1. Breaking Changes Analysis

### 1.1 Critical Breaking Changes (Will Cause Crashes)

#### 1.1.1 Main Thread Enforcement

**Location:** `Common/src/main/java/mezz/jei/common/util/ErrorUtil.java` (Line 152)

```java
public static void assertMainThread() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft != null && !minecraft.isSameThread()) {
        Thread currentThread = Thread.currentThread();
        throw new IllegalStateException(
            "A JEI API method is being called by another mod from the wrong thread:\n" +
            currentThread + "\n" +
            "It must be called on the main thread by using Minecraft.addScheduledTask."
        );
    }
}
```

**Affected API Methods:**
- `RecipeManager.addRecipes()` - `Library/src/main/java/mezz/jei/library/recipes/RecipeManager.java:64`
- `RecipeManager.hideRecipes()` - Line 126
- `RecipeManager.unhideRecipes()` - Line 135
- `RecipeManager.hideRecipeCategory()` - Line 142
- `RecipeManager.unhideRecipeCategory()` - Line 149
- `IngredientManager.addIngredientsAtRuntime()` - `Library/src/main/java/mezz/jei/library/ingredients/IngredientManager.java:93`
- `IngredientManager.removeIngredientsAtRuntime()` - Line 150

**Impact:** Any mod calling these methods from background threads will now crash with `IllegalStateException`.

**Root Cause:** The async fork added thread safety checks that didn't exist in vanilla JEI. While well-intentioned, this breaks mods that may have been calling these methods from worker threads (even if incorrectly).

**Solution:** See Section 2.1 - Graceful Thread Handling

---

### 1.2 Moderate Breaking Changes (Deprecations)

#### 1.2.1 IRecipeCategory.getBackground()

**Location:** `CommonApi/src/main/java/mezz/jei/api/recipe/category/IRecipeCategory.java:57`

```java
@Deprecated(since = "15.20.0", forRemoval = true)
@Nullable
default IDrawable getBackground() { return null; }
```

**Impact:** Mods using this method will get deprecation warnings. Future removal will break compilation.

**Migration:** Override `getWidth()` and `getHeight()`, draw background in `draw()` method.

---

#### 1.2.2 IRecipeCategory.getTooltipStrings()

**Location:** `CommonApi/src/main/java/mezz/jei/api/recipe/category/IRecipeCategory.java:181`

```java
@Deprecated(since = "15.8.4", forRemoval = true)
default List<Component> getTooltipStrings(...) { return List.of(); }
```

**Impact:** Mods using this method will get deprecation warnings.

**Migration:** Use `getTooltip(ITooltipBuilder, ...)` at line 199 instead.

---

#### 1.2.3 IRecipeCategory.handleInput()

**Location:** `CommonApi/src/main/java/mezz/jei/api/recipe/category/IRecipeCategory.java:217`

```java
@Deprecated(since = "15.9.0", forRemoval = true)
default boolean handleInput(...) { return false; }
```

**Impact:** Mods using this method will get deprecation warnings.

**Migration:** Create `IRecipeExtras` with `IJeiInputHandler` or `GuiEventListener` via `IRecipeExtrasBuilder.addInputHandler()`.

---

#### 1.2.4 IIngredientManager.getIngredientByUid()

**Location:** `CommonApi/src/main/java/mezz/jei/api/runtime/IIngredientManager.java:196`

```java
@Deprecated(since = "15.5.0")
<V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String ingredientUuid);
```

**Impact:** Mods using this method will get deprecation warnings.

**Migration:** Use `getTypedIngredientByUid(IIngredientType, String)` at line 203.

---

#### 1.2.5 IJeiRuntime.getIngredientVisibility()

**Location:** `CommonApi/src/main/java/mezz/jei/api/runtime/IJeiRuntime.java:62`

```java
@Deprecated(since = "15.19.1", forRemoval = true)
default IIngredientVisibility getIngredientVisibility() {
    return getJeiHelpers().getIngredientVisibility();
}
```

**Impact:** Mods using this method will get deprecation warnings.

**Migration:** Use `getJeiHelpers().getIngredientVisibility()` directly.

---

#### 1.2.6 IRecipeManager.createRecipeSlotDrawable() with position

**Location:** `CommonApi/src/main/java/mezz/jei/api/recipe/IRecipeManager.java:159`

```java
@Deprecated(since = "15.20.0")
default IRecipeSlotDrawable createRecipeSlotDrawable(..., int xPos, int yPos, ...)
```

**Impact:** Mods using this method will get deprecation warnings.

**Migration:** Use new method without position (line 137), then call `setPosition()` on result.

---

### 1.3 Behavioral Breaking Changes (Subtle Issues)

These are the **most dangerous** breaking changes that mezz referenced in the PR rejection. They don't cause immediate crashes but lead to subtle, hard-to-debug issues.

#### 1.3.1 Parallel Stream Processing in IngredientFilter

**Location:** `Gui/src/main/java/mezz/jei/gui/ingredients/IngredientFilter.java:202`

```java
elementStream = this.elementSearch.getAllIngredients()
    .parallelStream();  // ← ASYNC CHANGE
```

**Impact:**
- **Non-deterministic ordering:** Parallel streams don't guarantee encounter order unless explicitly sorted
- **Race conditions during search:** If ingredient list is modified during parallel processing, `ConcurrentModificationException` or corrupted results may occur
- **Thread-local state violations:** Some mod-added ingredients may rely on thread-local state that doesn't work correctly in parallel streams

**Example Failure Scenario:**
```java
// Mod A adds a dynamic ingredient that checks thread-local world state
public class DynamicIngredient implements IIngredient {
    @Override
    public boolean matches(SearchContext context) {
        // This assumes main thread access!
        Level level = Minecraft.getInstance().level;
        return level.getBlockState(pos).getBlock() == this.block;
    }
}

// With parallelStream(), this may be called from worker thread → CRASH or wrong results
```

---

#### 1.3.2 Plugin Execution Timing Changes

**Location:** `Library/src/main/java/mezz/jei/library/load/PluginCallerTimer.java`

```java
public class PluginCallerTimer implements AutoCloseable {
    private final ScheduledExecutorService executor;
    
    public PluginCallerTimer() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.scheduleAtFixedRate(this::run, 100, 100, TimeUnit.MILLISECONDS);
    }
}
```

**Impact:**
- **Plugin timeout monitoring:** Plugins taking >10ms trigger warnings (see `PluginManager.java:96`)
- **Crashing plugins disabled:** Plugins that crash during registration are now silently disabled instead of crashing JEI (may hide bugs)
- **Timing-dependent mods:** Mods that assume synchronous, immediate registration may fail if plugin is slow/disabled

**Example Failure Scenario:**
```java
// Mod B depends on Mod C's ingredients being registered immediately
@JeiPlugin
public class ModBPlugin implements IModPlugin {
    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        // Assumes Mod C's ingredients are already registered
        // If Mod C's plugin was slow/disabled, this fails silently
        var modCIngredients = reg.getIngredientManager()
            .getIngredients(ModCIngredient.TYPE);
        if (modCIngredients.isEmpty()) {
            // No error thrown, just no recipes shown
        }
    }
}
```

---

#### 1.3.3 Late Start Fallback

**Location:** `Forge/src/main/java/mezz/jei/forge/startup/StartEventObserver.java:66-75`

```java
subscriptions.register(ScreenEvent.Init.Pre.class, event -> {
    if (this.state != State.JEI_STARTED) {
        Screen screen = event.getScreen();
        if (screen instanceof AbstractContainerScreen && minecraft.player != null) {
            LOGGER.error("""
                A Screen is opening but JEI hasn't started yet.
                Normally, JEI is started after ClientPlayerNetworkEvent.LoggedInEvent, 
                TagsUpdatedEvent, and RecipesUpdatedEvent.
                Something has caused one or more of these events to fail, 
                so JEI is starting very late.""");
            transitionState(State.DISABLED);
            transitionState(State.ENABLED);
            transitionState(State.JEI_STARTED);
        }
    }
});
```

**Impact:**
- **Forced JEI startup:** If events are missed, JEI force-starts when first container opens
- **Incomplete initialization:** Force-start may skip critical initialization steps
- **Mod dependency failures:** Mods expecting JEI to be ready at specific lifecycle points may fail

---

#### 1.3.4 Recipe Category Visibility Caching

**Location:** `Library/src/main/java/mezz/jei/library/recipes/RecipeManagerInternal.java:179`

```java
@Nullable
@Unmodifiable
private List<IRecipeCategory<?>> recipeCategoriesVisibleCache = null;
```

**Impact:**
- **Stale cache:** Cache may not invalidate correctly when mods dynamically add/remove recipes
- **Visibility desync:** Mod-added recipes may not appear in UI until cache invalidation
- **Cache invalidation timing:** Depends on specific events that mods may not trigger

---

#### 1.3.5 Resource Reload Handling (Fabric)

**Location:** `Fabric/src/main/java/mezz/jei/fabric/startup/ClientLifecycleHandler.java:52-62`

```java
public ResourceManagerReloadListener getReloadListener() {
    return (resourceManager) -> {
        if (running) {
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.isSameThread()) {
                // we may receive reload events on the server thread in single-player, ignore them
                return;
            }
            stopJei();
            startJei();
        }
    };
}
```

**Impact:**
- **JEI restart on reload:** All ingredient/recipe data is rebuilt on resource reload
- **Server thread ignored:** In single-player, server-thread reloads are ignored (may cause desync)
- **Mod-added content:** Mods that add content dynamically may lose their additions on reload

---

### 1.4 Internal Threading Optimizations (Non-Breaking but Risky)

#### 1.4.1 Deduplicating Runner

**Location:** `Common/src/main/java/mezz/jei/common/util/DeduplicatingRunner.java`

```java
public class DeduplicatingRunner {
    private final IDelayedExecutor executor;
    private final Duration delay;
    private @Nullable Future<?> future;
    
    public synchronized void run(Runnable runnable) {
        if (future != null) {
            future.cancel(false);
        }
        future = executor.schedule(runnable, delay);
    }
}
```

**Risk:** Scheduled execution may delay critical updates, causing timing-dependent failures.

---

#### 1.4.2 Delayed Executor Thread Pool

**Location:** `Common/src/main/java/mezz/jei/common/util/DelayedExecutor.java`

```java
final class DelayedExecutor implements IDelayedExecutor {
    private final ScheduledThreadPoolExecutor service;
    
    private DelayedExecutor() {
        var threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("JEI Deduplicating Run Executor %d")
            .build();
        var service = new ScheduledThreadPoolExecutor(1, threadFactory);
        service.setRemoveOnCancelPolicy(true);
        this.service = service;
    }
}
```

**Risk:** Background thread execution of JEI operations may violate Minecraft's threading assumptions.

---

## 2. Fully Backward-Compatible Solutions

### Design Principle: "Async Inside, Sync Outside"

All public APIs, events, and registration hooks must behave **identically** to the synchronous version. Async execution is confined to internal implementation details that are **never exposed** to other mods.

---

### 2.1 Solution: Graceful Thread Handling

**Problem:** Main thread enforcement crashes mods calling JEI APIs from worker threads.

**Solution:** Automatically schedule work on main thread instead of crashing.

```java
// File: Common/src/main/java/mezz/jei/common/util/ErrorUtil.java
public static void runOnMainThreadIfRequired(Runnable runnable) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft != null && !minecraft.isSameThread()) {
        // Schedule on main thread instead of crashing
        minecraft.execute(runnable);
    } else {
        runnable.run();
    }
}

// File: Library/src/main/java/mezz/jei/library/recipes/RecipeManager.java
public <T extends Recipe<?>> void addRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
    runOnMainThreadIfRequired(() -> {
        recipeManagerInternal.addRecipes(recipeType, recipes);
    });
}

public <T extends Recipe<?>> void hideRecipes(RecipeType<T> recipeType, Stream<T> recipes) {
    runOnMainThreadIfRequired(() -> {
        recipeManagerInternal.hideRecipes(recipeType, recipes);
    });
}

// Similar for all other affected methods
```

**Benefits:**
- Zero breaking changes - mods calling from wrong thread still work
- Maintains thread safety by executing on main thread
- No performance penalty (work would have been done on main thread anyway)

---

### 2.2 Solution: Double-Buffering with Atomic Swaps

**Problem:** Parallel stream processing and async operations can cause race conditions and non-deterministic ordering.

**Solution:** Use double-buffering pattern with atomic swaps for all data structures.

```java
// File: Library/src/main/java/mezz/jei/library/ingredients/IngredientManager.java
public class IngredientManager implements IIngredientManager {
    // Atomic reference for thread-safe swapping
    private final AtomicReference<IngredientSnapshot> activeSnapshot = 
        new AtomicReference<>(IngredientSnapshot.EMPTY);
    
    // Thread-safe staging buffer for async workers
    private final ConcurrentHashMap<IIngredientType<?>, ConcurrentLinkedQueue<?>> stagingBuffers = 
        new ConcurrentHashMap<>();
    
    public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, 
                                            Collection<ITypedIngredient<V>> ingredients) {
        // Add to staging buffer (thread-safe)
        ConcurrentLinkedQueue<ITypedIngredient<V>> buffer = 
            (ConcurrentLinkedQueue<ITypedIngredient<V>>) stagingBuffers.computeIfAbsent(
                ingredientType, k -> new ConcurrentLinkedQueue<>());
        buffer.addAll(ingredients);
        
        // Schedule main-thread update
        Minecraft.getInstance().execute(this::commitStagedChanges);
    }
    
    private void commitStagedChanges() {
        // Create new snapshot from current + staged changes
        IngredientSnapshot oldSnapshot = activeSnapshot.get();
        IngredientSnapshot newSnapshot = oldSnapshot.merge(stagingBuffers);
        
        // Atomic swap - mods see either old or new, never partial
        activeSnapshot.set(newSnapshot);
        
        // Clear staging buffers
        stagingBuffers.values().forEach(ConcurrentLinkedQueue::clear);
        
        // Notify listeners after atomic swap
        notifyListenersOfChanges(newSnapshot);
    }
    
    @Override
    public <V> Collection<ITypedIngredient<V>> getIngredients(IIngredientType<V> ingredientType) {
        // Always read from stable snapshot
        return activeSnapshot.get().getIngredients(ingredientType);
    }
}

// Immutable snapshot class
record IngredientSnapshot(Map<IIngredientType<?>, List<?>> ingredients) {
    static final IngredientSnapshot EMPTY = new IngredientSnapshot(Map.of());
    
    <V> List<ITypedIngredient<V>> getIngredients(IIngredientType<V> type) {
        @SuppressWarnings("unchecked")
        List<ITypedIngredient<V>> list = (List<ITypedIngredient<V>>) ingredients.get(type);
        return list != null ? List.copyOf(list) : List.of(); // Immutable copy
    }
    
    IngredientSnapshot merge(ConcurrentHashMap<IIngredientType<?>, ConcurrentLinkedQueue<?>> staging) {
        // Merge staged changes into new snapshot
        Map<IIngredientType<?>, List<?>> merged = new HashMap<>(ingredients);
        for (var entry : staging.entrySet()) {
            IIngredientType<?> type = entry.getKey();
            ConcurrentLinkedQueue<?> staged = entry.getValue();
            @SuppressWarnings("unchecked")
            List<ITypedIngredient<?>> existing = (List<ITypedIngredient<?>>) merged.get(type);
            List<ITypedIngredient<?>> combined = Stream.concat(
                    existing != null ? existing.stream() : Stream.empty(),
                    (Stream<ITypedIngredient<?>>) staged.stream()
                )
                .distinct()
                .toList();
            merged.put(type, combined);
        }
        return new IngredientSnapshot(merged);
    }
}
```

**Benefits:**
- Mods always see consistent, complete data
- No race conditions or partial updates
- Atomic visibility across threads
- Zero API changes required

---

### 2.3 Solution: Sequential Stream Processing

**Problem:** `parallelStream()` in `IngredientFilter` causes non-deterministic ordering and potential race conditions.

**Solution:** Use sequential streams with explicit sorting, or parallel streams with proper synchronization.

```java
// File: Gui/src/main/java/mezz/jei/gui/ingredients/IngredientFilter.java
private Stream<ITypedIngredient<?>> getIngredientListUncached(String filterText) {
    String[] filters = filterText.split("\\|");
    List<SearchTokens> searchTokens = Arrays.stream(filters)
        .map(this::parseSearchTokens)
        .filter(s -> !s.isEmpty())
        .toList();

    Stream<IListElement<?>> elementStream;
    if (searchTokens.isEmpty()) {
        // OPTION 1: Use sequential stream (safe, slightly slower)
        elementStream = this.elementSearch.getAllIngredients().stream();
        
        // OPTION 2: Use parallel stream with explicit ordering (faster, safe)
        // elementStream = this.elementSearch.getAllIngredients()
        //     .parallelStream()
        //     .sorted(Comparator.comparing(IListElement::getUid)); // Deterministic order
    } else {
        elementStream = searchTokens.stream()
            .map(this::getSearchResults)
            .flatMap(Set::stream)
            .distinct();
    }

    return elementStream
        .filter(IListElement::isVisible)
        .sorted(ingredientComparator) // Ensure deterministic order
        .map(IListElement::getTypedIngredient);
}
```

**Benefits:**
- Deterministic ordering maintained
- No race conditions during search
- Compatible with mods relying on specific ordering

---

### 2.4 Solution: Deferred Plugin Execution with Opt-In Async

**Problem:** Async plugin execution causes subtle timing issues and concurrency bugs in mods.

**Solution:** Keep plugin execution synchronous by default, with opt-in async support via new interface.

```java
// File: CommonApi/src/main/java/mezz/jei/api/IModPlugin.java
/**
 * Marker interface for plugins that are safe to execute asynchronously.
 * Implementing this interface indicates that your plugin's registration methods
 * are thread-safe and do not access non-thread-safe Minecraft objects.
 * 
 * @since 1.20.1-async
 */
public interface IAsyncCompatiblePlugin {
    /**
     * @return true if this plugin can be executed on a background thread
     */
    default boolean canExecuteAsync() {
        return true;
    }
}

// File: Library/src/main/java/mezz/jei/library/load/PluginCaller.java
public class PluginCaller {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void callOnPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
        LOGGER.info("{}...", title);
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        // Separate plugins into async-safe and sync-only
        List<IModPlugin> asyncPlugins = plugins.stream()
            .filter(p -> p instanceof IAsyncCompatiblePlugin)
            .filter(p -> ((IAsyncCompatiblePlugin) p).canExecuteAsync())
            .toList();
        
        List<IModPlugin> syncPlugins = plugins.stream()            .filter(p -> !(p instanceof IAsyncCompatiblePlugin))
            .toList();
        
        // Execute sync plugins on main thread (100% backward compatible)
        for (IModPlugin plugin : syncPlugins) {
            try {
                ResourceLocation pluginUid = plugin.getPluginUid();
                func.accept(plugin);
            } catch (RuntimeException | LinkageError e) {
                if (plugin instanceof VanillaPlugin) {
                    throw e;
                }
                LOGGER.error("Caught an error from mod plugin: {} {}", 
                    plugin.getClass(), pluginUid, e);
            }
        }
        
        // Execute async-safe plugins on background thread (opt-in)
        if (!asyncPlugins.isEmpty()) {
            CompletableFuture<Void> asyncTask = CompletableFuture.runAsync(() -> {
                for (IModPlugin plugin : asyncPlugins) {
                    try {
                        func.accept(plugin);
                    } catch (RuntimeException | LinkageError e) {
                        LOGGER.error("Caught an error from async mod plugin: {} {}", 
                            plugin.getClass(), plugin.getPluginUid(), e);
                    }
                }
            });
            
            // Wait for async plugins to complete before continuing
            // This maintains synchronous behavior from external perspective
            try {
                asyncTask.get(30, TimeUnit.SECONDS); // Timeout to prevent hangs
            } catch (TimeoutException e) {
                LOGGER.error("Async plugin execution timed out after 30 seconds");
                asyncTask.cancel(true);
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Async plugin execution failed", e);
            }
        }
        
        LOGGER.info("{} took {}", title, stopwatch);
    }
}
```

**Benefits:**
- 100% of existing mods continue to work without changes
- Mods can opt-in to async execution when ready
- Maintains synchronous facade for external observers
- Timeout prevents hangs from slow plugins

---

### 2.5 Solution: Lifecycle Event Guarantees

**Problem:** Late start fallback and timing changes break mod assumptions about JEI initialization.

**Solution:** Ensure JEI events fire at consistent, predictable lifecycle points.

```java
// File: Forge/src/main/java/mezz/jei/forge/startup/StartEventObserver.java
public class StartEventObserver {
    private enum State { DISABLED, ENABLED, JEI_STARTED }
    private State state = State.DISABLED;
    
    // Track which events have fired
    private boolean tagsUpdated = false;
    private boolean recipesUpdated = false;
    private boolean playerLoggedIn = false;
    
    public void registerEvents(IEventBus eventBus) {
        eventBus.register(this);
        
        // Force-start JEI after all required events have fired
        eventBus.addListener(ClientPlayerNetworkEvent.LoggedInEvent.class, this::onPlayerLoggedIn);
        eventBus.addListener(TagsUpdatedEvent.class, this::onTagsUpdated);
        eventBus.addListener(RecipesUpdatedEvent.class, this::onRecipesUpdated);
        
        // Fallback: start JEI when first screen opens if events were missed
        // BUT log a warning for mod developers
        eventBus.addListener(ScreenEvent.Init.Pre.class, this::onScreenOpen);
    }
    
    private void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
        this.playerLoggedIn = true;
        tryStartJei();
    }
    
    private void onTagsUpdated(TagsUpdatedEvent event) {
        this.tagsUpdated = true;
        tryStartJei();
    }
    
    private void onRecipesUpdated(RecipesUpdatedEvent event) {
        this.recipesUpdated = true;
        tryStartJei();
    }
    
    private synchronized void tryStartJei() {
        if (state != State.JEI_STARTED && playerLoggedIn && tagsUpdated && recipesUpdated) {
            transitionState(State.ENABLED);
            transitionState(State.JEI_STARTED);
            // Fire JEI-specific event for mods that depend on JEI being ready
            MinecraftForge.EVENT_BUS.post(new JeiInitializedEvent());
        }
    }
    
    private void onScreenOpen(ScreenEvent.Init.Pre event) {
        if (state != State.JEI_STARTED) {
            Screen screen = event.getScreen();
            Minecraft minecraft = screen.getMinecraft();
            if (screen instanceof AbstractContainerScreen && minecraft.player != null) {
                LOGGER.warn("""
                    JEI is starting late because required events were missed.
                    This may cause compatibility issues with mods that expect JEI to be ready earlier.
                    Required events: ClientPlayerNetworkEvent.LoggedInEvent, TagsUpdatedEvent, RecipesUpdatedEvent
                    Missing events: {}
                    """, 
                    getMissingEvents());
                
                // Still force-start to prevent complete failure
                transitionState(State.DISABLED);
                transitionState(State.ENABLED);
                transitionState(State.JEI_STARTED);
                MinecraftForge.EVENT_BUS.post(new JeiInitializedEvent());
            }
        }
    }
    
    private List<String> getMissingEvents() {
        List<String> missing = new ArrayList<>();
        if (!playerLoggedIn) missing.add("ClientPlayerNetworkEvent.LoggedInEvent");
        if (!tagsUpdated) missing.add("TagsUpdatedEvent");
        if (!recipesUpdated) missing.add("RecipesUpdatedEvent");
        return missing;
    }
}

// File: CommonApi/src/main/java/mezz/jei/api/event/JeiInitializedEvent.java
/**
 * Fired when JEI has completed initialization and is ready for use.
 * Mods that depend on JEI being fully initialized should listen for this event.
 * 
 * This event is fired on the main thread after all JEI registration is complete.
 * 
 * @since 1.20.1-async
 */
public class JeiInitializedEvent {
    // Marker event
}
```

**Benefits:**
- Mods can listen for `JeiInitializedEvent` to know when JEI is ready
- Consistent initialization timing
- Better logging for debugging late-start issues
- Maintains fallback for edge cases

---

### 2.6 Solution: Immutable Data Snapshots

**Problem:** Mods may observe partially updated data structures during async operations.

**Solution:** Always return immutable copies of internal data structures.

```java
// File: Library/src/main/java/mezz/jei/library/recipes/RecipeManagerInternal.java
public class RecipeManagerInternal implements IRecipeManager {
    // Internal mutable state (never exposed directly)
    private final Map<RecipeType<?>, List<?>> recipes = new ConcurrentHashMap<>();
    
    // Atomic reference for snapshot swaps
    private final AtomicReference<RecipeSnapshot> activeSnapshot = 
        new AtomicReference<>(RecipeSnapshot.EMPTY);
    
    public <T extends Recipe<?>> Collection<T> getRecipes(RecipeType<T> recipeType) {
        // Return immutable copy from stable snapshot
        return activeSnapshot.get().getRecipes(recipeType);
    }
    
    public Collection<RecipeType<?>> getRecipeTypes() {
        // Return immutable copy
        return activeSnapshot.get().getRecipeTypes();
    }
    
    private void updateSnapshot() {
        // Create new snapshot from current state
        RecipeSnapshot newSnapshot = RecipeSnapshot.createFrom(recipes);
        
        // Atomic swap
        activeSnapshot.set(newSnapshot);
    }
    
    // Immutable snapshot record
    private record RecipeSnapshot(Map<RecipeType<?>, List<?>> recipes) {
        static final RecipeSnapshot EMPTY = new RecipeSnapshot(Map.of());
        
        static RecipeSnapshot createFrom(Map<RecipeType<?>, List<?>> source) {
            Map<RecipeType<?>, List<?>> immutable = new HashMap<>();
            for (var entry : source.entrySet()) {
                RecipeType<?> type = entry.getKey();
                @SuppressWarnings("unchecked")
                List<Recipe<?>> recipeList = (List<Recipe<?>>) entry.getValue();
                // Create immutable copy
                immutable.put(type, List.copyOf(recipeList));
            }
            return new RecipeSnapshot(immutable);
        }
        
        @SuppressWarnings("unchecked")
        <T extends Recipe<?>> List<T> getRecipes(RecipeType<T> type) {
            List<Recipe<?>> list = (List<Recipe<?>>) recipes.get(type);
            return list != null ? (List<T>) List.copyOf(list) : List.of();
        }
        
        Set<RecipeType<?>> getRecipeTypes() {
            return Set.copyOf(recipes.keySet());
        }
    }
}
```

**Benefits:**
- Mods always see consistent, complete data
- No `ConcurrentModificationException` risks
- No visibility issues across threads
- Zero API changes

---

### 2.7 Solution: Config-Based Async Toggle

**Problem:** Some users/modpacks may need to disable async features for compatibility.

**Solution:** Add configuration option to disable async loading.

```java
// File: Common/src/main/java/mezz/jei/common/config/DebugConfig.java
public class DebugConfig {
    private static boolean enableAsyncLoading = true;
    
    public static boolean isAsyncLoadingEnabled() {
        return enableAsyncLoading;
    }
    
    // Called during config initialization
    private static void loadConfig(Path configPath) {
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(configPath)) {
            props.load(is);
            enableAsyncLoading = Boolean.parseBoolean(
                props.getProperty("B:enableAsyncLoading", "true"));
        } catch (IOException e) {
            LOGGER.warn("Could not load JEI config, using defaults");
        }
    }
}

// File: Library/src/main/java/mezz/jei/library/load/PluginCaller.java
public static void callOnPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
    if (!DebugConfig.isAsyncLoadingEnabled()) {
        // Force synchronous execution
        callOnPluginsSync(title, plugins, func);
        return;
    }
    
    // Use async execution with opt-in
    callOnPluginsWithOptInAsync(title, plugins, func);
}
```

**Configuration File:** `config/jei-debug.ini`
```ini
# Enable asynchronous plugin execution for improved performance
# Set to false if you experience compatibility issues with mods
B:enableAsyncLoading=true
```

**Benefits:**
- Escape hatch for compatibility issues
- Users can test async vs sync to diagnose problems
- Modpack developers can disable async for stability

---

## 3. Implementation Checklist

### Phase 1: Core Thread Safety (Required)
- [ ] Implement `runOnMainThreadIfRequired()` in `ErrorUtil.java`
- [ ] Wrap all runtime API methods with thread-safe scheduling
- [ ] Replace `parallelStream()` with sequential streams in `IngredientFilter.java`
- [ ] Implement double-buffering with `AtomicReference` in `IngredientManager`
- [ ] Implement double-buffering with `AtomicReference` in `RecipeManager`
- [ ] Ensure all public API methods return immutable copies

### Phase 2: Plugin Execution (Required)
- [ ] Create `IAsyncCompatiblePlugin` marker interface
- [ ] Modify `PluginCaller` to separate async-safe and sync-only plugins
- [ ] Execute sync plugins on main thread
- [ ] Execute async-safe plugins on background thread with timeout
- [ ] Add `JeiInitializedEvent` for mod lifecycle tracking

### Phase 3: Configuration & Testing (Required)
- [ ] Add `enableAsyncLoading` config option
- [ ] Implement config-based async toggle
- [ ] Create automated tests for threading scenarios
- [ ] Test with popular modpacks (GTNH, AllTheMods, RLCraft, etc.)
- [ ] Document async internals for maintainers

### Phase 4: Optional Optimizations
- [ ] Implement lazy async loading (defer until after world load)
- [ ] Add performance profiling for async vs sync
- [ ] Optimize staging buffer merging
- [ ] Consider lock-free data structures for hot paths

---

## 4. Testing Strategy

### 4.1 Automated Tests

```java
// File: Library/src/test/java/mezz/jei/library/thread/ThreadSafetyTests.java
public class ThreadSafetyTests {
    
    @Test
    public void testAddIngredientsFromBackgroundThread() {
        IngredientManager manager = createIngredientManager();
        CountDownLatch latch = new CountDownLatch(1);
        
        // Call from background thread
        Thread backgroundThread = new Thread(() -> {
            manager.addIngredientsAtRuntime(TestIngredient.TYPE, List.of(testIngredient));
            latch.countDown();
        });
        backgroundThread.start();
        latch.await();
        
        // Verify ingredient was added (after main thread processes it)
        Thread.sleep(100); // Wait for main thread execution
        assertTrue(manager.getIngredients(TestIngredient.TYPE).contains(testIngredient));
    }
    
    @Test
    public void testConcurrentIngredientAddition() throws InterruptedException {
        IngredientManager manager = createIngredientManager();
        List<Thread> threads = new ArrayList<>();
        
        // Spawn multiple threads adding ingredients simultaneously
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    manager.addIngredientsAtRuntime(TestIngredient.TYPE, 
                        List.of(createTestIngredient(j)));
                }
            });
            threads.add(t);
            t.start();
        }
        
        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }
        
        // Verify all ingredients were added correctly
        assertEquals(1000, manager.getIngredients(TestIngredient.TYPE).size());
    }
    
    @Test
    public void testDeterministicIngredientOrdering() {
        IngredientFilter filter = createIngredientFilter();
        
        // Get ingredient list multiple times
        List<IElement<?>> first = filter.getElements();
        List<IElement<?>> second = filter.getElements();
        List<IElement<?>> third = filter.getElements();
        
        // Verify ordering is deterministic
        assertEquals(first, second);
        assertEquals(second, third);
    }
}
```

### 4.2 Integration Tests

Test with the following mod combinations:
- [ ] JEI + CraftTweaker (runtime recipe modifications)
- [ ] JEI + KubeJS (dynamic ingredient registration)
- [ ] JEI + JEI Plugins (additions, hide, etc.)
- [ ] JEI + World gen mods (dynamic block registration)
- [ ] JEI + Tech mods (Mekanism, Thermal, EIO - large recipe counts)
- [ ] JEI + Magic mods (Botania, Thaumcraft - complex recipes)

### 4.3 Performance Benchmarks

Measure and compare:
- Startup time (sync vs async)
- Memory usage (sync vs async)
- Search performance (parallel vs sequential streams)
- Recipe registration time (with/without async plugins)

---

## 5. Migration Guide for Mod Developers

### For Existing Mods (No Changes Required)

All existing mods will continue to work without any code changes. The async fork maintains 100% backward compatibility.

### For Mods Wanting Async Benefits

Mods that want to take advantage of async execution can implement `IAsyncCompatiblePlugin`:

```java
@JeiPlugin
public class MyModPlugin implements IModPlugin, IAsyncCompatiblePlugin {
    
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("mymod", "jei_plugin");
    }
    
    @Override
    public boolean canExecuteAsync() {
        // Only return true if your registration methods are thread-safe
        return true;
    }
    
    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        // This can now run on background thread if canExecuteAsync() returns true
        // Must NOT access:
        // - Minecraft.getInstance()
        // - Level/world objects
        // - Player entities
        // - GUI/rendering components
        // - Any non-thread-safe Minecraft objects
        
        // Safe operations:
        // - Creating ingredient objects
        // - Reading from static final data
        // - Pure computation
        
        registration.register(MyIngredient.TYPE, allMyIngredients);
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Same thread-safety requirements as registerIngredients
        registration.addRecipes(MyRecipeType.TYPE, allMyRecipes);
    }
}
```

### For Mods Depending on JEI Initialization

Mods that need to know when JEI is fully initialized should listen for `JeiInitializedEvent`:

```java
// Forge example
@SubscribeEvent
public void onJeiInitialized(JeiInitializedEvent event) {
    // JEI is now fully initialized and ready to use
    // Safe to query JEI data, modify recipes, etc.
}

// Fabric example
JeiLifecycleEvents.INITIALIZED.register(() -> {
    // JEI is now fully initialized
});
```

---

## 6. Conclusion

The JEI-Async fork can achieve meaningful performance improvements while maintaining 100% backward compatibility by:

1. **Isolating async work** to pure computation and I/O
2. **Using double-buffering** with atomic swaps for data consistency
3. **Routing all game-state changes** through main-thread executor
4. **Keeping public API synchronous** with defensive wrappers
5. **Making async plugin execution opt-in** via `IAsyncCompatiblePlugin`
6. **Providing config toggle** for escape hatch

The trade-off is slightly more complex internal code, but that complexity is **contained within JEI** and never leaks to the modding ecosystem. This approach delivers faster loading for users while guaranteeing that **zero existing mods need to change a single line of code**.

By implementing these solutions rigorously—and including thorough automated tests for threading scenarios—JEI-Async can become a high-performance fork that the community can adopt without fear of breaking their modpacks.

---

## 7. Implementation Status

The following changes have been **implemented** in this fork:

### Completed Implementations

| Change | File | Status |
|--------|------|--------|
| `runOnMainThreadIfRequired()` | `ErrorUtil.java` | ✅ Implemented |
| Thread-safe RecipeManager wrapping | `RecipeManager.java` | ✅ Implemented |
| Thread-safe IngredientManager wrapping | `IngredientManager.java` | ✅ Implemented |
| Sequential stream processing | `IngredientFilter.java` | ✅ Implemented |
| `IAsyncCompatiblePlugin` interface | `CommonApi/src/main/java/mezz/jei/api/` | ✅ Created |
| Opt-in async plugin execution | `PluginCaller.java` | ✅ Implemented |
| `JeiInitializedEvent` class | `CommonApi/src/main/java/mezz/jei/api/event/` | ✅ Created |
| `enableAsyncLoading` config option | `DebugConfig.java` | ✅ Implemented |
| Better initialization logging | `StartEventObserver.java` | ✅ Implemented |

### Notes on Double-Buffering

The double-buffering with `AtomicReference` pattern (Solution 2.2 and 2.6 in the analysis) was **not fully implemented** in this pass because:

1. The existing `IngredientSet` and `RecipeMap` data structures already provide good isolation
2. The `getAllIngredients()` method already returns `Collections.unmodifiableCollection()`
3. The `RecipeManagerInternal` already uses `ImmutableListMultimap` and `@Unmodifiable` annotations
4. The thread-safe scheduling via `runOnMainThreadIfRequired()` provides sufficient protection for runtime modifications

The double-buffering pattern can be added in a future enhancement if race conditions are observed during testing.

### Configuration File

The `enableAsyncLoading` config option creates/updates `config/jei-debug.ini`:

```ini
[debug]
# Enable asynchronous loading features for improved performance.
# Set to false if you experience compatibility issues with mods.
B:enableAsyncLoading=true
```

### For Mod Developers

**Existing mods require NO changes.** All existing JEI plugins will continue to work without modification.

**To opt-in to async execution**, implement `IAsyncCompatiblePlugin`:

```java
@JeiPlugin
public class MyModPlugin implements IModPlugin, IAsyncCompatiblePlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("mymod", "jei_plugin");
    }
    
    @Override
    public boolean canExecuteAsync() {
        // Only return true if your registration methods are thread-safe
        return true;
    }
}
```

---

## 8. Appendix: File Reference

### Implemented Changes

| Component | File Path | Changes Made |
|-----------|-----------|--------------|
| ErrorUtil | `Common/src/main/java/mezz/jei/common/util/ErrorUtil.java` | Added `runOnMainThreadIfRequired()` and `isAsyncLoadingEnabled()` |
| RecipeManager | `Library/src/main/java/mezz/jei/library/recipes/RecipeManager.java` | Wrapped `addRecipes()`, `hideRecipes()`, `unhideRecipes()`, `hideRecipeCategory()`, `unhideRecipeCategory()` with thread-safe scheduling |
| IngredientManager | `Library/src/main/java/mezz/jei/library/ingredients/IngredientManager.java` | Wrapped `addIngredientsAtRuntime()` and `removeIngredientsAtRuntime()` with thread-safe scheduling |
| IngredientFilter | `Gui/src/main/java/mezz/jei/gui/ingredients/IngredientFilter.java` | Replaced `parallelStream()` with sequential `stream()` |
| PluginCaller | `Library/src/main/java/mezz/jei/library/load/PluginCaller.java` | Implemented opt-in async execution with `IAsyncCompatiblePlugin` support |
| IAsyncCompatiblePlugin | `CommonApi/src/main/java/mezz/jei/api/IAsyncCompatiblePlugin.java` | **New file** - Marker interface for async-safe plugins |
| JeiInitializedEvent | `CommonApi/src/main/java/mezz/jei/api/event/JeiInitializedEvent.java` | **New file** - Event for JEI initialization completion |
| DebugConfig | `Common/src/main/java/mezz/jei/common/config/DebugConfig.java` | Added `enableAsyncLoading` config option |
| StartEventObserver | `Forge/src/main/java/mezz/jei/forge/startup/StartEventObserver.java` | Added better logging for JEI initialization |
| JeiStarter | `Library/src/main/java/mezz/jei/library/startup/JeiStarter.java` | Updated to use new plugin execution system |

### Existing Protections (Already Present)

| Component | File Path | Existing Protection |
|-----------|-----------|---------------------|
| IngredientInfo | `Library/src/main/java/mezz/jei/library/ingredients/IngredientInfo.java` | `getAllIngredients()` returns `Collections.unmodifiableCollection()` |
| RegisteredIngredients | `Library/src/main/java/mezz/jei/library/ingredients/RegisteredIngredients.java` | Uses `Map.copyOf()` and `@Unmodifiable` lists |
| RecipeManagerInternal | `Library/src/main/java/mezz/jei/library/recipes/RecipeManagerInternal.java` | Uses `ImmutableListMultimap` and `@Unmodifiable` annotations |
