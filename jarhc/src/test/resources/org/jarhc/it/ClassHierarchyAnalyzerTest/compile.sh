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

# compile and package b-1.jar
javac --release 8 -d build/b-1 src/b-1.jar/b/*.java
jar -c -f b-1.jar -C build/b-1 b

# compile and package b-2.jar
javac --release 8 -d build/b-2 src/b-2.jar/b/*.java
jar -c -f b-2.jar -C build/b-2 b

# compile and package a.jar (depending on b-1.jar)
javac --release 8 -cp b-1.jar -d build/a src/a.jar/a/*.java
jar -c -f a.jar -C build/a a
