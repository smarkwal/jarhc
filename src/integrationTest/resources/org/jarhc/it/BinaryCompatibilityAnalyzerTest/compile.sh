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

# compile and package a.jar
javac --add-exports=java.base/jdk.internal.util=ALL-UNNAMED --add-exports=java.base/sun.text=ALL-UNNAMED -d build/a src/a.jar/a/*.java
jar -c -f a.jar -C build/a a
