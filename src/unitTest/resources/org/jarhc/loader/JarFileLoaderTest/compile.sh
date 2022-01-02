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

javac --release 8 -d build/a src/a.jar/a/A.java
jar -c -f a.jar -C build/a a

javac --release 8 -d build/b/java8 src/b.jar/java8/b/B.java
javac --release 11 -d build/b/java11 src/b.jar/java11/b/B.java
jar -c -f b.jar -C build/b/java8 b --release 11 -C build/b/java11 b

javac --release 9 --module-path b.jar -d build/c src/c.jar/c/C.java src/c.jar/module-info.java
jar -c -f c.jar -C build/c c -C build/c module-info.class

jar -c -f x.jar a.jar b.jar -C build/c c
