#!/bin/bash

### This script must be executed when you are at root directory of this project
docker compose -f ./services/storages.compose.yml $@
