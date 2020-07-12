#!/usr/bin/env bash

set -e

# set the PureConfig root directory as working directory
cd "$(dirname "${BASH_SOURCE[0]}")/.."

TMP_DIR=/tmp/pureconfig_website_diff
WEBSITE_OUTPUT_DIR=docs/target/jekyll

# In order to avoid handling branch changes and non-clean working directories, we clone a new copy
# of the PureConfig repo to have its state in master.
#
# Clone or update the local copy of the repo
if [[ -d $TMP_DIR ]]; then
  git -C "$TMP_DIR" reset --hard
  git -C "$TMP_DIR" pull
elif [[ ! -e $TMP_DIR ]]; then
  git clone --depth 1 https://github.com/pureconfig/pureconfig.git "$TMP_DIR"
else
  echo "Error: $TMP_DIR exists but is not a directory"
  exit 1
fi

# Build website on current working directory and on master
(cd "$TMP_DIR"; sbt makeMicrosite)
sbt makeMicrosite

# Compare the two builds
# Using `git diff --no-index` instead of `diff -r` because its output is prettier.
git -c color.ui=always diff --no-index "$TMP_DIR/$WEBSITE_OUTPUT_DIR" "$WEBSITE_OUTPUT_DIR"
