
REM compile and package a.jar
javac --release 9 --add-modules jdk.unsupported -d build/a src/a.jar/a/*.java
jar -c -f a.jar -C build/a a
