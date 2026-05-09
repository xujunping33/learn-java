import { loadStudents } from "../modules/storage.js";
import { statsScores } from "../modules/utils.js";

export function initHomePage() {
  const run = () => {
    const countEl = document.getElementById("stat-count");
    const avgEl = document.getElementById("stat-avg");
    if (!countEl || !avgEl) {
      return;
    }
    const students = loadStudents();
    const scores = students.map((s) => Number(s.score)).filter((n) => !Number.isNaN(n));
    const st = statsScores(scores);
    countEl.textContent = String(students.length);
    avgEl.textContent = st.avg === null ? "—" : st.avg.toFixed(1);
  };

  setTimeout(run, 0);
}
