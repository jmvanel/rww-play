rww-play 
========

This is an implementation in Play of a number of tools to build a Read-Write-Web server using Play2.x and akka.
It is very early stages at present and it implements sketches of the following

* A [CORS](http://www.w3.org/TR/cors/) proxy
* An initial implementation of [Linked Data Basic Profile](http://www.w3.org/2012/ldp/wiki/Main_Page)

This currently works in the [TLS branch of the bblfish fork of Play 2.x](https://github.com/bblfish/Play20), which comes with TLS support and a few more patches.

We use [Travis CI](http://travis-ci.org/) to verify the build: [![Build Status](https://travis-ci.org/read-write-web/rww-play.png)](http://travis-ci.org/read-write-web/rww-play)



Getting going
-------------


* You need Java 7 at least - the official Oracle JVM or another one based on [the GPLed code](http://openjdk.java.net/): removing the dependency on Oracle's JVM will require [publishing of the GPLed java security libs](http://stackoverflow.com/questions/12982595/openjdk-sun-security-libs-on-maven)
* clone [this project](https://github.com/stample/rww-play) 

```bash
 $ git clone git://github.com/stample/rww-play.git 
``` 

In the `rww-play` home directory, run the `build` bash script. It will download a precompiled tuned 
version of play, build the application, and run it. (If there is no remotely downloadable version
it will build it from source in the `Play20` directory.)

```bash
$ ./build
```
Some network config.
--------------------

In file:
conf/application.conf
set the smtp parameters: host= and user=
of your mail provider server.

In file:
/etc/hosts
add host names for the subdomains you will create, e.g. :
127.0.0.1 jmv.localhost
127.0.0.1 jmv1.localhost
127.0.0.1 jmv2.localhost

Installing RWW apps
----------
The RWW apps are stored in other git repositories.
One can run the script `./install-app.sh` to install or update the RWW apps that we ship with the platform.
Check the script content, it is simply a git clone.

Running
-------
To start Play in secure mode with lightweight client certificate verification (for WebID); that is, a self-signed certificate:

```bash
 $ Play20/play
 [RWWeb] $ idea with-sources=yes	// if you want to run intelliJ
 [RWWeb] $ eclipse with-source=true	// if you want to run eclipse Scala IDE
 [RWWeb] $ compile
 [RWWeb] $ ~run -Dhttps.port=8443 -Dhttps.trustStore=noCA -Drww.subdomains=true -Dakka.loglevel=DEBUG -Dakka.debug.receive=on -Dsmtp.password=xxxx


Then you can direct your browser to:
https://localhost:8443/


Documentation
-------------

Further documentation can be found on the [rww-play wiki](https://github.com/stample/rww-play/wiki).

Licence
-------

   Copyright 2013 Henry Story

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
