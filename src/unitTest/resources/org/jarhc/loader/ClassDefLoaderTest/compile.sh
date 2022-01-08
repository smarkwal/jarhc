#!/bin/bash

#
# Copyright 2022 Stephan Markwalder
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

javac -source 1.6 -target 1.6 -d java6 Main.java
javac -source 1.6 -target 1.6 -d java6 ClassRefs.java
javac -source 1.7 -target 1.7 -d java7 Main.java
javac -source 1.8 -target 1.8 -d java8 Main.java
javac -source 9 -target 9 -d java9 Main.java
javac -source 9 -target 9 -d java9 Annotations.java
javac -source 9 -target 9 -d java9 AnnotationRefs.java
javac -source 10 -target 10 -d java10 Main.java
javac -source 11 -target 11 -d java11 Main.java
javac --release 12 -d java12 Main.java
javac --release 13 -d java13 Main.java
javac --release 14 -d java14 Main.java
javac --release 15 -d java15 Main.java
javac --release 16 -d java16 Main.java
javac --release 16 -d java16 MyAnnotation.java
javac --release 16 -cp java16 -d java16 Record.java
javac --release 17 -d java17 Main.java
javac --release 17 -d java17 SealedParent.java FinalChild.java
