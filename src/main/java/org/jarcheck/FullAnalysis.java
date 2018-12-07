package org.jarcheck;

import org.jarcheck.analyzer.*;

public class FullAnalysis extends Analysis {

	public FullAnalysis() {
		super(
				new JarFilesListAnalyzer(),
				new ClassVersionAnalyzer(),
				new PackagesAnalyzer(),
				new SplitPackagesAnalyzer(),
				new DuplicateClassesAnalyzer(),
				new ShadowedClassesAnalyzer(),
				new JarDependenciesAnalyzer(),
				new MissingClassesAnalyzer()
		);
	}

}
