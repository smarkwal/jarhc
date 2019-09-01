/*
 * Copyright 2019 Stephan Markwalder
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

package org.jarhc.test;

import java.security.Permission;

/**
 * Security manager intercepting {@link System#exit(int)} calls by throwing a
 * {@link SystemExitException} instead.
 */
public class SystemExitManager extends SecurityManager {

	private final SecurityManager originalSecurityManager;

	public SystemExitManager(SecurityManager originalSecurityManager) {
		this.originalSecurityManager = originalSecurityManager;
	}

	@Override
	public void checkExit(int status) {
		throw new SystemExitException(status);
	}

	// delegate all other checks to original security manager

	@Override
	public void checkPermission(Permission perm) {
		if (originalSecurityManager != null) {
			originalSecurityManager.checkPermission(perm);
		}
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		if (originalSecurityManager != null) {
			originalSecurityManager.checkPermission(perm, context);
		}
	}

}
