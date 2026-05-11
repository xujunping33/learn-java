import { isNonEmptyString } from "../modules/utils.js";
import { saveStudents } from "../modules/storage.js";
import { listStudents, addStudent } from "../modules/studentsModel.js";

const showFormError = (el, text) => {
  if (!el) {
    return;
  }
  el.textContent = text;
  el.hidden = false;
};

const clearFormError = (el) => {
  if (!el) {
    return;
  }
  el.textContent = "";
  el.hidden = true;
};

const validateStudentForm = () => {
  const nameInput = document.getElementById("student-name");
  const scoreInput = document.getElementById("student-score");
  const ageInput = document.getElementById("student-age");

  const name = nameInput ? nameInput.value : "";
  if (!isNonEmptyString(name)) {
    return { ok: false, message: "姓名不能为空（需为非空文本）。" };
  }

  const scoreRaw = scoreInput ? scoreInput.value : "";
  if (scoreRaw === "" || String(scoreRaw).trim() === "") {
    return { ok: false, message: "请填写成绩。" };
  }
  const scoreNum = Number(scoreRaw);
  if (Number.isNaN(scoreNum)) {
    return { ok: false, message: "成绩必须是有效数字。" };
  }
  if (scoreNum < 0 || scoreNum > 100) {
    return { ok: false, message: "成绩须在 0～100 之间。" };
  }

  const ageRaw = ageInput ? ageInput.value : "";
  if (ageRaw === "" || String(ageRaw).trim() === "") {
    return { ok: false, message: "请填写年龄。" };
  }
  const ageNum = Number(ageRaw);
  if (Number.isNaN(ageNum)) {
    return { ok: false, message: "年龄必须是有效数字。" };
  }
  if (ageNum < 1 || ageNum > 120) {
    return { ok: false, message: "年龄须在 1～120 之间。" };
  }

  return {
    ok: true,
    payload: {
      name: name.trim(),
      score: scoreNum,
      age: ageNum,
    },
  };
};

export function initStudentFormPage() {
  const form = document.getElementById("student-form");
  const err = document.getElementById("form-error");
  if (!form) {
    return;
  }

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    const result = validateStudentForm();
    if (!result.ok) {
      showFormError(err, result.message);
      return;
    }
    clearFormError(err);

    const list = listStudents();
    const next = addStudent(list, result.payload);
    saveStudents(next);

    setTimeout(() => {
      window.alert("已写入本地存储（localStorage）。即将跳转学生列表。");
      window.location.href = "students.html";
    }, 0);
  });

  form.addEventListener("reset", () => {
    clearFormError(err);
  });

  form.addEventListener(
    "input",
    () => {
      if (err && !err.hidden) {
        clearFormError(err);
      }
    },
    true,
  );
}
