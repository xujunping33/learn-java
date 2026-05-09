import { loadStudents, nextStudentId } from "./storage.js";

export const mergeStudent = (base, patch = {}) => ({ ...base, ...patch });

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

export const listStudents = () => [...loadStudents()];

export const addStudent = (students, patch = {}) => {
  const id = nextStudentId(students);
  const row = normalizeStudentShape(mergeStudent({ id, name: "", score: 0, age: 0 }, patch));
  return [...students, row];
};

export const removeStudentById = (students, id) => {
  const target = Number(id);
  return students.filter((s) => s.id !== target);
};

export const updateStudentById = (students, id, patch = {}) => {
  const target = Number(id);
  return students.map((s) => (s.id === target ? normalizeStudentShape(mergeStudent(s, patch)) : s));
};

export const sortStudentsByScore = (students, desc = false) =>
  [...students].sort((a, b) => {
    const sa = Number(a.score);
    const sb = Number(b.score);
    const na = Number.isNaN(sa) ? 0 : sa;
    const nb = Number.isNaN(sb) ? 0 : sb;
    return desc ? nb - na : na - nb;
  });
