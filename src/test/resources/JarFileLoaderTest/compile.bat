javac -source 1.8 -target 1.8 -d build src/a/A.java
jar -c -f a.jar -C build a

javac --release 8 -d build src/java/b/B.java
javac --release 11 -d build-11 src/java-11/b/B.java
jar -c -f b.jar -C build b --release 11 -C build-11 b