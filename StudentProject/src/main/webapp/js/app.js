/**
 * app.js — Student Record System
 *
 * Fully modular and resilient client logic:
 * - Dynamic API_URL calculation (works across all deployment context paths)
 * - Safe data-bound modal operations avoiding quote-escaping issues
 * - Compatible JavaScript syntax (no unhandled nullish operators or broken inline handlers)
 * - Toast notifications for user feedback
 * - In-sync summary stats and record counter
 */

const API_URL = (function () {
    const path = window.location.pathname;
    const dir = path.substring(0, path.lastIndexOf('/') + 1);
    return dir + 'api/students';
})();

// In-memory list of students currently retrieved
let currentStudents = [];

// ============================================================
// PAGE LOAD
// ============================================================

document.addEventListener("DOMContentLoaded", function () {
    loadStudents();
    bindModalClose();
});


// ============================================================
// LOAD ALL STUDENTS
// ============================================================

async function loadStudents() {

    showTableLoading();

    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Server error: " + response.status);
        }

        const students = await response.json();
        currentStudents = Array.isArray(students) ? students : [];
        displayStudents(currentStudents);

    } catch (error) {
        console.error("Load error:", error);
        showTableError("Unable to connect to server.");
        updateRecordCount(0);
        updateStats([]);
    }
}


// ============================================================
// DISPLAY STUDENTS
// ============================================================

