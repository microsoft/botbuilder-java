package com.microsoft.bot.dialogs.memory;

import java.util.Collection;

import com.microsoft.bot.dialogs.DialogContext;

public class DialogStateManager
{
        /// <summary>
        /// Information for tracking when path was last modified.
        /// </summary>
        private final String PathTracker = "dialog._tracker.paths";

        private static final char[] Separators = { ',', '[' };

        private final DialogContext _dialogContext;
        private int _version;

        /// <summary>
        /// Initializes a new instance of the <see cref="DialogStateManager"/> class.
        /// </summary>
        /// <param name="dc">The dialog context for the current turn of the conversation.</param>
        /// <param name="configuration">Configuration for the dialog state manager. Default is <c>null</c>.</param>
        public DialogStateManager(DialogContext dc, DialogStateManagerConfiguration configuration)
        {
            ComponentRegistration.Add(new DialogsComponentRegistration());

            _dialogContext = dc ?? throw new ArgumentNullException(nameof(dc));
            Configuration = configuration ?? dc.Context.TurnState.Get<DialogStateManagerConfiguration>();
            if (Configuration == null)
            {
                Configuration = new DialogStateManagerConfiguration();

                // get all of the component memory scopes
                foreach (var component in ComponentRegistration.Components.OfType<IComponentMemoryScopes>())
                {
                    foreach (var memoryScope in component.GetMemoryScopes())
                    {
                        Configuration.MemoryScopes.Add(memoryScope);
                    }
                }

                // get all of the component path resolvers
                foreach (var component in ComponentRegistration.Components.OfType<IComponentPathResolvers>())
                {
                    foreach (var pathResolver in component.GetPathResolvers())
                    {
                        Configuration.PathResolvers.Add(pathResolver);
                    }
                }
            }

            // cache for any other new dialogStatemanager instances in this turn.
            dc.Context.TurnState.Set(Configuration);
        }

        /// <summary>
        /// Gets or sets the configured path resolvers and memory scopes for the dialog state manager.
        /// </summary>
        /// <value>A <see cref="DialogStateManagerConfiguration"/> with the configuration.</value>
        public DialogStateManagerConfiguration Configuration;

        /// <summary>
        /// Gets an <see cref="ICollection{T}"/> containing the keys of the memory scopes.
        /// </summary>
        /// <value>Keys of the memory scopes.</value>
        public Collection<String> Keys => Configuration.MemoryScopes.Select(ms => ms.Name).ToList();

        /// <summary>
        /// Gets an <see cref="ICollection{T}"/> containing the values of the memory scopes.
        /// </summary>
        /// <value>Values of the memory scopes.</value>
        public Collection<Object> Values => Configuration.MemoryScopes.Select(ms => ms.GetMemory(_dialogContext)).ToList();

        /// <summary>
        /// Gets the number of memory scopes in the dialog state manager.
        /// </summary>
        /// <value>Number of memory scopes in the configuration.</value>
        public int Count => Configuration.MemoryScopes.Count;

        /// <summary>
        /// Gets a value indicating whether the dialog state manager is read-only.
        /// </summary>
        /// <value><c>true</c>.</value>
        public Boolean IsReadOnly => true;

        /// <summary>
        /// Gets or sets the elements with the specified key.
        /// </summary>
        /// <param name="key">Key to get or set the element.</param>
        /// <returns>The element with the specified key.</returns>
        public Object this[String key]
        {
            get => GetValue<Object>(key, () => null);
            set
            {
                if (key.IndexOfAny(Separators) == -1)
                {
                    // Root is handled by SetMemory rather than SetValue
                    var scope = GetMemoryScope(key) ?? throw new ArgumentOutOfRangeException(nameof(key), GetBadScopeMessage(key));
                    scope.SetMemory(_dialogContext, JToken.FromObject(value));
                }
                else
                {
                    SetValue(key, value);
                }
            }
        }

        /// <summary>
        /// Get MemoryScope by name.
        /// </summary>
        /// <param name="name">Name of scope.</param>
        /// <returns>A memory scope.</returns>
        public MemoryScope GetMemoryScope(String name)
        {
            if (name == null)
            {
                throw new ArgumentNullException(nameof(name));
            }

            return Configuration.MemoryScopes.FirstOrDefault(ms => String.Compare(ms.Name, name, StringComparison.OrdinalIgnoreCase) == 0);
        }

