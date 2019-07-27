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
if ! git -c color.ui=always diff --no-index "$TMP_DIR/$WEBSITE_OUTPUT_DIR" "$WEBSITE_OUTPUT_DIR"; then
  if [[ -z $TRAVIS_PULL_REQUEST ]]; then
    # If $TRAVIS_PULL_REQUEST isn't set, we're probably outside CI or outside a pull request. In
    # this case the scripts exits with an error status code, which may be useful for automations
    # or manual checks.
    exit 1
  else
    # Else, comment on GitHub and exit with status 0 in order not to break the build.
    COMMENT_CONTENT=$(echo "\
      Warning: the content of the PureConfig website changed with this pull request. This may be \
      intentional (as is the case when sbt-microsites is updated or some breaking change occurs) \
      or may be an unexpected change in the library's behavior. Please check the logs of the \
      [\`diff_website\` job]($TRAVIS_JOB_WEB_URL) in the Travis build to see the differences." | \
        sed -E 's/ +/ /g')

    curl -f -H "Authorization: Token ${GITHUB_TOKEN}" -XPOST \
      -d "{\"body\": \"$COMMENT_CONTENT\"}" \
      "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/issues/${TRAVIS_PULL_REQUEST}/comments"
  fi
fi
