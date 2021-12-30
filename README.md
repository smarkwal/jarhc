# JarHC - JAR Health Check

JarHC is a static analysis tool to help you find your way through "JAR hell" or "classpath hell".

Its main purpose is to analyze a set of JAR files (\*.jar) and check whether they are compatible on a binary level, and whether they contain any "unpleasant surprises" for you.

It is a standalone Java application run from the command line:

```shell
java -jar jarhc-with-deps.jar [options] <path> [<path>]*
```

More information can be found in the [wiki](https://github.com/smarkwal/jarhc/wiki).

![JarHC Logo](https://github.com/smarkwal/jarhc/blob/master/src/site/images/jarhc-logo.png?raw=true)

---

[![License](https://img.shields.io/github/license/smarkwal/jarhc?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Release](https://img.shields.io/github/v/release/smarkwal/jarhc?label=Latest)](https://github.com/smarkwal/jarhc/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/smarkwal/jarhc/total?label=Downloads)](https://github.com/smarkwal/jarhc/releases)

[![Build](https://github.com/smarkwal/jarhc/actions/workflows/build.yml/badge.svg)](https://github.com/smarkwal/jarhc/actions/workflows/build.yml)
[![Tests](https://img.shields.io/sonar/tests/smarkwal_jarhc/master?label=Tests&server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?id=smarkwal_jarhc&metric=test_success_density&selected=smarkwal_jarhc%3Asrc%2Ftest%2Fjava%2Forg%2Fjarhc)
[![Coverage](https://img.shields.io/sonar/coverage/smarkwal_jarhc/master?label=Coverage&server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?id=smarkwal_jarhc&metric=coverage&view=list)
[![Quality](https://img.shields.io/sonar/quality_gate/smarkwal_jarhc/master?label=Quality&server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/dashboard?id=smarkwal_jarhc)

[![Issues](https://img.shields.io/github/issues/smarkwal/jarhc?label=Issues)](https://github.com/smarkwal/jarhc/issues)
