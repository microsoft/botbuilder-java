// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.ComponentRegistration;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogPath;
import com.microsoft.bot.dialogs.DialogsComponentRegistration;
import com.microsoft.bot.dialogs.ObjectPath;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;
import com.microsoft.bot.schema.ResultPair;

import org.apache.commons.lang3.StringUtils;

/**
 * The DialogStateManager manages memory scopes and pathresolvers MemoryScopes
 * are named root level Objects, which can exist either in the dialogcontext or
 * off of turn state PathResolvers allow for shortcut behavior for mapping
 * things like $foo to dialog.foo.
 */
public class DialogStateManager implements Map<String, Object> {

    /**
     * Information for tracking when path was last modified.
     */
    private final String pathTracker = "dialog._tracker.paths";

    private static final char[] SEPARATORS = {',', '[' };

    private final DialogContext dialogContext;
    private int version;

    private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    /**
     * Initializes a new instance of the
     * {@link com.microsoft.bot.dialogs.memory.DialogStateManager} class.
     *
     * @param dc            The dialog context for the current turn of the
     *                      conversation.
     */
    public DialogStateManager(DialogContext dc) {
        this(dc, null);
    }

    /**
     * Initializes a new instance of the
     * {@link com.microsoft.bot.dialogs.memory.DialogStateManager} class.
     *
     * @param dc            The dialog context for the current turn of the
     *                      conversation.
     * @param configuration Configuration for the dialog state manager.
     */
    public DialogStateManager(DialogContext dc, DialogStateManagerConfiguration configuration) {
        ComponentRegistration.add(new DialogsComponentRegistration());

        if (dc != null) {
            dialogContext = dc;
        } else {
            throw new IllegalArgumentException("dc cannot be null.");
        }

        if (configuration != null) {
            this.configuration = configuration;
        } else {
            this.configuration = dc.getContext().getTurnState().get(DialogStateManagerConfiguration.class.getName());
        }

        if (this.configuration == null) {
            this.configuration = new DialogStateManagerConfiguration();

            Map<String, Object> turnStateServices = dc.getContext().getTurnState().getTurnStateServices();
            for (Map.Entry<String, Object> entry : turnStateServices.entrySet()) {
                if (entry.getValue() instanceof MemoryScope[]) {
                    this.configuration.getMemoryScopes().addAll(Arrays.asList((MemoryScope[]) entry.getValue()));
                }
                if (entry.getValue() instanceof PathResolver[]) {
                    this.configuration.getPathResolvers().addAll(Arrays.asList((PathResolver[]) entry.getValue()));
                }
            }

            Iterable<Object> components = ComponentRegistration.getComponents();

            components.forEach((component) -> {
                if (component instanceof ComponentMemoryScopes) {
                    ((ComponentMemoryScopes) component).getMemoryScopes().forEach((scope) -> {
                        this.configuration.getMemoryScopes().add(scope);
                    });
                }
                if (component instanceof ComponentPathResolvers) {
                    ((ComponentPathResolvers) component).getPathResolvers().forEach((pathResolver) -> {
                        this.configuration.getPathResolvers().add(pathResolver);
                    });
                }
            });
        }
        // cache for any other new dialogStatemanager instances in this turn.
        dc.getContext().getTurnState().replace(this.configuration);
    }

    private DialogStateManagerConfiguration configuration;

    /**
     * Sets the configured path resolvers and memory scopes for the dialog.
     *
     * @return The DialogStateManagerConfiguration.
     */
    public DialogStateManagerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configured path resolvers and memory scopes for the dialog.
     *
     * @param withDialogStateManagerConfiguration The configuration to set.
     */
    public void setConfiguration(DialogStateManagerConfiguration withDialogStateManagerConfiguration) {
        this.configuration = withDialogStateManagerConfiguration;
    }

    /**
     * Gets a value indicating whether the dialog state manager is read-only.
     *
     * @return true
     */
    public Boolean getIsReadOnly() {
        return true;
    }

