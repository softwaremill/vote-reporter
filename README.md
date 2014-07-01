Vote reporter
=============

Preparing the Raspberry Pi
-----------------------------------

### Required software
- avahi-daemon
- ntpdate
- oracle-java8-jdk

### Configuration
Set the device-id and vote-counter-endpoint in application.conf

### Running

```
sudo java -jar vote-reporter-assembly-1.0.jar -Dconfig.file=application.conf
```
