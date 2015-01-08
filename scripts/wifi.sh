#!/bin/bash
CONFIG_FILE="/etc/wpa_supplicant/wpa_supplicant.conf"
ssh vote_reporter "if ! sudo grep -q $1 $CONFIG_FILE; then echo -e 'network={\nssid=\"$1\"\npsk=\"$2\"\n}' | sudo tee -a $CONFIG_FILE > /dev/null; wpa_cli reconfigure; fi"
