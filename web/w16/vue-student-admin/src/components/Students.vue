<script setup>
import StudentForm from './StudentForm.vue'
import StudentTable from './StudentTable.vue'

defineProps({
  students: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(['add', 'delete'])
</script>

<template>
  <section class="students" aria-labelledby="students-heading">
    <h2 id="students-heading" class="students__title">学生管理</h2>
    <StudentForm @add="emit('add', $event)" />
    <StudentTable :students="students" @delete="emit('delete', $event)" />
  </section>
</template>

<style scoped>
.students__title {
  margin: 0 0 1rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--heading, #0f172a);
}
</style>
