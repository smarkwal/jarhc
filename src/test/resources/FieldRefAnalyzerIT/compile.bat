
REM compile and package b-1.jar
javac --release 8 -d build/b-1 src/b-1.jar/b/B.java
jar -c -f b-1.jar -C build/b-1 b

REM compile and package b-2.jar
javac --release 8 -d build/b-2 src/b-2.jar/b/B.java
jar -c -f b-2.jar -C build/b-2 b

REM compile and package a.jar (depending on b-1.jar)
javac --release 8 -cp b-1.jar -d build/a src/a.jar/a/A.java
jar -c -f a.jar -C build/a a
