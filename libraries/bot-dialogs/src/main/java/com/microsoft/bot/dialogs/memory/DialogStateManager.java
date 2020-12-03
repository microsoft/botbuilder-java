package com.microsoft.bot.dialogs.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.security.auth.login.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.builder.ComponentRegistration;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogsComponentRegistration;
import com.microsoft.bot.dialogs.ObjectPath;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;
import com.microsoft.bot.schema.ResultPair;

import org.apache.commons.lang3.StringUtils;

/**
 * The DialogStateManager manages memory scopes and pathresolvers MemoryScopes
 * are named root level Objects, which can exist either in the dialogcontext or
 * off of turn state PathResolvers allow for shortcut behavior for mapping
 * things like $foo -> dialog.foo.
 */
public class DialogStateManager {

    /**
     * Information for tracking when path was last modified.
     */
    private final String PathTracker = "dialog._tracker.paths";

    private static final char[] Separators = { ',', '[' };

    private final DialogContext _dialogContext;
    private int _version;

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
            _dialogContext = dc;
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

            Iterable<ComponentRegistration> components = ComponentRegistration.getComponents();

            components.forEach((component) -> {
                if (component instanceof ComponentMemoryScopes) {
                    configuration.getMemoryScopes().forEach((scope) -> {
                        this.configuration.getMemoryScopes().add(scope);
                    });
                }
                if (component instanceof ComponentPathResolvers) {
                    configuration.getPathResolvers().forEach((pathResolver) -> {
                        this.configuration.getPathResolvers().add(pathResolver);
                    });
                }
            });
        }

        // cache for any other new dialogStatemanager instances in this turn.
        dc.getContext().getTurnState().add(this.configuration);
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

    private Collection<String> keys;

    /**
     *
     * @return Gets an Collection containing the keys of the memory scopes.
     */
    public Collection<String> getKeys() {
        return configuration.getMemoryScopes().stream().map(scope -> scope.getName()).collect(Collectors.toList());
    }

    private Collection<Object> values;

    /**
     * Gets an Collection containing the values of the memory scopes.
     *
     * @return Values of the memory scopes.
     */
    public Collection<Object> getValues() {
        return configuration.getMemoryScopes().stream().map(scope -> scope.getMemory(_dialogContext))
                .collect(Collectors.toList());
    }

    /**
     * Gets the number of memory scopes in the dialog state manager.
     *
     * @return Number of memory scopes in the configuration.
     */
    public int getCount() {
        return configuration.getMemoryScopes().size();
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
        return GetValue(key);
    }

    /**
     * Sets the elements with the specified key.
     *
     * @param key     Key to get or set the element.
     * @param element The element to store with the provided key.
     */
    public void setElement(String key, Object element) {
        if (key.indexOf(Separators[0]) == -1 && key.indexOf(Separators[1]) == -1) {
            MemoryScope scope = getMemoryScope(key);
            if (scope != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    scope.setMemory(_dialogContext, mapper.writeValueAsString(element));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                throw new IllegalArgumentException();
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
        return configuration.getMemoryScopes().stream().filter((scope) -> scope.getName().equalsIgnoreCase(name))
                .findFirst().get();
    }

    /**
     * Version help caller to identify the updates and decide cache or not.
     *
     * @return Current version
     */
    public String version() {
        return Integer.toString(_version);
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
                remainingPath = new StringBuilder(path.substring(sepIndex + 1));
                return memoryScope;
            }
        }

        remainingPath = new StringBuilder();
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
     * @param path    >path expression to use.
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
            Object memory = memoryScope.getMemory(_dialogContext);
            if (memory == null) {
                return new ResultPair<>(false, instance);
            }

            instance = (TypeT) ObjectPath.mapValueTo(memory, clsType);

            return new ResultPair<>(true, instance);
        }

        // TODO: HACK to support .First() retrieval on turn.recognized.entities.foo,
        // replace with Expressions
        // once expression ship
        final String first = ".FIRST()";
        int iFirst = path.toUpperCase(Locale.US).lastIndexOf(first);
        if (iFirst >= 0) {
            Object entity = null;
            remainingPath = new StringBuilder(path.substring(iFirst + first.length()));
            path = path.substring(0, iFirst);
            if (tryGetFirstNestedValue(entity, new AtomicReference<String>(path), this)) {
                if (StringUtils.isEmpty(remainingPath.toString()) || remainingPath.toString() == null) {
                    instance = (TypeT) ObjectPath.mapValueTo(entity, clsType);
                    return new ResultPair<>(true, instance);
                }
                instance = (TypeT) ObjectPath.tryGetPathValue(entity, path, clsType);

                return new ResultPair<>(true, instance);
            }

            return new ResultPair<>(false, instance);
        }

        instance = (TypeT) ObjectPath.tryGetPathValue(this, path, clsType);

        return new ResultPair<>(true, instance);
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
     * @param pathExpression The path expression.
     * @param defaultValue Default value if the value doesn't exist.
     * @return String or default value if path is not valid.
     */
    public String getStringValue(String pathExpression, String defaultValue) {
            return getValue(pathExpression, defaultValue, String.class);
    }


    /**
     * Set memory to value.
     * @param path Path to memory.
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
                ObjectMapper mapper = new ObjectMapper();
                try {
                     String jsonValue = mapper.writeValueAsString(value);
                     value = jsonValue;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            path = transformPath(path);
            if (trackChange(path, value)) {
                ObjectPath.setPathValue(this, path, value);
            }

            // Every set will increase version
            _version++;
        }

    private static Boolean tryGetFirstNestedValue(Object value,
                                                  AtomicReference<String> remainingPath,
                                                  Object memory) {
        ArrayNode array = new ArrayNode(JsonNodeFactory.instance);

        array = ObjectPath.tryGetPathValue(memory, remainingPath.get(), ArrayNode.class);

        if (array != null && array.size() > 0) {
            JsonNode firstNode = array.get(0);
            if (firstNode instanceof ArrayNode) {
                if (firstNode.size() > 0) {
                    JsonNode secondNode = firstNode.get(0);
                    value = ObjectPath.mapValueTo(secondNode, Object.class);
                    return true;
                }
                return false;
            }
            value = ObjectPath.mapValueTo(firstNode, Object.class);
            return true;
        }
        return false;
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
            String root = segments.size() > 1 ? (String) segments.get(1)  : new String();

            // Skip _* as first scope, i.e. _adaptive, _tracker, ...
            if (!root.startsWith("_")) {
                List<String> stringSegments = segments.stream()
                                              .map(object -> Objects.toString(object, null))
                                              .collect(Collectors.toList());

                // Convert to a simple path with _ between segments
                String pathName = String.join("_", stringSegments);
                String trackedPath = String.format("%s.%s", PathTracker, pathName);
                Integer counter = null;
                counter = getValue(DialogPath.EVENTCOUNTER, 0, Integer.class);
                /**
                 *
                 */
                class UpdateClass {
                    /**
                     * Update method
                     */
                    public void update() {
                        ResultPair<Integer> result = tryGetValue(trackedPath, Integer.class);
                        if (result.result()) {
                            if (counter == null) {
                                counter = getValue(DialogPath.EVENTCOUNTER, 0, Integer.class);
                            }
                            setValue(trackedPath, counter);
                        }
                    }
                }

                new UpdateClass().update();

                if (value instanceof Object) {
                    /**
                     * For an object we need to see if any children path are being tracked
                     */
                    class ChildCheck {
                        /**
                         *  Check the child properties
                         * @param property Property to check.
                         * @param instance Instance of returned value.
                         */
                        void checkChildren(String property, Object instance) {
                            // Add new child segment
                            trackedPath += "_" + property.toLowerCase();
                            new UpdateClass().update();
                            if (instance instanceof Object) {
                                ObjectPath.forEachProperty(instance, ChildCheck::checkChildren);
                            }

                            // Remove added child segment
                            trackedPath = trackedPath.substring(0, trackedPath.lastIndexOf('_'));
                        }
                    }
                    // For an Object we need to see if any children path are being tracked

                    ObjectPath.forEachProperty(value, ChildCheck::checkChildren);
                }
            }

            hasPath = true;
        }

        return hasPath;
    }

}
