#!/usr/bin/env bash

set -e

# set the PureConfig root directory as working directory
cd "$(dirname "${BASH_SOURCE[0]}")/.."

WEBSITE_REPO="git@github.com:pureconfig/pureconfig.github.io.git"
WEBSITE_DIR="docs/target/website"

VERSION=$(sed -E 's/ThisBuild \/ version := "([^"]+)"/\1/' < version.sbt)
COMMIT_MESSAGE="Update website for version $VERSION"

echo "Generating website for version $VERSION"

# Clone or update the local copy of the website repo
if [[ -d $WEBSITE_DIR ]]; then
  git -C "$WEBSITE_DIR" reset --hard
  git -C "$WEBSITE_DIR" pull
elif [[ ! -e $WEBSITE_DIR ]]; then
  git clone $WEBSITE_REPO "$WEBSITE_DIR"
else
  echo "Error: $WEBSITE_DIR exists but is not a directory"
  exit 1
fi

# delete all files and folders at the website that aren't a version folder
ls -d -1 "$WEBSITE_DIR"/** | grep -Ev 'v[0-9]+\.[0-9]+' | xargs rm -r

# generate the main website pages
sbt "set docs / siteDirectory := file(\"$WEBSITE_DIR\")" \
    makeMicrosite

# generate the website pages for this version
rm -rf 'docs/target/streams/_global/makeSite'
sbt "set docs / siteDirectory := file(\"$WEBSITE_DIR/v$VERSION\")" \
    "set docs / micrositeBaseUrl := \"v$VERSION\"" \
    makeMicrosite

# show Git status and prompt for user confirmation before pushing changes
git -C "$WEBSITE_DIR" add -A
echo
echo "The website for version $VERSION was generated."
echo
git -C "$WEBSITE_DIR" status

read -rp "Press Enter to commit and push these changes..."

# commit and push the changes
git -C "$WEBSITE_DIR" commit -m "$COMMIT_MESSAGE"
git -C "$WEBSITE_DIR" push
