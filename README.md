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

#### Configuration over SSH with utility scripts

##### Prerequisites
1. Configure your ethernet interface to have a static `10.0.0.1` IP address and a `255.255.255.0` netmask (the RPi images have their IP hardcoded to  `10.0.0.2` and the gateway to `10.0.0.1`).

2. Add a `vote_reporter` entry to your local `~/.ssh/config`:
	```
	Host vote_reporter
		HostName 10.0.0.2
		User pi
		StrictHostKeyChecking no
		UserKnownHostsFile=/dev/null
	```

	The default password for the `pi` user is `smlRzondzi`.

##### Running the scripts

If you have a single script to run (i.e. you won't need to enter the password a hundred times), you can run a single command on the RPi with:
```bash
ssh vote_reporter your_command
```

or all commands from a script file with:

```bash
ssh vote_reporter 'bash -s' < your_script
```

If you need to run multiple scripts (or a single one multiple times) on a single RPi, it's reasonable to configure the use of your private key instead of password for authentication, which is another one or two commands prior to running the scripts:

```bash
brew install ssh-copy-id # OS X only, Linux already has it
ssh-copy-id vote_reporter

```

##### Utility scripts

- [wifi.sh](scripts/wifi.sh) - configures the SSID and password used by WPA Supplicant. It looks for the given `SSID` in `/etc/wpa_supplicant/wpa_supplicant.conf` and iff it's not there, adds a new entry with the given `SSID` and `password`, then runs `wpa_cli reconfigure` to apply the changes. Usage:

	```bash
	wifi.sh SSID password
	```

- [set-device-key.sh](scripts/set-device-key.sh) - updates the `device-key` in `/opt/voter/application.conf` to `new_key`. Usage:

	```
	set-device-key.sh new_key
	```

	**NOTE**: you need to restart the voter service for the change to take effect.

- [set-vote-counter-endpoint.sh](scripts/set-vote-counter-endpoint) - updates the `vote-counter-endpoint` in `/opt/voter/application.conf` to `new_endpoint`. Usage:

	```
	set-vote-counter-endpoint new_endpoint
	```

	**NOTE**: you need to restart the voter service for the change to take effect.

- [restart-voter-service.sh](scripts/restart-voter-service.sh), [reboot.sh](scripts/reboot.sh) - self-explanatory


### Running
Vote reporter is configured as a system service, so normally it should be enough to manipulate it with:
```bash
/etc/init.d/voter [start|stop|restart]
```

You can always manually run the server in `/home/pi` with:

```
sudo java -jar vote-reporter-assembly-1.0.jar -Dconfig.file=application.conf
```
