/*
 * Copyright 2025 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function init() {

	// check if document is a diff report
	const body = document.getElementsByTagName("body")[0];
	if (body.classList.contains("diff-report")) {
		initDiffUI();
	}

	initRowMarking();
}

function initDiffUI() {
	const div = document.createElement("div");

	// render checkbox
	const input = document.createElement("input");
	input.type = "checkbox";
	input.style.marginLeft = "0";
	input.onchange = function() {
		showOnlyDiff(this.checked);
	};
	div.appendChild(input);

	// render label for checkbox
	const span = document.createElement("span");
	span.innerText = "Show only differences";
	span.style.cursor = "default";
	span.onclick = function() {
		input.click();
	};
	div.appendChild(span);

	const section = document.getElementById("Diff");
	section.appendChild(div);
}

function showOnlyDiff(enabled) {
	const rows = document.getElementsByClassName("report-table-row");
	for (let row of rows) {
		if (!row.classList.contains("diff")) {
			if (enabled) {
				row.style.display = "none";
			} else {
				row.style.display = "table-row";
			}
		}
	}
}

function initRowMarking() {

	const toggleMarked = function(element) {
		if (element.classList.contains("marked")) {
			element.classList.remove("marked");
		} else {
			element.classList.add("marked");
		}
	}

	const rows = document.getElementsByClassName("report-table-row");
	for (let row of rows) {
		row.ondblclick = () => {
			toggleMarked(row)
		}
	}
}

init();
