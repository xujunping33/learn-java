const STORAGE_SCHEMA_VERSION = 1;
const STUDENT_STORAGE_KEY = "learn-java-w13-students-v1";

const DEFAULT_STUDENTS = [
  { id: 1, name: "张三", score: 88, age: 18 },
  { id: 2, name: "李四", score: 92, age: 19 },
  { id: 3, name: "王五", score: 76, age: 18 },
  { id: 4, name: "赵六", score: 95, age: 20 },
  { id: 5, name: "钱七", score: 81, age: 19 },
];

const cloneDefaultStudents = () => DEFAULT_STUDENTS.map((s) => ({ ...s }));

function normalizeStudentList(arr) {
  if (!Array.isArray(arr)) {
    return [];
  }
  return arr
    .filter((s) => s && typeof s === "object")
    .map((s) => ({
      id: Number(s.id),
      name: s.name != null ? String(s.name) : "",
      score: Number(s.score),
      age: Number(s.age),
    }))
    .filter((s) => !Number.isNaN(s.id));
}

function parseStoredJson(raw) {
  const data = JSON.parse(raw);
  if (Array.isArray(data)) {
    return {
      version: STORAGE_SCHEMA_VERSION,
      students: normalizeStudentList(data),
      legacyPlainArray: true,
    };
  }
  if (data && typeof data === "object" && Array.isArray(data.students)) {
    const v = Number(data.version);
    const version = Number.isNaN(v) ? STORAGE_SCHEMA_VERSION : v;
    return {
      version,
      students: normalizeStudentList(data.students),
      legacyPlainArray: false,
    };
  }
  return null;
}

export function saveStudents(list) {
  try {
    const payload = {
      version: STORAGE_SCHEMA_VERSION,
      students: Array.isArray(list) ? list : [],
    };
    localStorage.setItem(STUDENT_STORAGE_KEY, JSON.stringify(payload));
  } catch (e) {
    console.warn("saveStudents failed", e);
  }
}

export function loadStudents() {
  try {
    const raw = localStorage.getItem(STUDENT_STORAGE_KEY);
    if (!raw) {
      return cloneDefaultStudents();
    }
    const parsed = parseStoredJson(raw);
    if (!parsed) {
      return cloneDefaultStudents();
    }
    if (parsed.legacyPlainArray) {
      saveStudents(parsed.students);
    }
    return parsed.students;
  } catch {
    return cloneDefaultStudents();
  }
}

export function nextStudentId(list) {
  if (!Array.isArray(list) || list.length === 0) {
    return 1;
  }
  const maxId = list.reduce((m, s) => {
    const id = Number(s && s.id);
    return Number.isNaN(id) ? m : Math.max(m, id);
  }, 0);
  return maxId + 1;
}
