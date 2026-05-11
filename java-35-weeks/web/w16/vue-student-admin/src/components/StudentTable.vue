<script setup>
const props = defineProps({
  students: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(['delete'])

function onDeleteClick(id) {
  emit('delete', id)
}
</script>

<template>
  <div class="table-wrap">
    <table v-if="props.students.length" class="student-table">
      <thead>
        <tr>
          <th scope="col">ID</th>
          <th scope="col">姓名</th>
          <th scope="col">分数</th>
          <th scope="col">年龄</th>
          <th scope="col"><span class="sr-only">操作</span></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in props.students" :key="row.id">
          <td>{{ row.id }}</td>
          <td>{{ row.name }}</td>
          <td>{{ row.score }}</td>
          <td>{{ row.age }}</td>
          <td>
            <button
              type="button"
              class="student-table__del"
              @click="onDeleteClick(row.id)"
            >
              删除
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="student-table__empty">暂无学生</p>
  </div>
</template>

<style scoped>
.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 10px;
}

.student-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}

.student-table th,
.student-table td {
  padding: 0.65rem 0.85rem;
  text-align: left;
  border-bottom: 1px solid var(--border, #e5e7eb);
}

.student-table th {
  font-weight: 600;
  background: var(--surface, #f9fafb);
  color: var(--muted, #475569);
}

.student-table tbody tr:last-child td {
  border-bottom: none;
}

.student-table__del {
  font: inherit;
  font-size: 0.85rem;
  padding: 0.35rem 0.65rem;
  border-radius: 6px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  cursor: pointer;
}

.student-table__del:hover {
  background: #fee2e2;
}

.student-table__empty {
  margin: 0;
  padding: 1.25rem;
  text-align: center;
  color: var(--muted, #64748b);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
