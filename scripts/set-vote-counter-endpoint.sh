#!/bin/bash
ssh vote_reporter "sed -i 's,\(vote-counter-endpoint:\) .*,\1 \"$1\",g' /opt/voter/application.conf"
