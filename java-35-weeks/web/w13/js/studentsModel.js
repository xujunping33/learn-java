// Day 101：数据层 ES6（解构、默认参数、展开）。依赖全局 loadStudents / saveStudents / nextStudentId。

const mergeStudent = (base, patch = {}) => ({ ...base, ...patch });

const normalizeStudentShape = (s) => {
  const merged = mergeStudent({ id: 0, name: "", score: 0, age: 0 }, s);
  const { id, name, score, age } = merged;
  return {
    id: Number(id),
    name: name != null ? String(name) : "",
    score: Number(score),
    age: Number(age),
  };
};

const listStudents = () => [...loadStudents()];

const addStudent = (students, patch = {}) => {
  const id = nextStudentId(students);
  const row = normalizeStudentShape(mergeStudent({ id, name: "", score: 0, age: 0 }, patch));
  return [...students, row];
};

const removeStudentById = (students, id) => {
  const target = Number(id);
  return students.filter((s) => s.id !== target);
};

const updateStudentById = (students, id, patch = {}) => {
  const target = Number(id);
  return students.map((s) => (s.id === target ? normalizeStudentShape(mergeStudent(s, patch)) : s));
};

const sortStudentsByScore = (students, desc = false) =>
  [...students].sort((a, b) => {
    const sa = Number(a.score);
    const sb = Number(b.score);
    const na = Number.isNaN(sa) ? 0 : sa;
    const nb = Number.isNaN(sb) ? 0 : sb;
    return desc ? nb - na : na - nb;
  });
