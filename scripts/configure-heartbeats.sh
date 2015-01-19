#!/bin/bash
ssh vote_reporter "sed -i 's,\([^-]endpoint:\) .*,\1 \"$1\",g' /opt/voter/application.conf; sed -i 's,\(interval:\) .*,\1 $2,g' /opt/voter/application.conf"