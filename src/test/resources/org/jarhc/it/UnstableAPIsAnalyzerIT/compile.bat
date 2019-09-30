
REM compile and package b.jar
javac --release 8 -cp annotations.jar -d build/b src/b.jar/b/*.java
jar -c -f b.jar -C build/b b

REM compile and package a.jar (depending on b.jar)
javac --release 8 -cp b.jar -d build/a src/a.jar/a/A.java
jar -c -f a.jar -C build/a a
