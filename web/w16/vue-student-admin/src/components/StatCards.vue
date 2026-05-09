<script setup>
import { computed } from 'vue'

const props = defineProps({
  students: {
    type: Array,
    required: true,
  },
})

const totalCount = computed(() => props.students.length)

const averageScore = computed(() => {
  const list = props.students
  if (!list.length) return null
  let sum = 0
  for (const row of list) {
    const n = Number(row?.score)
    sum += Number.isFinite(n) ? n : 0
  }
  return Math.round((sum / list.length) * 10) / 10
})
</script>

<template>
  <div class="stat-cards" aria-label="统计概览">
    <article class="stat-cards__item">
      <p class="stat-cards__label">人数</p>
      <p class="stat-cards__value">{{ totalCount }}</p>
    </article>
    <article class="stat-cards__item">
      <p class="stat-cards__label">平均分</p>
      <p class="stat-cards__value">
        {{ averageScore == null ? '—' : averageScore }}
      </p>
    </article>
  </div>
</template>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(9rem, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-cards__item {
  margin: 0;
  padding: 1rem 1.1rem;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 10px;
  background: var(--surface, #f9fafb);
}

.stat-cards__label {
  margin: 0 0 0.35rem;
  font-size: 0.85rem;
  color: var(--muted, #64748b);
}

.stat-cards__value {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--heading, #0f172a);
}
</style>
