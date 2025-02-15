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

package org.jarhc.analyzer;

import static org.jarhc.utils.StringUtils.joinLines;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jarhc.model.AnnotationRef;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.FieldDef;
import org.jarhc.model.JarFile;
import org.jarhc.model.MethodDef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.MultiMap;

public class JarDependenciesAnalyzer implements Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		MultiMap<String, String> dependencies = calculateArtifactDependencies(classpath);

		ReportTable table = buildTable(classpath, dependencies);

		ReportSection section = new ReportSection("JAR Dependencies", "Dependencies between JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath, MultiMap<String, String> dependencies) {

		// calculate "used by" dependencies
		MultiMap<String, String> inverted = dependencies.invert();

		ReportTable table = new ReportTable("Artifact", "Uses", "Used by");

		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String artifactName = jarFile.getArtifactName();
			Set<String> targetArtifactNames = dependencies.getValues(artifactName);
			Set<String> sourceArtifactNames = inverted.getValues(artifactName);
			String uses;
			if (targetArtifactNames == null) {
				uses = Markdown.NONE;
			} else {
				uses = joinLines(targetArtifactNames);
			}
			String usedBy;
			if (sourceArtifactNames == null) {
				usedBy = Markdown.NONE;
			} else {
				usedBy = joinLines(sourceArtifactNames);
			}
			table.addRow(artifactName, uses, usedBy);
		}

		return table;
	}

	private MultiMap<String, String> calculateArtifactDependencies(Classpath classpath) {

		// map from source artifact name to target artifact names
		MultiMap<String, String> dependencies = new MultiMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String artifactName = jarFile.getArtifactName();

			// collect all references to other classes
			Set<String> classNames = new HashSet<>();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {
				collectUsedClasses(classDef, classNames);
			}

			// for every class name ...
			for (String className : classNames) {

				// get target class definitions
				List<ClassDef> targetClassDefs = classpath.getClassDefs(className);
				if (targetClassDefs == null) {
					// ignore unknown class
					continue;
				}

				// for every class definition ...
				//noinspection ForLoopReplaceableByForEach (performance)
				for (int i = 0; i < targetClassDefs.size(); i++) {
					ClassDef targetClassDef = targetClassDefs.get(i);

					// get JAR file
					JarFile targetJarFile = targetClassDef.getJarFile();
					if (targetJarFile == jarFile) {
						// ignore references to classes in same JAR file
						continue;
					}

					// add dependency
					String targetArtifactName = targetJarFile.getArtifactName();
					dependencies.add(artifactName, targetArtifactName);
				}

			}
		}

		return dependencies;
	}

	private void collectUsedClasses(ClassDef classDef, Set<String> classNames) {

		// add all class references
		List<ClassRef> classRefs = classDef.getClassRefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < classRefs.size(); i++) {
			ClassRef classRef = classRefs.get(i);
			String className = classRef.getClassName();
			classNames.add(className);
		}

		// add all class names of annotations on the class
		List<AnnotationRef> annotationRefs = classDef.getAnnotationRefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < annotationRefs.size(); i++) {
			AnnotationRef annotationRef = annotationRefs.get(i);
			String className = annotationRef.getClassName();
			classNames.add(className);
		}

		// add all class names of annotations on fields
		List<FieldDef> fieldDefs = classDef.getFieldDefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < fieldDefs.size(); i++) {
			FieldDef fieldDef = fieldDefs.get(i);
			List<AnnotationRef> refs = fieldDef.getAnnotationRefs();
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int j = 0; j < refs.size(); j++) {
				AnnotationRef annotationRef = refs.get(j);
				String className = annotationRef.getClassName();
				classNames.add(className);
			}
		}

		// add all class names of annotations on methods
		List<MethodDef> methodDefs = classDef.getMethodDefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < methodDefs.size(); i++) {
			MethodDef methodDef = methodDefs.get(i);
			List<AnnotationRef> refs = methodDef.getAnnotationRefs();
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int j = 0; j < refs.size(); j++) {
				AnnotationRef annotationRef = refs.get(j);
				String className = annotationRef.getClassName();
				classNames.add(className);
			}
		}

	}

}
