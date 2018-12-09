# JarHC - JAR Health Check

A tool to statically analyze dependencies and references between a set of JAR (Java Archive) files, and to produce a report with all incompatibilities and potential issues. 
The goal is to give a qualitative answer to the question how "consistent" a set of JAR files is on binary API level.

## Usage

JarHC is currently available only as a command line tool.

    java -jar jarhc.jar <path> [<path>]*
    Where
       <path> : Path to *.jar file or directory with *.jar files (searched recursively).

It will print a text report to STDOUT.
