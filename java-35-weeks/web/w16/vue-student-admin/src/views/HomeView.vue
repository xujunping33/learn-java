<script setup>
import StatCards from '../components/StatCards.vue'

defineProps({
  students: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section class="home-view" aria-labelledby="home-heading">
    <h2 id="home-heading" class="home-view__title">首页</h2>
    <p class="home-view__lead">
      当前示例数据下的班级概览。增删改请到<strong>学生管理</strong>页。
    </p>
    <StatCards :students="students" />
  </section>
</template>

<style scoped>
.home-view__title {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--heading, #0f172a);
}

.home-view__lead {
  margin: 0 0 1.25rem;
  color: var(--muted, #64748b);
  font-size: 0.95rem;
}

.home-view__lead strong {
  color: var(--heading, #334155);
  font-weight: 600;
}
</style>
