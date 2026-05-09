<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import Students from './components/Students.vue'
import HomeView from './views/HomeView.vue'

const PROJECT_TITLE = 'Vue 学生管理 · W16'

const activePage = ref('home')

const students = ref([
  { id: 1, name: '张三', score: 88, age: 18 },
  { id: 2, name: '李四', score: 92, age: 19 },
  { id: 3, name: '王五', score: 76, age: 18 },
])

function nextStudentId(list) {
  if (!list.length) return 1
  return Math.max(...list.map((s) => Number(s.id))) + 1
}

function addStudent(payload) {
  const row = {
    id: nextStudentId(students.value),
    name: payload.name,
    score: payload.score,
    age: payload.age,
  }
  students.value = [...students.value, row]
}

function removeById(id) {
  const target = Number(id)
  students.value = students.value.filter((s) => s.id !== target)
}

const currentTime = ref(formatNow())

function formatNow() {
  return new Date().toLocaleString('zh-CN', {
    hour12: false,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

let timerId

onMounted(() => {
  timerId = setInterval(() => {
    currentTime.value = formatNow()
  }, 1000)
})

onUnmounted(() => {
  clearInterval(timerId)
})
</script>

<template>
  <div class="app">
    <AppHeader
      :active-page="activePage"
      :title="PROJECT_TITLE"
      subtitle="Day 112 · 仿站 + Vue 整合演示"
      :current-time="currentTime"
      @navigate="activePage = $event"
    />
    <main class="app__main">
      <HomeView v-if="activePage === 'home'" :students="students" />
      <Students
        v-else
        :students="students"
        @add="addStudent"
        @delete="removeById"
      />
    </main>
  </div>
</template>

<style scoped>
.app {
  max-width: 56rem;
  margin: 0 auto;
  padding: 1.75rem 1.25rem 2.5rem;
  font-family: system-ui, sans-serif;
}

.app__main {
  text-align: left;
}
</style>
