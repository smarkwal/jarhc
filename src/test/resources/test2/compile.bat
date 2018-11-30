javac -source 1.8 -target 1.8 -d build src/a/A.java
jar -c -f a.jar -C build a
