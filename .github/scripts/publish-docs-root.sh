#!/usr/bin/env bash
#
# Publish the host-root SEO files (robots.txt and the sitemap index) to the
# root of the gh-pages branch, where GitHub Pages serves them as
# https://jarhc.org/robots.txt and https://jarhc.org/sitemap.xml.
#
# mike deploys each docs version into a sub-directory and never touches these
# root files, so this script keeps them in sync from docs-root/ on every deploy.
# It is idempotent: it only commits and pushes when something actually changed.
#
set -euo pipefail

# Stash the source files, since switching to gh-pages replaces the working tree.
tmp="$(mktemp -d)"
cp docs-root/robots.txt docs-root/sitemap.xml "$tmp/"

git fetch origin gh-pages
git checkout gh-pages

cp "$tmp/robots.txt" "$tmp/sitemap.xml" .
git add robots.txt sitemap.xml

if git diff --cached --quiet; then
  echo "Root robots.txt and sitemap.xml already up to date."
else
  git commit -m "Update root robots.txt and sitemap.xml for SEO"
  git push origin gh-pages
fi

git checkout -
