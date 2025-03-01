function init() {

	initNavUI();

	// check if document is a diff report
	const body = document.getElementsByTagName("body")[0];
	if (body.classList.contains("diff-report")) {
		initDiffUI();
	}

	initRowMarking();
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

	const div = createCheckbox("Show only differences", function() {
		showOnlyDiff(this.checked);
	});

	const controls = document.getElementById("controls");
	controls.appendChild(div);
}

function createCheckbox(text, onchange) {

	// create checkbox
	const input = document.createElement("input");
	input.type = "checkbox";
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
