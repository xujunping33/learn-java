function clampScore(n) {
  const x = Number(n);
  if (Number.isNaN(x)) {
    return NaN;
  }
  return Math.min(100, Math.max(0, x));
}

function isNonEmptyString(s) {
  return typeof s === "string" && s.trim().length > 0;
}

const filterStudents = (students, keyword) => {
  if (!Array.isArray(students)) {
    return [];
  }
  const kw = typeof keyword === "string" ? keyword.trim() : "";
  if (kw === "") {
    return students.slice();
  }
  const lower = kw.toLowerCase();
  return students.filter((s) => {
    if (!s || typeof s !== "object") {
      return false;
    }
    const name = s.name != null ? String(s.name) : "";
    return name.toLowerCase().includes(lower);
  });
};

const statsScores = (scores) => {
  if (!Array.isArray(scores) || scores.length === 0) {
    return { min: null, max: null, avg: null };
  }
  const nums = scores.map((x) => Number(x)).filter((n) => !Number.isNaN(n));
  if (nums.length === 0) {
    return { min: null, max: null, avg: null };
  }
  return {
    min: Math.min(...nums),
    max: Math.max(...nums),
    avg: nums.reduce((sum, n) => sum + n, 0) / nums.length,
  };
};
