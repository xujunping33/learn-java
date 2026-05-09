import { filterStudents, isNonEmptyString } from "../modules/utils.js";
import { saveStudents } from "../modules/storage.js";
import {
  listStudents,
  addStudent,
  removeStudentById,
  sortStudentsByScore,
} from "../modules/studentsModel.js";

export function initStudentsPage() {
  let students = listStudents();

  const getTbody = () => document.getElementById("student-tbody");

  const applySearch = () => {
    const input = document.getElementById("student-search");
    const kw = input ? input.value : "";
    const filtered = filterStudents(students, kw);
    const ids = new Set(filtered.map((s) => s.id));
    const tb = getTbody();
    if (!tb) {
      return;
    }
    tb.querySelectorAll("tr").forEach((tr) => {
      const id = Number(tr.dataset.studentId);
      tr.hidden = !ids.has(id);
    });
  };

  const deleteStudent = (id) => {
    const next = removeStudentById(students, id);
    if (next.length === students.length) {
      return;
    }
    students = next;
    saveStudents(students);
    renderTable();
    applySearch();
  };

  const quickAddStudent = () => {
    const nameEl = document.getElementById("quick-name");
    const scoreEl = document.getElementById("quick-score");
    const ageEl = document.getElementById("quick-age");
    const name = nameEl ? nameEl.value : "";
    if (!isNonEmptyString(name)) {
      window.alert("请填写姓名。");
      return;
    }
    const scoreRaw = scoreEl ? scoreEl.value : "";
    if (scoreRaw === "" || String(scoreRaw).trim() === "") {
      window.alert("请填写成绩（0～100）。");
      return;
    }
    const scoreNum = Number(scoreRaw);
    if (Number.isNaN(scoreNum) || scoreNum < 0 || scoreNum > 100) {
      window.alert("成绩须在 0～100 之间。");
      return;
    }
    const ageRaw = ageEl ? ageEl.value : "";
    if (ageRaw === "" || String(ageRaw).trim() === "") {
      window.alert("请填写年龄（1～120）。");
      return;
    }
    const ageNum = Number(ageRaw);
    if (Number.isNaN(ageNum) || ageNum < 1 || ageNum > 120) {
      window.alert("年龄须在 1～120 之间。");
      return;
    }
    students = addStudent(students, {
      name: name.trim(),
      score: scoreNum,
      age: ageNum,
    });
    saveStudents(students);
    renderTable();
    applySearch();
    if (nameEl) {
      nameEl.value = "";
    }
    if (scoreEl) {
      scoreEl.value = "";
    }
    if (ageEl) {
      ageEl.value = "";
    }
  };

  const sortByScore = (desc) => {
    students = sortStudentsByScore(students, desc);
    saveStudents(students);
    renderTable();
    applySearch();
  };

  const renderTable = () => {
    const tb = getTbody();
    if (!tb) {
      return;
    }
    tb.replaceChildren();
    for (const s of students) {
      const tr = document.createElement("tr");
      tr.dataset.studentId = String(s.id);

      [s.id, s.name, s.score, s.age].forEach((val) => {
        const td = document.createElement("td");
        td.textContent = String(val);
        tr.appendChild(td);
      });

      const tdOp = document.createElement("td");
      const btnEdit = document.createElement("button");
      btnEdit.type = "button";
      btnEdit.textContent = "编辑";
      const btnDel = document.createElement("button");
      btnDel.type = "button";
      btnDel.textContent = "删除";
      btnDel.dataset.action = "delete";
      btnDel.dataset.studentId = String(s.id);
      tdOp.appendChild(btnEdit);
      tdOp.appendChild(document.createTextNode(" "));
      tdOp.appendChild(btnDel);
      tr.appendChild(tdOp);

      tb.appendChild(tr);
    }
  };

  students = listStudents();
  renderTable();
  applySearch();

  const searchBtn = document.getElementById("student-search-btn");
  if (searchBtn) {
    searchBtn.addEventListener("click", applySearch);
  }

  const asc = document.getElementById("sort-score-asc");
  const desc = document.getElementById("sort-score-desc");
  if (asc) {
    asc.addEventListener("click", () => sortByScore(false));
  }
  if (desc) {
    desc.addEventListener("click", () => sortByScore(true));
  }

  const tb = getTbody();
  if (tb) {
    tb.addEventListener("click", (e) => {
      const del = e.target.closest("button[data-action='delete']");
      if (del && del.matches("button[data-action='delete']")) {
        deleteStudent(Number(del.dataset.studentId));
      }
    });
  }

  const quickBtn = document.getElementById("quick-add-btn");
  if (quickBtn) {
    quickBtn.addEventListener("click", quickAddStudent);
  }
}