    /**
     * Gets the elements with the specified key.
     *
     * @param key Key to get or set the element.
     * @return The element with the specified key.
     */
    public Object getElement(String key) {
        // return GetValue(key);
        return null;
    }

    /**
     * Sets the elements with the specified key.
     *
     * @param key     Key to get or set the element.
     * @param element The element to store with the provided key.
     */
    public void setElement(String key, Object element) {
        if (key.indexOf(SEPARATORS[0]) == -1 && key.indexOf(SEPARATORS[1]) == -1) {
            MemoryScope scope = getMemoryScope(key);
            if (scope != null) {
                try {
                    scope.setMemory(dialogContext, mapper.writeValueAsString(element));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                throw new IllegalArgumentException(getBadScopeMessage(key));
            }
        }
    }

    /**
     * Get MemoryScope by name.
     *
     * @param name Name of scope.
     * @return A memory scope.
     */
    public MemoryScope getMemoryScope(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        Optional<MemoryScope> result = configuration.getMemoryScopes().stream()
                .filter((scope) -> scope.getName().equalsIgnoreCase(name))
                .findFirst();
        return result.isPresent() ? result.get() : null;
    }

    /**
     * Version help caller to identify the updates and decide cache or not.
     *
     * @return Current version
     */
    public String version() {
        return Integer.toString(version);
    }

    /**
     * ResolveMemoryScope will find the MemoryScope for and return the remaining
     * path.
     *
     * @param path          Incoming path to resolve to scope and remaining path.
     * @param remainingPath Remaining subpath in scope.
     * @return The memory scope.
     */
    public MemoryScope resolveMemoryScope(String path, StringBuilder remainingPath) {
        String scope = path;
        int sepIndex = -1;
        int dot = StringUtils.indexOfIgnoreCase(path, ".");
        int openSquareBracket = StringUtils.indexOfIgnoreCase(path, "[");

        if (dot > 0 && openSquareBracket > 0) {
            sepIndex = Math.min(dot, openSquareBracket);
        } else if (dot > 0) {
            sepIndex = dot;
        } else if (openSquareBracket > 0) {
            sepIndex = openSquareBracket;
        }

        if (sepIndex > 0) {
            scope = path.substring(0, sepIndex);
            MemoryScope memoryScope = getMemoryScope(scope);
            if (memoryScope != null) {
                remainingPath.append(path.substring(sepIndex + 1));
                return memoryScope;
            }
        }

        MemoryScope resultScope = getMemoryScope(scope);
        if (resultScope == null) {
            throw new IllegalArgumentException(getBadScopeMessage(path));
        } else {
            return resultScope;
        }
    }

    /**
     * Transform the path using the registered PathTransformers.
     *
     * @param path Path to transform.
     * @return The transformed path.
     */
    public String transformPath(String path) {
        List<PathResolver> resolvers = configuration.getPathResolvers();

        for (PathResolver resolver : resolvers) {
            path = resolver.transformPath(path);
        }

        return path;
    }

    /**
     * Get the value from memory using path expression (NOTE: This always returns
     * clone of value).
     *
     * @param <TypeT> the value type to return.
     * @param path    path expression to use.
     * @param clsType the Type that is being requested as a result
     * @return ResultPair with boolean and requested type TypeT as a result
     */
    public <TypeT> ResultPair<TypeT> tryGetValue(String path, Class<TypeT> clsType) {
        TypeT instance = null;

        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        path = transformPath(path);

        MemoryScope memoryScope = null;
        StringBuilder remainingPath = new StringBuilder();

        try {
            memoryScope = resolveMemoryScope(path, remainingPath);
        } catch (Exception err) {
            // Trace.TraceError(err.Message);
            return new ResultPair<>(false, instance);
        }

        if (memoryScope == null) {
            return new ResultPair<>(false, instance);
        }

        if (remainingPath.length() == 0) {
            Object memory = memoryScope.getMemory(dialogContext);
            if (memory == null) {
                return new ResultPair<>(false, instance);
            }

            instance = (TypeT) ObjectPath.mapValueTo(memory, clsType);

            return new ResultPair<>(true, instance);
        }

        // HACK to support .First() retrieval on turn.recognized.entities.foo,
        // replace with Expressions
        // once expression ship
        final String first = ".FIRST()";
        int iFirst = path.toUpperCase(Locale.US).lastIndexOf(first);
        if (iFirst >= 0) {
            remainingPath = new StringBuilder(path.substring(iFirst + first.length()));
            path = path.substring(0, iFirst);
            ResultPair<Object> getResult = tryGetFirstNestedValue(new AtomicReference<String>(path), this);
            if (getResult.result()) {
                if (StringUtils.isEmpty(remainingPath.toString())) {
                    instance = (TypeT) ObjectPath.mapValueTo(getResult.getRight(), clsType);
                    return new ResultPair<>(true, instance);
                }
                instance = (TypeT) ObjectPath.tryGetPathValue(getResult.getRight(), remainingPath.toString(), clsType);

                return new ResultPair<>(true, instance);
            }

            return new ResultPair<>(false, instance);
        }

        instance = (TypeT) ObjectPath.tryGetPathValue(this, path, clsType);

        return new ResultPair<>(instance != null, instance);
    }

    /**
     * Get the value from memory using path expression (NOTE: This always returns
     * clone of value).
     *
     * @param <T>            The value type to return.
     * @param pathExpression Path expression to use.
     * @param defaultValue   Default value to return if there is none found.
     * @param clsType        Type of value that is being requested as a return.
     * @return Result or the default value if the path is not valid.
     */
    public <T> T getValue(String pathExpression, T defaultValue, Class<T> clsType) {
        if (pathExpression == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        ResultPair<T> result = tryGetValue(pathExpression, clsType);
        if (result.result()) {
            return result.value();
        } else {
            return defaultValue;
        }
    }

    /**
     * Get a int value from memory using a path expression.
     *
     * @param pathExpression Path expression.
     * @param defaultValue   Default value if the value doesn't exist.
     * @return Value or default value if path is not valid.
     */
    public int getIntValue(String pathExpression, int defaultValue) {
        if (pathExpression == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        ResultPair<Integer> result = tryGetValue(pathExpression, Integer.class);
        if (result.result()) {
            return result.value();
        } else {
            return defaultValue;
        }
    }

    /**
     * Get a boolean value from memory using a path expression.
     *
     * @param pathExpression Path expression.
     * @param defaultValue   Default value if the value doesn't exist.
     * @return Value or default value if path is not valid.
     */
    public Boolean getBoolValue(String pathExpression, Boolean defaultValue) {
        if (pathExpression == null || StringUtils.isEmpty(pathExpression)) {
            throw new IllegalArgumentException("path cannot be null");
        }

        ResultPair<Boolean> result = tryGetValue(pathExpression, Boolean.class);
        if (result.result()) {
            return result.value();
        } else {
            return defaultValue;
        }
    }

    /**
     * Get a String value from memory using a path expression.
     *
     * @param pathExpression The path expression.
     * @param defaultValue   Default value if the value doesn't exist.
     * @return String or default value if path is not valid.
     */
    public String getStringValue(String pathExpression, String defaultValue) {
        return getValue(pathExpression, defaultValue, String.class);
    }

    /**
     * Set memory to value.
     *
     * @param path  Path to memory.
     * @param value Object to set.
     */
    public void setValue(String path, Object value) {
        if (value instanceof CompletableFuture) {
            throw new IllegalArgumentException(
                    String.format("%s = You can't pass an unresolved CompletableFuture to SetValue", path));
        }

        if (path == null || StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path cannot be null");
        }

        if (value != null) {
            value = mapper.valueToTree(value);
        }

        path = transformPath(path);
        if (trackChange(path, value)) {
            ObjectPath.setPathValue(this, path, value);
        }

        // Every set will increase version
        version++;
    }

    /**
     * Remove property from memory.
     *
     * @param path Path to remove the leaf property.
     */
    public void removeValue(String path) {
        if (!StringUtils.isNotBlank(path)) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        path = transformPath(path);
        if (trackChange(path, null)) {
            ObjectPath.removePathValue(this, path);
        }
    }

    /**
     * Gets all memoryscopes suitable for logging.
     *
     * @return JsonNode that which represents all memory scopes.
     */
    public JsonNode getMemorySnapshot() {
        ObjectNode result = mapper.createObjectNode();

        List<MemoryScope> scopes = configuration.getMemoryScopes().stream().filter((x) -> x.getIncludeInSnapshot())
                .collect(Collectors.toList());
        for (MemoryScope scope : scopes) {
            Object memory = scope.getMemory(dialogContext);
            if (memory != null) {
                try {
                    result.put(scope.getName(), mapper.writeValueAsString(memory));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * Load all of the scopes.
     *
     * @return A Completed Future.
     */
    public CompletableFuture<Void> loadAllScopes() {
        configuration.getMemoryScopes().forEach((scope) -> {
            scope.load(dialogContext, false).join();
        });
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Save all changes for all scopes.
     *
     * @return Completed Future
     */
    public CompletableFuture<Void> saveAllChanges() {
        configuration.getMemoryScopes().forEach((memoryScope) -> {
            memoryScope.saveChanges(dialogContext, false).join();
        });
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Delete the memory for a scope.
     *
     * @param name name of the scope
     * @return Completed CompletableFuture
     */
    public CompletableFuture<Void> deleteScopesMemory(String name) {
        // Make a copy here that is final so it can be used in lamdba expression below
        final String uCaseName = name.toUpperCase();
        MemoryScope scope = configuration.getMemoryScopes().stream().filter((s) -> {
            return s.getName().toUpperCase() == uCaseName;
        }).findFirst().get();
        if (scope != null) {
            return scope.delete(dialogContext).thenApply(result -> null);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Adds an element to the dialog state manager.
     *
     * @param key   Key of the element to add.
     * @param value Value of the element to add.
     */
    public void add(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether the dialog state manager contains an element with the
     * specified key.
     *
     * @param key The key to locate in the dialog state manager.
     * @return true if the dialog state manager contains an element with the key;
     *         otherwise, false.
     */
    public Boolean containsKey(String key) {
        for (MemoryScope scope : configuration.getMemoryScopes()) {
            if (scope.getName().toUpperCase().equals(key.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the element with the specified key from the dialog state manager.
     *
     * @param key The key of the element to remove.
     * @return true if the element is succesfully removed; otherwise, false.
     */
    public Boolean remove(String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the value associated with the specified key.
     *
     * @param key   The key whose value to get.
     * @param value When this method returns, the value associated with the
     *              specified key, if the key is found; otherwise, the default value
     *              for the type of the value parameter.
     * @return true if the dialog state manager contains an element with the
     *         specified key;
     */
    public ResultPair<Object> tryGetValue(String key, Object value) {
        return tryGetValue(key, Object.class);
    }

    /**
     * Adds an item to the dialog state manager.
     *
     * @param item The SimpleEntry with the key and Object of the item to add.
     */
    public void add(SimpleEntry<String, Object> item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all items from the dialog state manager.
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether the dialog state manager contains a specific value.
     *
     * @param item The of the item to locate.
     * @return True if item is found in the dialog state manager; otherwise,false
     */
    public Boolean contains(SimpleEntry<String, Object> item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Copies the elements of the dialog state manager to an array starting at a
     * particular index.
     *
     * @param array      The one-dimensional array that is the destination of the
     *                   elements copiedfrom the dialog state manager. The array
     *                   must have zero-based indexing.
     * @param arrayIndex The zero-based index in array at which copying begins.
     */
    public void copyTo(SimpleEntry<String, Object>[] array, int arrayIndex) {
        for (MemoryScope scope : configuration.getMemoryScopes()) {
            array[arrayIndex++] = new SimpleEntry<String, Object>(scope.getName(), scope.getMemory(dialogContext));
        }
    }

    /// <summary>
    /// Removes the first occurrence of a specific Object from the dialog state
    /// manager.
    /// </summary>
    /// <param name="item">The Object to remove from the dialog state
    /// manager.</param>
    /// <returns><c>true</c> if the item was successfully removed from the dialog
    /// state manager;
    /// otherwise, <c>false</c>.</returns>
    /// <remarks>This method is not supported.</remarks>
    /**
     * Removes the first occurrence of a specific Object from the dialog state
     * manager.
     *
     * @param item The Object to remove from the dialog state manager.
     * @return true if the item was successfully removed from the dialog state
     *         manager otherwise false
     */
    public boolean remove(SimpleEntry<String, Object> item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an Iterator that iterates through the collection.
     *
     * @return An Iterator that can be used to iterate through the collection.
     */
    public Iterable<SimpleEntry<String, Object>> getEnumerator() {
        List<SimpleEntry<String, Object>> resultList = new ArrayList<SimpleEntry<String, Object>>();
        for (MemoryScope scope : configuration.getMemoryScopes()) {
            resultList.add(new SimpleEntry<String, Object>(scope.getName(), scope.getMemory(dialogContext)));
        }
        return resultList;
    }

    /**
     * Track when specific paths are changed.
     *
     * @param paths Paths to track.
     * @return Normalized paths to pass to AnyPathChanged.
     */
    public List<String> trackPaths(Iterable<String> paths) {
        List<String> allPaths = new ArrayList<String>();
        for (String path : paths) {
            String tpath = transformPath(path);
            // Track any path that resolves to a constant path
            ArrayList<Object> resolved = ObjectPath.tryResolvePath(this, tpath);
            String[] segments = resolved.toArray(new String[resolved.size()]);
            if (resolved != null) {
                String npath = String.join("_", segments);
                setValue(pathTracker + "." + npath, 0);
                allPaths.add(npath);
            }
        }
        return allPaths;
    }

    /**
     * Check to see if any path has changed since watermark.
     *
     * @param counter Time counter to compare to.
     * @param paths   Paths from Trackpaths to check.
     * @return True if any path has changed since counter.
     */
    public Boolean anyPathChanged(int counter, Iterable<String> paths) {
        Boolean found = false;
        if (paths != null) {
            for (String path : paths) {
                int resultValue = getValue(pathTracker + "." + path, -1, Integer.class);
                if (resultValue != -1 && resultValue > counter) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private static ResultPair<Object> tryGetFirstNestedValue(AtomicReference<String> remainingPath, Object memory) {
        ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
        Object value;
        array = ObjectPath.tryGetPathValue(memory, remainingPath.get(), ArrayNode.class);

        if (array != null && array.size() > 0) {
            JsonNode firstNode = array.get(0);
            if (firstNode instanceof ArrayNode) {
                if (firstNode.size() > 0) {
                    JsonNode secondNode = firstNode.get(0);
                    value = ObjectPath.mapValueTo(secondNode, Object.class);
                    return new ResultPair<Object>(true, value);
                }
                return new ResultPair<Object>(false, null);
            }
            value = ObjectPath.mapValueTo(firstNode, Object.class);
            return new ResultPair<Object>(true, value);
        }
        return new ResultPair<Object>(false, null);
    }

    private String getBadScopeMessage(String path) {
        StringBuilder errorMessage = new StringBuilder(path);
        errorMessage.append(" does not match memory scopes:[");
        List<String> scopeNames = new ArrayList<String>();
        List<MemoryScope> scopes = configuration.getMemoryScopes();
        scopes.forEach((sc) -> {
            scopeNames.add(sc.getName());
        });
        errorMessage.append(String.join(",", scopeNames));
        errorMessage.append("]");
        return errorMessage.toString();
    }

    private Boolean trackChange(String path, Object value) {
        Boolean hasPath = false;
        ArrayList<Object> segments = ObjectPath.tryResolvePath(this, path, false);
        if (segments != null) {
            String root = segments.size() > 1 ? (String) segments.get(1) : new String();

            // Skip _* as first scope, i.e. _adaptive, _tracker, ...
            if (!root.startsWith("_")) {
                List<String> stringSegments = segments.stream().map(Object -> Objects.toString(Object, null))
                        .collect(Collectors.toList());

                // Convert to a simple path with _ between segments
                String pathName = String.join("_", stringSegments);
                String trackedPath = String.format("%s.%s", pathTracker, pathName);
                Integer counter = null;
                /**
                 *
                 */
                ResultPair<Integer> result = tryGetValue(trackedPath, Integer.class);
                if (result.result()) {
                    if (counter == null) {
                        counter = getValue(DialogPath.EVENTCOUNTER, 0, Integer.class);
                    }
                    setValue(trackedPath, counter);
                }
                if (value instanceof Map) {
                    final int count = counter;
                    ((Map<String, Object>) value).forEach((key, val) -> {
                        checkChildren(key, val, trackedPath, count);
                    });
                } else if (value instanceof ObjectNode) {
                    ObjectNode node = (ObjectNode) value;
                    Iterator<String> fields = node.fieldNames();

                    while (fields.hasNext()) {
                        String field = fields.next();
                        checkChildren(field, node.findValue(field), trackedPath, counter);
                    }
                }
            }
            hasPath = true;
        }
        return hasPath;
    }

    private void checkChildren(String property, Object instance, String path, Integer counter) {
        // Add new child segment
        String trackedPath = path + "_" + property.toLowerCase();
        ResultPair<Integer> pathCheck = tryGetValue(trackedPath, Integer.class);
        if (pathCheck.result()) {
            if (counter == null) {
                counter = getValue(DialogPath.EVENTCOUNTER, 0, Integer.class);
            }
            setValue(trackedPath, counter);
        }

        if (instance instanceof Map) {
            final int count = counter;
            ((Map<String, Object>) instance).forEach((key, value) -> {
                checkChildren(key, value, trackedPath, count);
            });
        } else if (instance instanceof ObjectNode) {
            ObjectNode node = (ObjectNode) instance;
            Iterator<String> fields = node.fieldNames();

            while (fields.hasNext()) {
                String field = fields.next();
                checkChildren(field, node.findValue(field), trackedPath, counter);
            }
        }
    }

    @Override
    public final int size() {
        return configuration.getMemoryScopes().size();
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public final boolean containsKey(Object key) {
        return false;
    }

    @Override
    public final boolean containsValue(Object value) {
        return false;
    }

    @Override
    public final Object get(Object key) {
        return tryGetValue(key.toString(), Object.class).value();
    }

    @Override
    public final Object put(String key, Object value) {
        setElement(key, value);
        return value;
    }

    @Override
    public final Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putAll(Map<? extends String, ? extends Object> m) {
    }

    @Override
    public final Set<String> keySet() {
        return configuration.getMemoryScopes().stream().map(scope -> scope.getName()).collect(Collectors.toSet());
    }

    @Override
    public final Collection<Object> values() {
        return configuration.getMemoryScopes().stream().map(scope -> scope.getMemory(dialogContext))
                .collect(Collectors.toSet());
    }

    @Override
    public final Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> resultSet = new HashSet<Entry<String, Object>>();
        configuration.getMemoryScopes().forEach((scope) -> {
            resultSet.add(new AbstractMap.SimpleEntry<String, Object>(scope.getName(), scope.getMemory(dialogContext)));
        });

        return resultSet;
    }

}
