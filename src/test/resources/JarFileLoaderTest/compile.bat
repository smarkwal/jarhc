REM SET PATH=C:\Program Files\Java\jdk-11.0.1\bin

javac --release 8 -d build/a src/a.jar/a/A.java
jar -c -f a.jar -C build/a a

javac --release 8 -d build/b/java8 src/b.jar/java8/b/B.java
javac --release 11 -d build/b/java11 src/b.jar/java11/b/B.java
jar -c -f b.jar -C build/b/java8 b --release 11 -C build/b/java11 b

javac --release 9 --module-path b.jar -d build/c src/c.jar/c/C.java src/c.jar/module-info.java
jar -c -f c.jar -C build/c c -C build/c module-info.class