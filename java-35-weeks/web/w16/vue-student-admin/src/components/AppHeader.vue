<script setup>
defineProps({
  activePage: {
    type: String,
    required: true,
    validator: (v) => v === 'home' || v === 'students',
  },
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: '',
  },
  currentTime: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['navigate'])

function go(page) {
  emit('navigate', page)
}
</script>

<template>
  <header class="app-header">
    <div class="app-header__top">
      <div class="app-header__brand">
        <h1 class="app-header__title">{{ title }}</h1>
        <p v-if="subtitle" class="app-header__subtitle">{{ subtitle }}</p>
      </div>
      <p class="app-header__time" aria-live="polite">当前时间：{{ currentTime }}</p>
    </div>
    <nav class="app-header__nav" aria-label="站内导航">
      <button
        type="button"
        class="app-header__link"
        :class="{ 'app-header__link--active': activePage === 'home' }"
        :aria-current="activePage === 'home' ? 'page' : undefined"
        @click="go('home')"
      >
        首页
      </button>
      <button
        type="button"
        class="app-header__link"
        :class="{ 'app-header__link--active': activePage === 'students' }"
        :aria-current="activePage === 'students' ? 'page' : undefined"
        @click="go('students')"
      >
        学生管理
      </button>
    </nav>
  </header>
</template>

<style scoped>
.app-header {
  margin-bottom: 1.75rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border, #e5e7eb);
}

.app-header__top {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.app-header__title {
  margin: 0 0 0.25rem;
  font-size: 1.6rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--heading, #0f172a);
}

.app-header__subtitle {
  margin: 0;
  color: var(--muted, #64748b);
  font-size: 0.9rem;
}

.app-header__time {
  margin: 0;
  font-variant-numeric: tabular-nums;
  font-size: 0.95rem;
  color: var(--muted, #475569);
}

.app-header__nav {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.app-header__link {
  font: inherit;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 0.45rem 0.9rem;
  border: 1px solid var(--border, #cbd5e1);
  border-radius: 8px;
  background: var(--bg, #fff);
  color: var(--heading, #0f172a);
  cursor: pointer;
}

.app-header__link:hover {
  border-color: #818cf8;
  color: #4338ca;
}

.app-header__link--active {
  border-color: #4f46e5;
  background: #eef2ff;
  color: #3730a3;
}
</style>
