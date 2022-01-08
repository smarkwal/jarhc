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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

class Annotations {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface TypeAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface FieldAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface MethodAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface ParameterAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.CONSTRUCTOR)
	@interface ConstructorAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.LOCAL_VARIABLE)
	@interface LocalVariableAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.ANNOTATION_TYPE)
	@interface AnnotationTypeAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PACKAGE)
	@interface PackageAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_PARAMETER)
	@interface TypeParameterAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface TypeUseAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.MODULE)
	@interface ModuleAnnotation {
	}

}