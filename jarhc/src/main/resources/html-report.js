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

	initNavUI();

	// check if document is a diff report
	const body = document.getElementsByTagName("body")[0];
	if (body.classList.contains("diff-report")) {
		initDiffUI();
	}

	initRowMarking();
	initKeyboardShortcuts();
}

function initNavUI() {

	// link to next section
	const link1 = createIconLink("icon-down", "Next section", function() {
		scrollToSection(true);
	});

	// link to previous section
	const link2 = createIconLink("icon-up", "Previous section", function() {
		scrollToSection(false);
	});

	const controls = document.getElementById("controls");
	controls.appendChild(link1);
	controls.appendChild(link2);
}

function createIconLink(iconClass, text, onclick) {

	const icon = document.createElement("span");
	icon.classList.add("icon");
	icon.classList.add(iconClass);

	const label = document.createElement("span");
	label.classList.add("label");
	label.innerText = text;

	return createControl(icon, label, onclick);
}

function scrollToSection(next) {

	const targetY = 80; // target scroll position (relative to viewport)

	// find next section
	let nextSection = null;
	let nextSectionY = next ? Number.MAX_VALUE : -Number.MAX_VALUE;

	// iterate over all sections
	const sections = document.getElementsByClassName("report-section");
	for (let section of sections) {

		// get position of section (relative to viewport)
		const sectionY = section.getBoundingClientRect().top;

		// check if section is closer than previous next section
		let better = next ? (sectionY > targetY + 10 && sectionY < nextSectionY) : (sectionY < targetY - 10 && sectionY > nextSectionY);
		if (better) {
			nextSection = section;
			nextSectionY = sectionY;
		}
	}

	// if there is a next section ...
	if (nextSection) {

		// get current vertical scroll position
		const scrollY = window.scrollY;

		// scroll to next section
		window.scrollTo(0, scrollY + nextSectionY - targetY);

	} else if (!next) { // if there is no previous section

		// scroll to top
		window.scrollTo(0, 0);
	}

}

function initDiffUI() {

	showOnlyDiff(true);

	const div = createCheckbox("Show only differences", true, function() {
		showOnlyDiff(this.checked);
	});

	const controls = document.getElementById("controls");
	controls.appendChild(div);
}

function createCheckbox(text, checked, onchange) {

	// create checkbox
	const input = document.createElement("input");
	input.type = "checkbox";
	input.checked = checked;
	input.onchange = onchange;

	// create label for checkbox
	const label = document.createElement("span");
	label.classList.add("label");
	label.innerText = text;
	label.onclick = function() {
		input.click();
	};

	return createControl(input, label, null);
}

function createControl(control, label, onclick) {

	const div = document.createElement("div");
	div.classList.add("control");
	if (onclick) {
		div.onclick = onclick
	}

	div.appendChild(control);
	div.appendChild(label);

	return div;
}

function showOnlyDiff(enabled) {

	// show or hide table rows
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

	// show or hide issues
	const issues = document.getElementsByClassName("report-issue");
	for (let issue of issues) {
		if (!issue.classList.contains("diff")) {
			if (enabled) {
				issue.style.display = "none";
			} else {
				issue.style.display = "block";
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

function initKeyboardShortcuts() {

	// prepare event handler
	function handleShortcut(event) {
		if (event.ctrlKey && event.key === "ArrowUp") { // Ctrl + ArrowUp
			event.preventDefault();
			// scroll to previous section
			scrollToSection(false);
		} else if (event.ctrlKey && event.key === "ArrowDown") { // Ctrl + ArrowDown
			event.preventDefault();
			// scroll to next section
			scrollToSection(true);
		}
	}

	// register event handler
	document.addEventListener("keydown", handleShortcut);
}

init();
