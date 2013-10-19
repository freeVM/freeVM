#!/usr/bin/bash

#           Licensed to the Apache Software Foundation (ASF) under one
#           or more contributor license agreements.  See the NOTICE file
#           distributed with this work for additional information
#           regarding copyright ownership.  The ASF licenses this file
#           to you under the Apache License, Version 2.0 (the
#           "License"); you may not use this file except in compliance
#           with the License.  You may obtain a copy of the License at
#
#             http://www.apache.org/licenses/LICENSE-2.0
#
#           Unless required by applicable law or agreed to in writing,
#           software distributed under the License is distributed on an
#           "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#           KIND, either express or implied.  See the License for the
#           specific language governing permissions and limitations
#           under the License.    
#
ln -s apache-harmony-hdk-r${1}-linux-amd64-64-snapshot.tar.gz latest-harmony-hdk-linux-x86_64.tar.gz
ln -s apache-harmony-hdk-r${1}-linux-amd64-64-snapshot.tar.gz.md5 latest-harmony-hdk-linux-x86_64.tar.gz.md5
ln -s apache-harmony-hdk-r${1}-linux-amd64-64-snapshot.tar.gz.sha latest-harmony-hdk-linux-x86_64.tar.gz.sha

ln -s apache-harmony-hdk-r${1}-linux-x86-32-snapshot.tar.gz latest-harmony-hdk-linux-x86.tar.gz
ln -s apache-harmony-hdk-r${1}-linux-x86-32-snapshot.tar.gz.md5 latest-harmony-hdk-linux-x86.tar.gz.md5
ln -s apache-harmony-hdk-r${1}-linux-x86-32-snapshot.tar.ga.shaz latest-harmony-hdk-linux-x86.tar.gz.sha

ln -s apache-harmony-hdk-r${1}-windows-x86-32-snapshot.zip latest-harmony-hdk-windows-x86.zip
ln -s apache-harmony-hdk-r${1}-windows-x86-32-snapshot.zip.md5 latest-harmony-hdk-windows-x86.zipmd5
ln -s apache-harmony-hdk-r${1}-windows-x86-32-snapshot.zip.sha latest-harmony-hdk-windows-x86.zip.sha

ln -s apache-harmony-jdk-r${1}-linux-amd64-64-snapshot.tar.gz latest-harmony-jdk-linux-x86_64.tar.gz
ln -s apache-harmony-jdk-r${1}-linux-amd64-64-snapshot.tar.gz.md5 latest-harmony-jdk-linux-x86_64.tar.gz.md5
ln -s apache-harmony-jdk-r${1}-linux-amd64-64-snapshot.tar.gz.sha latest-harmony-jdk-linux-x86_64.tar.gz.sha

ln -s apache-harmony-jdk-r${1}-linux-x86-32-snapshot.tar.gz latest-harmony-jdk-linux-x86.tar.gz
ln -s apache-harmony-jdk-r${1}-linux-x86-32-snapshot.tar.gz.md5 latest-harmony-jdk-linux-x86.tar.gz.md5
ln -s apache-harmony-jdk-r${1}-linux-x86-32-snapshot.tar.gz.sha latest-harmony-jdk-linux-x86.tar.gz.sha

ln -s apache-harmony-jdk-r${1}-windows-x86-32-snapshot.zip latest-harmony-jdk-windows-x86.zip
ln -s apache-harmony-jdk-r${1}-windows-x86-32-snapshot.zip.md5 latest-harmony-jdk-windows-x86.zip.md5
ln -s apache-harmony-jdk-r${1}-windows-x86-32-snapshot.zip.sha latest-harmony-jdk-windows-x86.zip.sha

ln -s apache-harmony-jre-r${1}-linux-amd64-64-snapshot.tar.gz latest-harmony-jre-linux-x86_64.tar.gz
ln -s apache-harmony-jre-r${1}-linux-amd64-64-snapshot.tar.gz.md5 latest-harmony-jre-linux-x86_64.tar.gz.md5
ln -s apache-harmony-jre-r${1}-linux-amd64-64-snapshot.tar.gz.sha latest-harmony-jre-linux-x86_64.tar.gz.sha

ln -s apache-harmony-jre-r${1}-linux-x86-32-snapshot.tar.gz latest-harmony-jre-linux-x86.tar.gz
ln -s apache-harmony-jre-r${1}-linux-x86-32-snapshot.tar.gz.md5 latest-harmony-jre-linux-x86.tar.gz.md5
ln -s apache-harmony-jre-r${1}-linux-x86-32-snapshot.tar.gz.sha latest-harmony-jre-linux-x86.tar.gz.sha

ln -s apache-harmony-jre-r${1}-windows-x86-32-snapshot.zip latest-harmony-jre-windows-x86.zip
ln -s apache-harmony-jre-r${1}-windows-x86-32-snapshot.zip.md5 latest-harmony-jre-windows-x86.zip.md5
ln -s apache-harmony-jre-r${1}-windows-x86-32-snapshot.zip.sha latest-harmony-jre-windows-x86.zip.sha

chmod 444 *

