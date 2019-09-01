#!/bin/bash

jar cf test.war -C src WEB-INF -C src index.html
unzip -l test.war
