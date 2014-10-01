Vote reporter
=============

## Development
If you want to report issues please do so at vote-counter's [issue tracker](https://github.com/softwaremill/vote-counter/issues).

## Deployment

### Preparing the Raspberry Pi

#### Required software
- avahi-daemon
- ntpdate
- oracle-java8-jdk

#### Configuration
Set the device-id and vote-counter-endpoint in application.conf

#### Running
```
sudo java -jar vote-reporter-assembly-1.0.jar -Dconfig.file=application.conf
```
