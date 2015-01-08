#!/bin/bash
ssh vote_reporter "sed -i 's,\(device-key:\) .*,\1 \"$1\",g' /opt/voter/application.conf"
