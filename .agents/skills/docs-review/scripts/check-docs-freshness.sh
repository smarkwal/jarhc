#!/usr/bin/env bash
#
# check-docs-freshness.sh
#
# Flags documentation pages whose front-matter "sources" have a newer git commit
# date than the page's "last_reviewed" date, so they can be re-reviewed. Part of
# the docs-review skill.
#
# A page is tracked when its YAML front matter contains a "last_reviewed" date
# (YYYY-MM-DD). Pages without it are listed separately as "not tracked".
#
# Usage:
#   check-docs-freshness.sh [docs-dir]
#
# Default docs-dir is "<repo-root>/docs". Exits non-zero if any stale page,
# missing source, or source without git history is found.

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
docs_dir="${1:-$repo_root/docs}"

stale=0
problems=0
no_frontmatter=()

while IFS= read -r page; do
	rel_page="${page#"$repo_root"/}"

	# Extract "last_reviewed" and the "sources" list from the front matter.
	reviewed=""
	sources=()
	while IFS= read -r field; do
		case "$field" in
			"REVIEWED "*) reviewed="${field#REVIEWED }" ;;
			"SOURCE "*) sources+=("${field#SOURCE }") ;;
		esac
	done < <(awk '
		NR==1 && $0=="---" { fm=1; next }
		fm && $0=="---" { exit }
		fm {
			if ($1=="last_reviewed:") { print "REVIEWED " $2; next }
			if ($0 ~ /^sources:/) { insrc=1; next }
			if (insrc && $0 ~ /^[[:space:]]+-[[:space:]]*/) {
				s=$0; sub(/^[[:space:]]*-[[:space:]]*/,"",s); print "SOURCE " s; next
			}
			if ($0 ~ /^[^[:space:]]/) { insrc=0 }
		}
	' "$page")

	# Pages without a review date are not tracked.
	if [ -z "$reviewed" ]; then
		no_frontmatter+=("$rel_page")
		continue
	fi

	# Compare each source's last commit date against the review date.
	if [ "${#sources[@]}" -gt 0 ]; then
		for src in "${sources[@]}"; do
			if [ ! -e "$repo_root/$src" ]; then
				echo "MISSING  $rel_page -> $src (source not found)"
				problems=$((problems + 1))
				continue
			fi
			src_date="$(git -C "$repo_root" log -1 --format=%cd --date=short -- "$src" 2>/dev/null || true)"
			if [ -z "$src_date" ]; then
				echo "NOHIST   $rel_page -> $src (no git history; uncommitted?)"
				problems=$((problems + 1))
				continue
			fi
			# ISO dates compare correctly as plain strings.
			if [[ "$src_date" > "$reviewed" ]]; then
				echo "STALE    $rel_page (reviewed $reviewed) <- $src (changed $src_date)"
				stale=$((stale + 1))
			fi
		done
	fi
done < <(find "$docs_dir" -type f -name '*.md' | LC_ALL=C sort)

echo
echo "Checked docs in: $docs_dir"
echo "Stale findings: $stale, other problems: $problems"

if [ "${#no_frontmatter[@]}" -gt 0 ]; then
	echo
	echo "Pages without a last_reviewed date (not tracked):"
	for p in "${no_frontmatter[@]}"; do
		echo "  $p"
	done
fi

if [ "$stale" -gt 0 ] || [ "$problems" -gt 0 ]; then
	exit 1
fi
exit 0
