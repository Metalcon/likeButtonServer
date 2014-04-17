#!/bin/bash

configPath=/usr/share/metalcon/like

sudo mkdir -p $configPath
sudo chown `whoami` $configPath
cp main.cfg $configPath
