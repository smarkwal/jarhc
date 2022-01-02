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

# compile and package b.jar
javac --release 8 -cp annotations.jar -d build/b src/b.jar/b/*.java
jar -c -f b.jar -C build/b b

# compile and package a.jar (depending on b.jar)
javac --release 8 -cp b.jar -d build/a src/a.jar/a/A.java
jar -c -f a.jar -C build/a a