function displayStudents(students) {

    const tableBody = document.getElementById("studentTableBody");
    currentStudents = Array.isArray(students) ? students : [];

    tableBody.innerHTML = "";
    updateRecordCount(currentStudents.length);

    if (currentStudents.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5">
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <p>No student records found.<br>Add your first student to get started.</p>
                    </div>
                </td>
            </tr>
        `;
        updateStats(currentStudents);
        return;
    }

    currentStudents.forEach(function (student) {
        const row = document.createElement("tr");
        const marksClass = student.marks >= 85
            ? "high"
            : student.marks >= 60
                ? "medium"
                : "low";

        const nameVal = student.name != null ? student.name : "";
        const branchVal = student.branch != null ? student.branch : "";

        row.innerHTML = `
            <td>
                <span class="roll-badge">#${student.rollNumber}</span>
            </td>
            <td>${escapeHtml(nameVal)}</td>
            <td>
                <span class="branch-badge">${escapeHtml(branchVal)}</span>
            </td>
            <td>
                <div class="marks-bar-wrap">
                    <div class="marks-bar">
                        <div class="marks-bar-fill ${marksClass}"
                             style="width: ${Math.min(student.marks, 100)}%"></div>
                    </div>
                    <span class="marks-text">${student.marks}</span>
                </div>
            </td>
            <td>
                <div class="action-cell">
                    <button
                        class="btn btn-success btn-sm"
                        onclick="openEditModal(${student.rollNumber})"
                        title="Edit student">
                        ✏️ Edit
                    </button>
                    <button
                        class="btn btn-danger btn-sm"
                        onclick="openDeleteConfirm(${student.rollNumber})"
                        title="Delete student">
                        🗑️ Delete
                    </button>
                </div>
            </td>
        `;

        tableBody.appendChild(row);
    });

    updateStats(currentStudents);
}


// ============================================================
// STATS UPDATE
// ============================================================

function updateStats(students) {

    const totalEl   = document.getElementById("statTotal");
    const avgEl     = document.getElementById("statAvg");
    const topperEl  = document.getElementById("statTopper");

    if (!students || students.length === 0) {
        if (totalEl)  totalEl.textContent  = "0";
        if (avgEl)    avgEl.textContent    = "—";
        if (topperEl) topperEl.textContent = "—";
        return;
    }

    const total = students.length;
    const avg   = (
        students.reduce(function (sum, s) { return sum + (Number(s.marks) || 0); }, 0) / total
    ).toFixed(1);
    const top   = students.reduce(function (a, b) {
        return (a.marks > b.marks) ? a : b;
    });

    if (totalEl)  totalEl.textContent  = total;
    if (avgEl)    avgEl.textContent    = avg + "%";
    if (topperEl) topperEl.textContent = top.name;
}


// ============================================================
// RECORD COUNT
// ============================================================

function updateRecordCount(count) {
    const el = document.getElementById("recordCount");
    if (el) {
        el.textContent = count + " student" + (count !== 1 ? "s" : "");
    }
}


// ============================================================
// OPEN ADD MODAL
// ============================================================

function openAddModal() {

    const form = document.getElementById("studentForm");
    form.reset();

    document.getElementById("modalTitle").textContent = "➕ Add New Student";
    document.getElementById("submitBtn").textContent  = "Add Student";

    openModal("studentModal");
}


// ============================================================
// OPEN EDIT MODAL
// ============================================================

async function openEditModal(rollNumber) {

    try {
        const response = await fetch(
            API_URL + "?rollNumber=" + encodeURIComponent(rollNumber)
        );

        if (!response.ok) {
            throw new Error("Student not found");
        }

        const data    = await response.json();
        const student = Array.isArray(data) ? data[0] : data;

        if (!student) {
            showToast("Student not found.", "error");
            return;
        }

        const form = document.getElementById("editStudentForm");
        form.reset();

        document.getElementById("editRollNumber").value = student.rollNumber != null ? student.rollNumber : "";
        document.getElementById("editName").value       = student.name != null ? student.name : "";
        document.getElementById("editBranch").value     = student.branch != null ? student.branch : "";
        document.getElementById("editMarks").value      = student.marks != null ? student.marks : "";

        openModal("editStudentModal");

    } catch (error) {
        console.error("Edit load error:", error);
        showToast("Unable to load student data.", "error");
    }
}


// ============================================================
// FORM SUBMIT (ADD)
// ============================================================

document
    .getElementById("studentForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const rollNumber = Number(document.getElementById("rollNumber").value);
        const name   = document.getElementById("name").value.trim();
        const branch = document.getElementById("branch").value.trim();
        const marks  = Number(document.getElementById("marks").value);

        if (!rollNumber || rollNumber <= 0) {
            showToast("Roll number must be a positive number.", "error");
            return;
        }
        if (!name || name.indexOf("|") !== -1) {
            showToast("Invalid name.", "error");
            return;
        }
        if (!branch || branch.indexOf("|") !== -1) {
            showToast("Invalid branch.", "error");
            return;
        }
        if (isNaN(marks) || marks < 0 || marks > 100) {
            showToast("Marks must be between 0 and 100.", "error");
            return;
        }

        const student = { rollNumber, name, branch, marks };

        const submitBtn = document.getElementById("submitBtn");
        submitBtn.disabled = true;
        submitBtn.textContent = "Adding…";

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(student)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Operation failed");
            }

            showToast("Student added successfully! 🎉", "success");
            closeModal("studentModal");
            loadStudents();

        } catch (error) {
            console.error("Add error:", error);
            showToast(error.message || "Unable to save student.", "error");
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Add Student";
        }
    });

// ============================================================
// FORM SUBMIT (UPDATE)
// ============================================================

document
    .getElementById("editStudentForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const rollNumber = Number(document.getElementById("editRollNumber").value);
        const name   = document.getElementById("editName").value.trim();
        const branch = document.getElementById("editBranch").value.trim();
        const marks  = Number(document.getElementById("editMarks").value);

        if (!rollNumber || rollNumber <= 0) {
            showToast("Roll number is invalid.", "error");
            return;
        }
        if (!name || name.indexOf("|") !== -1) {
            showToast("Invalid name.", "error");
            return;
        }
        if (!branch || branch.indexOf("|") !== -1) {
            showToast("Invalid branch.", "error");
            return;
        }
        if (isNaN(marks) || marks < 0 || marks > 100) {
            showToast("Marks must be between 0 and 100.", "error");
            return;
        }

        const student = { rollNumber, name, branch, marks };

        const submitBtn = document.getElementById("editSubmitBtn");
        submitBtn.disabled = true;
        submitBtn.textContent = "Updating…";

        try {
            const response = await fetch(
                API_URL + "?rollNumber=" + encodeURIComponent(rollNumber),
                {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(student)
                }
            );

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Operation failed");
            }

            showToast("Student updated successfully! ✅", "success");
            closeModal("editStudentModal");
            loadStudents();

        } catch (error) {
            console.error("Update error:", error);
            showToast(error.message || "Unable to update student.", "error");
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Update Student";
        }
    });


// ============================================================
// SEARCH
// ============================================================

async function searchStudent() {

    const rollNumber = document
        .getElementById("searchRoll")
        .value
        .trim();

    if (rollNumber === "") {
        showToast("Please enter a roll number to search.", "info");
        return;
    }

    showTableLoading();

    try {
        const response = await fetch(
            API_URL + "?rollNumber=" + encodeURIComponent(rollNumber)
        );

        if (!response.ok) {
            throw new Error("Student not found");
        }

        const data = await response.json();
        const students = Array.isArray(data) ? data : [data];

        displayStudents(students);

    } catch (error) {
        console.error("Search error:", error);
        updateRecordCount(0);
        updateStats([]);

        const tableBody = document.getElementById("studentTableBody");
        tableBody.innerHTML = `
            <tr>
                <td colspan="5">
                    <div class="empty-state">
                        <div class="empty-state-icon">🔍</div>
                        <p>No student found with roll number <strong>${escapeHtml(rollNumber)}</strong>.</p>
                    </div>
                </td>
            </tr>
        `;
    }
}

// Search on Enter key
const searchInput = document.getElementById("searchRoll");
if (searchInput) {
    searchInput.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            searchStudent();
        }
    });
}


// ============================================================
// DELETE CONFIRM MODAL
// ============================================================

let pendingDeleteRoll = null;

function openDeleteConfirm(rollNumber) {
    pendingDeleteRoll = rollNumber;

    const student = currentStudents.find(function (s) {
        return Number(s.rollNumber) === Number(rollNumber);
    });

    const displayName = student && student.name ? student.name : ("#" + rollNumber);
    const nameEl = document.getElementById("deleteStudentName");
    if (nameEl) {
        nameEl.textContent = displayName + " (Roll #" + rollNumber + ")";
    }

    openModal("deleteModal");
}

async function confirmDelete() {

    if (!pendingDeleteRoll) return;

    const confirmBtn = document.getElementById("confirmDeleteBtn");
    confirmBtn.disabled = true;
    confirmBtn.textContent = "Deleting…";

    try {
        const response = await fetch(
            API_URL + "?rollNumber=" + encodeURIComponent(pendingDeleteRoll),
            { method: "DELETE" }
        );

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Delete failed");
        }

        showToast("Student deleted successfully. 🗑️", "success");
        closeModal("deleteModal");
        loadStudents();

    } catch (error) {
        console.error("Delete error:", error);
        showToast(error.message || "Unable to delete student.", "error");
    } finally {
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.textContent = "Delete";
        }
        pendingDeleteRoll = null;
    }
}


// ============================================================
// CLEAR FORM
// ============================================================

function clearForm() {
    const form = document.getElementById("studentForm");
    if (form) form.reset();
    
    const editForm = document.getElementById("editStudentForm");
    if (editForm) editForm.reset();
}


// ============================================================
// MODAL HELPERS
// ============================================================

function openModal(id) {
    const overlay = document.getElementById(id);
    if (overlay) {
        overlay.classList.add("open");
        document.body.style.overflow = "hidden";
    }
}

function closeModal(id) {
    const overlay = document.getElementById(id);
    if (overlay) {
        overlay.classList.remove("open");
        document.body.style.overflow = "";
    }
    if (id === "studentModal" || id === "editStudentModal") {
        clearForm();
    }
}

function bindModalClose() {
    // Close on overlay backdrop click
    document.querySelectorAll(".modal-overlay").forEach(function (overlay) {
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) {
                closeModal(overlay.id);
            }
        });
    });

    // Close on Escape key
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            document.querySelectorAll(".modal-overlay.open")
                .forEach(function (el) { closeModal(el.id); });
        }
    });
}


// ============================================================
// TOAST NOTIFICATIONS
// ============================================================

function showToast(message, type) {

    const container = document.getElementById("toast-container");
    if (!container) return;

    const icons = { success: "✅", error: "❌", info: "ℹ️" };
    const toast  = document.createElement("div");

    toast.className = "toast toast-" + (type || "info");
    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || "ℹ️"}</span>
        <span class="toast-msg">${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);

    setTimeout(function () {
        toast.classList.add("hiding");
        setTimeout(function () { toast.remove(); }, 300);
    }, 3500);
}


// ============================================================
// TABLE STATE HELPERS
// ============================================================

function showTableLoading() {
    const tableBody = document.getElementById("studentTableBody");
    tableBody.innerHTML = `
        <tr class="loading-row">
            <td colspan="5">
                <span class="spinner"></span>
                Loading students…
            </td>
        </tr>
    `;
}

function showTableError(message) {
    const tableBody = document.getElementById("studentTableBody");
    tableBody.innerHTML = `
        <tr>
            <td colspan="5">
                <div class="empty-state">
                    <div class="empty-state-icon">⚠️</div>
                    <p>${escapeHtml(message)}</p>
                </div>
            </td>
        </tr>
    `;
}


// ============================================================
// XSS PREVENTION
// ============================================================

function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
        .replace(/&/g,  "&amp;")
        .replace(/</g,  "&lt;")
        .replace(/>/g,  "&gt;")
        .replace(/"/g,  "&quot;")
        .replace(/'/g,  "&#39;");
}