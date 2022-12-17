/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Pool<T> {

	private final Deque<T> cache = new ArrayDeque<>();
	private final Supplier<T> factory;
	private final Consumer<T> onReturn;

	public Pool(Supplier<T> factory) {
		this(factory, null);
	}

	public Pool(Supplier<T> factory, Consumer<T> onReturn) {
		this.factory = factory;
		this.onReturn = onReturn;
	}

	public T doBorrow() {
		synchronized (cache) {
			if (cache.isEmpty()) {
				return factory.get();
			} else {
				return cache.removeLast();
			}
		}
	}

	public void doReturn(T obj) {
		if (onReturn != null) {
			onReturn.accept(obj);
		}
		synchronized (cache) {
			cache.add(obj);
		}
	}

}
