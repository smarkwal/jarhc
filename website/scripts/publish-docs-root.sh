#!/usr/bin/env bash
#
# Publish the host-root SEO files (robots.txt and the sitemap index) to the
# root of the gh-pages branch, where GitHub Pages serves them as
# https://jarhc.org/robots.txt and https://jarhc.org/sitemap.xml.
#
# mike deploys each docs version into a sub-directory and never touches these
# root files, so this script keeps them in sync from website/root/ on every
# deploy. It is idempotent: it only commits and pushes when something changed.
#
set -euo pipefail

# Operate from the repository root, regardless of the caller's working directory.
cd "$(git rev-parse --show-toplevel)"

# Stash the source files, since switching to gh-pages replaces the working tree.
tmp="$(mktemp -d)"
cp website/root/robots.txt website/root/sitemap.xml "$tmp/"

git fetch origin gh-pages
# Create or reset the local gh-pages branch to the just-fetched tip. Using
# FETCH_HEAD (always populated by the fetch above) works whether or not a local
# gh-pages branch already exists, without relying on remote-tracking refs.
git checkout -B gh-pages FETCH_HEAD

cp "$tmp/robots.txt" "$tmp/sitemap.xml" .
git add robots.txt sitemap.xml

if git diff --cached --quiet; then
  echo "Root robots.txt and sitemap.xml already up to date."
else
  git commit -m "Update root robots.txt and sitemap.xml for SEO"
  git push origin gh-pages
fi

git checkout -
