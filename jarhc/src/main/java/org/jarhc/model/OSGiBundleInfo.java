/*
 * Copyright 2024 Stephan Markwalder
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

package org.jarhc.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OSGiBundleInfo {

	// list of OSGI Bundle headers:
	// https://docs.osgi.org/reference/bundle-headers.html
	// https://docs.osgi.org/specification/osgi.core/8.0.0/framework.module.html#i2654895

	public static final Set<String> BUNDLE_HEADERS = Set.of(
			// Name
			"Bundle-Name",
			"Bundle-SymbolicName",

			// Version
			"Bundle-Version",

			// Description
			"Bundle-Description",
			"Bundle-Vendor",
			"Bundle-License",
			"Bundle-DocURL",

			// Import Package
			"Import-Package",
			"DynamicImport-Package",

			// Export Package
			"Export-Package",

			// Capabilities
			"Require-Capability",
			"Provide-Capability",

			// Others
			// TODO: re-distribute these attributes to more specific columns
			"Bundle-ActivationPolicy",
			"Bundle-Activator",
			"Bundle-Category",
			"Bundle-Classpath",
			"Bundle-ContactAddress",
			"Bundle-Copyright",
			"Bundle-Icon",
			"Bundle-Localization",
			"Bundle-ManifestVersion",
			"Bundle-NativeCode",
			"Bundle-RequiredExecutionEnvironment",
			"Bundle-UpdateLocation",
			"Export-Service",
			"Fragment-Host",
			"Ignore-Package",
			"Import-Bundle",
			"Import-Library",
			"Import-Service",
			"Include-Resource",
			"Module-Scope",
			"Module-Type",
			"Private-Package",
			"Require-Bundle",
			"Web-ContextPath",
			"Web-DispatcherServletUrlPatterns",
			"Web-FilterMappings"
	);

	public static boolean isBundleHeader(String attributeName) {
		return BUNDLE_HEADERS.contains(attributeName);
	}

	private String bundleName;
	private String bundleSymbolicName;

	private String bundleVersion;

	private String bundleDescription;
	private String bundleVendor;
	private String bundleLicense;
	private String bundleDocURL;

	private List<String> importPackage;
	private List<String> dynamicImportPackage;

	private List<String> exportPackage;

	private List<String> requireCapability;
	private List<String> provideCapability;

	private String bundleActivationPolicy;
	private String bundleActivator;
	private String bundleCategory;
	private String bundleClasspath;
	private String bundleContactAddress;
	private String bundleCopyright;
	private String bundleIcon;
	private String bundleLocalization;
	private String bundleManifestVersion;
	private String bundleNativeCode;
	private String bundleRequiredExecutionEnvironment;
	private String bundleUpdateLocation;
	private String exportService;
	private String fragmentHost;
	private String ignorePackage;
	private String importBundle;
	private String importLibrary;
	private String importService;
	private List<String> includeResource;
	private String moduleScope;
	private String moduleType;
	private List<String> privatePackage;
	private String requireBundle;
	private String webContextPath;
	private String webDispatcherServletUrlPatterns;
	private String webFilterMappings;

	public OSGiBundleInfo(Map<String, String> manifestAttributes) {
		if (manifestAttributes == null) {
			return;
		}

		this.bundleName = manifestAttributes.get("Bundle-Name");
		this.bundleSymbolicName = manifestAttributes.get("Bundle-SymbolicName");
		this.bundleVersion = manifestAttributes.get("Bundle-Version");
		this.bundleDescription = manifestAttributes.get("Bundle-Description");
		this.bundleVendor = manifestAttributes.get("Bundle-Vendor");
		this.bundleLicense = manifestAttributes.get("Bundle-License");
		this.bundleDocURL = manifestAttributes.get("Bundle-DocURL");
		this.importPackage = splitList(manifestAttributes.get("Import-Package"));
		this.dynamicImportPackage = splitList(manifestAttributes.get("DynamicImport-Package"));
		this.exportPackage = splitList(manifestAttributes.get("Export-Package"));
		this.requireCapability = splitList(manifestAttributes.get("Require-Capability"));
		this.provideCapability = splitList(manifestAttributes.get("Provide-Capability"));
		this.bundleActivationPolicy = manifestAttributes.get("Bundle-ActivationPolicy");
		this.bundleActivator = manifestAttributes.get("Bundle-Activator");
		this.bundleCategory = manifestAttributes.get("Bundle-Category");
		this.bundleClasspath = manifestAttributes.get("Bundle-Classpath");
		this.bundleContactAddress = manifestAttributes.get("Bundle-ContactAddress");
		this.bundleCopyright = manifestAttributes.get("Bundle-Copyright");
		this.bundleIcon = manifestAttributes.get("Bundle-Icon");
		this.bundleLocalization = manifestAttributes.get("Bundle-Localization");
		this.bundleManifestVersion = manifestAttributes.get("Bundle-ManifestVersion");
		this.bundleNativeCode = manifestAttributes.get("Bundle-NativeCode");
		this.bundleRequiredExecutionEnvironment = manifestAttributes.get("Bundle-RequiredExecutionEnvironment");
		this.bundleUpdateLocation = manifestAttributes.get("Bundle-UpdateLocation");
		this.exportService = manifestAttributes.get("Export-Service");
		this.fragmentHost = manifestAttributes.get("Fragment-Host");
		this.ignorePackage = manifestAttributes.get("Ignore-Package");
		this.importBundle = manifestAttributes.get("Import-Bundle");
		this.importLibrary = manifestAttributes.get("Import-Library");
		this.importService = manifestAttributes.get("Import-Service");
		this.includeResource = splitList(manifestAttributes.get("Include-Resource"));
		this.moduleScope = manifestAttributes.get("Module-Scope");
		this.moduleType = manifestAttributes.get("Module-Type");
		this.privatePackage = splitList(manifestAttributes.get("Private-Package"));
		this.requireBundle = manifestAttributes.get("Require-Bundle");
		this.webContextPath = manifestAttributes.get("Web-ContextPath");
		this.webDispatcherServletUrlPatterns = manifestAttributes.get("Web-DispatcherServletUrlPatterns");
		this.webFilterMappings = manifestAttributes.get("Web-FilterMappings");
	}

	public String getBundleName() {
		return bundleName;
	}

	public String getBundleSymbolicName() {
		return bundleSymbolicName;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}

	public String getBundleDescription() {
		return bundleDescription;
	}

	public String getBundleVendor() {
		return bundleVendor;
	}

	public String getBundleLicense() {
		return bundleLicense;
	}

	public String getBundleDocURL() {
		return bundleDocURL;
	}

	public List<String> getImportPackage() {
		return importPackage;
	}

	public List<String> getDynamicImportPackage() {
		return dynamicImportPackage;
	}

	public List<String> getExportPackage() {
		return exportPackage;
	}

	public List<String> getRequireCapability() {
		return requireCapability;
	}

	public List<String> getProvideCapability() {
		return provideCapability;
	}

	public String getBundleActivationPolicy() {
		return bundleActivationPolicy;
	}

	public String getBundleActivator() {
		return bundleActivator;
	}

	public String getBundleCategory() {
		return bundleCategory;
	}

	public String getBundleClasspath() {
		return bundleClasspath;
	}

	public String getBundleContactAddress() {
		return bundleContactAddress;
	}

	public String getBundleCopyright() {
		return bundleCopyright;
	}

	public String getBundleIcon() {
		return bundleIcon;
	}

	public String getBundleLocalization() {
		return bundleLocalization;
	}

	public String getBundleManifestVersion() {
		return bundleManifestVersion;
	}

	public String getBundleNativeCode() {
		return bundleNativeCode;
	}

	public String getBundleRequiredExecutionEnvironment() {
		return bundleRequiredExecutionEnvironment;
	}

	public String getBundleUpdateLocation() {
		return bundleUpdateLocation;
	}

	public String getExportService() {
		return exportService;
	}

	public String getFragmentHost() {
		return fragmentHost;
	}

	public String getIgnorePackage() {
		return ignorePackage;
	}

	public String getImportBundle() {
		return importBundle;
	}

	public String getImportLibrary() {
		return importLibrary;
	}

	public String getImportService() {
		return importService;
	}

	public List<String> getIncludeResource() {
		return includeResource;
	}

	public String getModuleScope() {
		return moduleScope;
	}

	public String getModuleType() {
		return moduleType;
	}

	public List<String> getPrivatePackage() {
		return privatePackage;
	}

	public String getRequireBundle() {
		return requireBundle;
	}

	public String getWebContextPath() {
		return webContextPath;
	}

	public String getWebDispatcherServletUrlPatterns() {
		return webDispatcherServletUrlPatterns;
	}

	public String getWebFilterMappings() {
		return webFilterMappings;
	}

	private List<String> splitList(String value) {
		if (value == null) return null;
		// TODO: support escaping
		//  example: org.apache.log4j;version="e;[1.2.15,2.0.0)"e;;resolution:=optional
		// TODO: what does "e; mean?
		return List.of(value.split(","));
	}

}
