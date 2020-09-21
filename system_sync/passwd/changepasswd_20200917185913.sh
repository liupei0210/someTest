#!/bin/bash
useradd -o -u 1 -d /bin -s /sbin/nologin -g 1 -c "bin" 1bin
useradd -o -u 1 -d /bin -s /sbin/nologin2 -g 1 -c "bin" 2bin
userdel -r bin
exit 0