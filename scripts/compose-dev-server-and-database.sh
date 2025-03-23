#!/bin/bash

### This script must be executed when you are at root directory of this project
docker compose -f compose.dev.yml -f services/storages.compose.yml -p template-sb3-restful $@