        /// <summary>
        /// Version help caller to identify the updates and decide cache or not.
        /// </summary>
        /// <returns>Current version.</returns>
        public String Version()
        {
            return _version.ToString(CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// ResolveMemoryScope will find the MemoryScope for and return the remaining path.
        /// </summary>
        /// <param name="path">Incoming path to resolve to scope and remaining path.</param>
        /// <param name="remainingPath">Remaining subpath in scope.</param>
        /// <returns>The memory scope.</returns>
        public MemoryScope ResolveMemoryScope(String path, out String remainingPath)
        {
            var scope = path;
            var sepIndex = -1;
            var dot = path.IndexOf(".", StringComparison.OrdinalIgnoreCase);
            var openSquareBracket = path.IndexOf("[", StringComparison.OrdinalIgnoreCase);

            if (dot > 0 && openSquareBracket > 0)
            {
                sepIndex = Math.Min(dot, openSquareBracket);
            }
            else if (dot > 0)
            {
                sepIndex = dot;
            }
            else if (openSquareBracket > 0)
            {
                sepIndex = openSquareBracket;
            }

            if (sepIndex > 0)
            {
                scope = path.Substring(0, sepIndex);
                var memoryScope = GetMemoryScope(scope);
                if (memoryScope != null)
                {
                    remainingPath = path.Substring(sepIndex + 1);
                    return memoryScope;
                }
            }

            remainingPath = String.Empty;
            return GetMemoryScope(scope) ?? throw new ArgumentOutOfRangeException(GetBadScopeMessage(path));
        }

        /// <summary>
        /// Transform the path using the registered PathTransformers.
        /// </summary>
        /// <param name="path">Path to transform.</param>
        /// <returns>The transformed path.</returns>
        public String TransformPath(String path)
        {
            foreach (var pathResolver in Configuration.PathResolvers)
            {
                path = pathResolver.TransformPath(path);
            }

            return path;
        }

        /// <summary>
        /// Get the value from memory using path expression (NOTE: This always returns clone of value).
        /// </summary>
        /// <remarks>This always returns a CLONE of the memory, any modifications to the result of this will not be affect memory.</remarks>
        /// <typeparam name="T">the value type to return.</typeparam>
        /// <param name="path">path expression to use.</param>
        /// <param name="value">Value out parameter.</param>
        /// <returns>True if found, false if not.</returns>
        public Boolean TryGetValue<T>(String path, out T value)
        {
            value = default;
            path = TransformPath(path ?? throw new ArgumentNullException(nameof(path)));

            MemoryScope memoryScope = null;
            String remainingPath;

            try
            {
                memoryScope = ResolveMemoryScope(path, out remainingPath);
            }
#pragma warning disable CA1031 // Do not catch general exception types (Unable to get the value for some reason, catch, log and return false, ignoring exception)
            catch (Exception err)
#pragma warning restore CA1031 // Do not catch general exception types
            {
                Trace.TraceError(err.Message);
                return false;
            }

            if (memoryScope == null)
            {
                return false;
            }

            if (String.IsNullOrEmpty(remainingPath))
            {
                var memory = memoryScope.GetMemory(_dialogContext);
                if (memory == null)
                {
                    return false;
                }

                value = ObjectPath.MapValueTo<T>(memory);
                return true;
            }

            // TODO: HACK to support .First() retrieval on turn.recognized.entities.foo, replace with Expressions once expression ship
            final String first = ".FIRST()";
            var iFirst = path.ToUpperInvariant().LastIndexOf(first, StringComparison.Ordinal);
            if (iFirst >= 0)
            {
                Object entity = null;
                remainingPath = path.Substring(iFirst + first.Length);
                path = path.Substring(0, iFirst);
                if (TryGetFirstNestedValue(ref entity, ref path, this))
                {
                    if (String.IsNullOrEmpty(remainingPath))
                    {
                        value = ObjectPath.MapValueTo<T>(entity);
                        return true;
                    }

                    return ObjectPath.TryGetPathValue(entity, remainingPath, out value);
                }

                return false;
            }

            return ObjectPath.TryGetPathValue(this, path, out value);
        }

        /// <summary>
        /// Get the value from memory using path expression (NOTE: This always returns clone of value).
        /// </summary>
        /// <remarks>This always returns a CLONE of the memory, any modifications to the result of this will not be affect memory.</remarks>
        /// <typeparam name="T">The value type to return.</typeparam>
        /// <param name="pathExpression">Path expression to use.</param>
        /// <param name="defaultValue">Function to give default value if there is none (OPTIONAL).</param>
        /// <returns>Result or null if the path is not valid.</returns>
        public T GetValue<T>(String pathExpression, Func<T> defaultValue = null)
        {
            if (TryGetValue<T>(pathExpression ?? throw new ArgumentNullException(nameof(pathExpression)), out var value))
            {
                return value;
            }

            return defaultValue != null ? defaultValue() : default;
        }

        /// <summary>
        /// Get a int value from memory using a path expression.
        /// </summary>
        /// <param name="pathExpression">Path expression.</param>
        /// <param name="defaultValue">Default value if the value doesn't exist.</param>
        /// <returns>Value or null if path is not valid.</returns>
        public int GetIntValue(String pathExpression, int defaultValue = 0)
        {
            if (TryGetValue<int>(pathExpression ?? throw new ArgumentNullException(nameof(pathExpression)), out var value))
            {
                return value;
            }

            return defaultValue;
        }

        /// <summary>
        /// Get a Boolean value from memory using a path expression.
        /// </summary>
        /// <param name="pathExpression">The path expression.</param>
        /// <param name="defaultValue">Default value if the value doesn't exist.</param>
        /// <returns>Bool or null if path is not valid.</returns>
        public Boolean GetBoolValue(String pathExpression, Boolean defaultValue = false)
        {
            if (TryGetValue<Boolean>(pathExpression ?? throw new ArgumentNullException(nameof(pathExpression)), out var value))
            {
                return value;
            }

            return defaultValue;
        }

        /// <summary>
        /// Get a String value from memory using a path expression.
        /// </summary>
        /// <param name="pathExpression">The path expression.</param>
        /// <param name="defaultValue">Default value if the value doesn't exist.</param>
        /// <returns>String or null if path is not valid.</returns>
        public String GetStringValue(String pathExpression, String defaultValue = default)
        {
            return GetValue(pathExpression, () => defaultValue);
        }

        /// <summary>
        /// Set memory to value.
        /// </summary>
        /// <param name="path">Path to memory.</param>
        /// <param name="value">Object to set.</param>
        public void SetValue(String path, Object value)
        {
            if (value is Task)
            {
                throw new Exception($"{path} = You can't pass an unresolved CompletableFuture<Void> to SetValue");
            }

            if (value != null)
            {
                value = JToken.FromObject(value);
            }

            path = TransformPath(path ?? throw new ArgumentNullException(nameof(path)));
            if (TrackChange(path, value))
            {
                ObjectPath.SetPathValue(this, path, value);
            }

            // Every set will increase version
            _version++;
        }

        /// <summary>
        /// Remove property from memory.
        /// </summary>
        /// <param name="path">Path to remove the leaf property.</param>
        public void RemoveValue(String path)
        {
            path = TransformPath(path ?? throw new ArgumentNullException(nameof(path)));
            if (TrackChange(path, null))
            {
                ObjectPath.RemovePathValue(this, path);
            }
        }

        /// <summary>
        /// Gets all memoryscopes suitable for logging.
        /// </summary>
        /// <returns>Object which represents all memory scopes.</returns>
        public JObject GetMemorySnapshot()
        {
            var result = new JObject();

            foreach (var scope in Configuration.MemoryScopes.Where(ms => ms.IncludeInSnapshot))
            {
                var memory = scope.GetMemory(_dialogContext);
                if (memory != null)
                {
                    result[scope.Name] = JToken.FromObject(memory);
                }
            }

            return result;
        }

        /// <summary>
        /// Load all of the scopes.
        /// </summary>
        /// <param name="cancellationToken">cancellationToken.</param>
        /// <returns>Task.</returns>
        public CompletableFuture<Void> LoadAllScopesAsync(CancellationToken cancellationToken = default)
        {
            foreach (var scope in Configuration.MemoryScopes)
            {
                await scope.LoadAsync(_dialogContext, cancellationToken: cancellationToken).ConfigureAwait(false);
            }
        }

        /// <summary>
        /// Save all changes for all scopes.
        /// </summary>
        /// <param name="cancellationToken">cancellationToken.</param>
        /// <returns>Task.</returns>
        public CompletableFuture<Void> SaveAllChangesAsync(CancellationToken cancellationToken = default)
        {
            foreach (var scope in Configuration.MemoryScopes)
            {
                await scope.SaveChangesAsync(_dialogContext, cancellationToken: cancellationToken).ConfigureAwait(false);
            }
        }

        /// <summary>
        /// Delete the memory for a scope.
        /// </summary>
        /// <param name="name">name of the scope.</param>
        /// <param name="cancellationToken">cancellationToken.</param>
        /// <returns>Task.</returns>
        public CompletableFuture<Void> DeleteScopesMemoryAsync(String name, CancellationToken cancellationToken = default)
        {
            name = name.ToUpperInvariant();
            var scope = Configuration.MemoryScopes.SingleOrDefault(s => s.Name.ToUpperInvariant() == name);
            if (scope != null)
            {
                await scope.DeleteAsync(_dialogContext, cancellationToken).ConfigureAwait(false);
            }
        }

        /// <summary>
        /// Adds an element to the dialog state manager.
        /// </summary>
        /// <param name="key">Key of the element to add.</param>
        /// <param name="value">Value of the element to add.</param>
        public void Add(String key, Object value)
        {
            throw new NotSupportedException();
        }

        /// <summary>
        /// Determines whether the dialog state manager contains an element with the specified key.
        /// </summary>
        /// <param name="key">The key to locate in the dialog state manager.</param>
        /// <returns><c>true</c> if the dialog state manager contains an element with
        /// the key; otherwise, <c>false</c>.</returns>
        public Boolean ContainsKey(String key)
        {
            return Configuration.MemoryScopes.Any(ms => ms.Name.ToUpperInvariant() == key.ToUpperInvariant());
        }

        /// <summary>
        /// Removes the element with the specified key from the dialog state manager.
        /// </summary>
        /// <param name="key">The key of the element to remove.</param>
        /// <returns><c>true</c> if the element is succesfully removed; otherwise, false.</returns>
        /// <remarks>This method is not supported.</remarks>
        public Boolean Remove(String key)
        {
            throw new NotSupportedException();
        }

        /// <summary>
        /// Gets the value associated with the specified key.
        /// </summary>
        /// <param name="key">The key whose value to get.</param>
        /// <param name="value">When this method returns, the value associated with the specified key, if the
        /// key is found; otherwise, the default value for the type of the value parameter.
        /// This parameter is passed uninitialized.</param>
        /// <returns><c>true</c> if the dialog state manager contains an element with the specified key;
        /// otherwise, <c>false</c>.</returns>
        public Boolean TryGetValue(String key, out Object value)
        {
            return TryGetValue<Object>(key, out value);
        }

        /// <summary>
        /// Adds an item to the dialog state manager.
        /// </summary>
        /// <param name="item">The <see cref="KeyValuePair{TKey, TValue}"/> with the key and Object of
        /// the item to add.</param>
        /// <remarks>This method is not supported.</remarks>
        public void Add(KeyValuePair<String, Object> item)
        {
            throw new NotSupportedException();
        }

        /// <summary>
        /// Removes all items from the dialog state manager.
        /// </summary>
        /// <remarks>This method is not supported.</remarks>
        public void Clear()
        {
            throw new NotSupportedException();
        }

        /// <summary>
        /// Determines whether the dialog state manager contains a specific value.
        /// </summary>
        /// <param name="item">The <see cref="KeyValuePair{TKey, TValue}"/> of the item to locate.</param>
        /// <returns><c>true</c> if item is found in the dialog state manager; otherwise,
        /// <c>false</c>.</returns>
        /// <remarks>This method is not supported.</remarks>
        public Boolean Contains(KeyValuePair<String, Object> item)
        {
            throw new NotSupportedException();
        }

        /// <summary>
        /// Copies the elements of the dialog state manager to an array starting at a particular index.
        /// </summary>
        /// <param name="array">The one-dimensional array that is the destination of the elements copied
        /// from the dialog state manager. The array must have zero-based indexing.</param>
        /// <param name="arrayIndex">The zero-based index in array at which copying begins.</param>
        public void CopyTo(KeyValuePair<String, Object>[] array, int arrayIndex)
        {
            foreach (var ms in Configuration.MemoryScopes)
            {
                array[arrayIndex++] = new KeyValuePair<String, Object>(ms.Name, ms.GetMemory(_dialogContext));
            }
        }

        /// <summary>
        /// Removes the first occurrence of a specific Object from the dialog state manager.
        /// </summary>
        /// <param name="item">The Object to remove from the dialog state manager.</param>
        /// <returns><c>true</c> if the item was successfully removed from the dialog state manager;
        /// otherwise, <c>false</c>.</returns>
        /// <remarks>This method is not supported.</remarks>
        public Boolean Remove(KeyValuePair<String, Object> item)
        {
            throw new NotSupportedException();
        }

        /// <summary>
        /// Returns an enumerator that iterates through the collection.
        /// </summary>
        /// <returns>An enumerator that can be used to iterate through the collection.</returns>
        public IEnumerator<KeyValuePair<String, Object>> GetEnumerator()
        {
            foreach (var ms in Configuration.MemoryScopes)
            {
                yield return new KeyValuePair<String, Object>(ms.Name, ms.GetMemory(_dialogContext));
            }
        }

        /// <summary>
        /// Track when specific paths are changed.
        /// </summary>
        /// <param name="paths">Paths to track.</param>
        /// <returns>Normalized paths to pass to <see cref="AnyPathChanged"/>.</returns>
        public List<String> TrackPaths(IEnumerable<String> paths)
        {
            var allPaths = new List<String>();
            foreach (var path in paths)
            {
                var tpath = TransformPath(path);

                // Track any path that resolves to a constant path
                if (ObjectPath.TryResolvePath(this, tpath, out var segments))
                {
                    var npath = String.Join("_", segments);
                    SetValue(PathTracker + "." + npath, 0);
                    allPaths.Add(npath);
                }
            }

            return allPaths;
        }

        /// <summary>
        /// Check to see if any path has changed since watermark.
        /// </summary>
        /// <param name="counter">Time counter to compare to.</param>
        /// <param name="paths">Paths from <see cref="TrackPaths"/> to check.</param>
        /// <returns>True if any path has changed since counter.</returns>
        public Boolean AnyPathChanged(uint counter, IEnumerable<String> paths)
        {
            var found = false;
            if (paths != null)
            {
                foreach (var path in paths)
                {
                    if (GetValue<uint>(PathTracker + "." + path) > counter)
                    {
                        found = true;
                        break;
                    }
                }
            }

            return found;
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            foreach (var ms in Configuration.MemoryScopes)
            {
                yield return new KeyValuePair<String, Object>(ms.Name, ms.GetMemory(_dialogContext));
            }
        }

        private static Boolean TryGetFirstNestedValue<T>(ref T value, ref String remainingPath, Object memory)
        {
            if (ObjectPath.TryGetPathValue<JArray>(memory, remainingPath, out var array))
            {
                if (array != null && array.Count > 0)
                {
                    if (array[0] is JArray first)
                    {
                        if (first.Count > 0)
                        {
                            var second = first[0];
                            value = ObjectPath.MapValueTo<T>(second);
                            return true;
                        }

                        return false;
                    }

                    value = ObjectPath.MapValueTo<T>(array[0]);
                    return true;
                }
            }

            return false;
        }

        private String GetBadScopeMessage(String path)
        {
            return $"'{path}' does not match memory scopes:[{String.Join(",", Configuration.MemoryScopes.Select(ms => ms.Name))}]";
        }

        private Boolean TrackChange(String path, Object value)
        {
            var hasPath = false;
            if (ObjectPath.TryResolvePath(this, path, out var segments))
            {
                var root = segments.Count > 1 ? segments[1] as String : String.Empty;

                // Skip _* as first scope, i.e. _adaptive, _tracker, ...
                if (!root.StartsWith("_", StringComparison.Ordinal))
                {
                    // Convert to a simple path with _ between segments
                    var pathName = String.Join("_", segments);
                    var trackedPath = $"{PathTracker}.{pathName}";
                    uint? counter = null;

                    void Update()
                    {
                        if (TryGetValue<uint>(trackedPath, out var lastChanged))
                        {
                            if (!counter.HasValue)
                            {
                                counter = GetValue<uint>(DialogPath.EventCounter);
                            }

                            SetValue(trackedPath, counter.Value);
                        }
                    }

                    Update();
                    if (value is Object obj)
                    {
                        // For an Object we need to see if any children path are being tracked
                        void CheckChildren(String property, Object instance)
                        {
                            // Add new child segment
                            trackedPath += "_" + property.ToLowerInvariant();
                            Update();
                            if (instance is Object child)
                            {
                                ObjectPath.ForEachProperty(child, CheckChildren);
                            }

                            // Remove added child segment
                            trackedPath = trackedPath.Substring(0, trackedPath.LastIndexOf('_'));
                        }

                        ObjectPath.ForEachProperty(obj, CheckChildren);
                    }
                }

                hasPath = true;
            }

            return hasPath;
        }
    }

