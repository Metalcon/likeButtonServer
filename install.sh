#!/bin/bash

configPath=/usr/share/metalcon/like

sudo mkdir -p $configPath
sudo chown `whoami`:users $configPath
cp main.cfg $configPath
