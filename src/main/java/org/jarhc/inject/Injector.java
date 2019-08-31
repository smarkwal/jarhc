/*
 * Copyright 2018 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jarhc.inject;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A small dependency injector.
 */
public class Injector {

	/**
	 * Instance bindings for interfaces and classes.
	 */
	private final Map<Class, Object> bindings = new HashMap<>();

	public <T> void addBinding(Class<T> cls, T object) {
		bindings.put(cls, object);
	}

	public <T> T createInstance(Class<T> cls) throws InjectorException {
		Constructor<T> constructor = findConstructor(cls);
		Object[] parameters = prepareParameters(constructor);
		try {
			return constructor.newInstance(parameters);
		} catch (Exception e) {
			throw new InjectorException("Error creating instance of class: " + cls.getName(), e);
		}
	}

	private <T> Constructor<T> findConstructor(Class<T> cls) throws InjectorException {
		List<Constructor<T>> constructors = findConstructors(cls);
		int size = constructors.size();
		if (size == 0) {
			throw new InjectorException("No supported constructor found in class: " + cls.getName());
		} else if (size == 1) {
			return constructors.get(0);
		} else {
			throw new InjectorException("Multiple supported constructor found in class: " + cls.getName());
		}
	}

	@SuppressWarnings("unchecked")
	private <T> List<Constructor<T>> findConstructors(Class<T> cls) {
		Constructor<T>[] constructors = (Constructor<T>[]) cls.getConstructors();
		return Arrays.stream(constructors).filter(this::hasMappings).collect(Collectors.toList());
	}

	private boolean hasMappings(Constructor constructor) {
		Class[] parameterTypes = constructor.getParameterTypes();
		return Arrays.stream(parameterTypes).allMatch(bindings::containsKey);
	}

	private Object[] prepareParameters(Constructor constructor) {
		Class[] parameterTypes = constructor.getParameterTypes();
		return Arrays.stream(parameterTypes).map(bindings::get).toArray();
	}

}
