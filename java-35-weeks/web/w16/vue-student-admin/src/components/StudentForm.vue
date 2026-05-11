<script setup>
import { ref, reactive } from 'vue'

const emit = defineEmits(['add'])

const name = ref('')
const score = ref('')
const age = ref('')

const errors = reactive({
  name: '',
  score: '',
  age: '',
})

const AGE_MIN = 6
const AGE_MAX = 99
const SCORE_MIN = 0
const SCORE_MAX = 100

function clearErrors() {
  errors.name = ''
  errors.score = ''
  errors.age = ''
}

function validate() {
  clearErrors()
  let ok = true

  const rawName = name.value.trim()
  if (!rawName) {
    errors.name = '姓名不能为空'
    ok = false
  }

  const scoreNum = Number(score.value)
  if (score.value === '' || Number.isNaN(scoreNum)) {
    errors.score = '请输入有效的分数（数字）'
    ok = false
  } else if (scoreNum < SCORE_MIN || scoreNum > SCORE_MAX) {
    errors.score = `分数须在 ${SCORE_MIN}～${SCORE_MAX} 之间`
    ok = false
  }

  const ageNum = Number(age.value)
  if (age.value === '' || Number.isNaN(ageNum) || !Number.isInteger(ageNum)) {
    errors.age = '请输入有效的年龄（整数）'
    ok = false
  } else if (ageNum < AGE_MIN || ageNum > AGE_MAX) {
    errors.age = `年龄须在 ${AGE_MIN}～${AGE_MAX} 岁之间`
    ok = false
  }

  return ok
}

function onSubmit(event) {
  event.preventDefault()
  if (!validate()) return

  emit('add', {
    name: name.value.trim(),
    score: Number(score.value),
    age: Number(age.value),
  })

  name.value = ''
  score.value = ''
  age.value = ''
  clearErrors()
}
</script>

<template>
  <form class="student-form" novalidate @submit="onSubmit">
    <h3 class="student-form__title">新增学生</h3>
    <div class="student-form__row">
      <label class="student-form__label" for="sf-name">姓名</label>
      <input
        id="sf-name"
        v-model.trim="name"
        class="student-form__input"
        type="text"
        name="name"
        autocomplete="name"
        :aria-invalid="errors.name ? true : false"
        :aria-describedby="errors.name ? 'sf-err-name' : undefined"
      />
      <p v-if="errors.name" id="sf-err-name" class="student-form__error" role="alert">
        {{ errors.name }}
      </p>
    </div>
    <div class="student-form__row">
      <label class="student-form__label" for="sf-score">分数</label>
      <input
        id="sf-score"
        v-model="score"
        class="student-form__input"
        type="number"
        name="score"
        min="0"
        max="100"
        step="1"
        inputmode="numeric"
        :aria-invalid="errors.score ? true : false"
        :aria-describedby="errors.score ? 'sf-err-score' : undefined"
      />
      <p v-if="errors.score" id="sf-err-score" class="student-form__error" role="alert">
        {{ errors.score }}
      </p>
    </div>
    <div class="student-form__row">
      <label class="student-form__label" for="sf-age">年龄</label>
      <input
        id="sf-age"
        v-model="age"
        class="student-form__input"
        type="number"
        name="age"
        :min="AGE_MIN"
        :max="AGE_MAX"
        step="1"
        inputmode="numeric"
        :aria-invalid="errors.age ? true : false"
        :aria-describedby="errors.age ? 'sf-err-age' : undefined"
      />
      <p v-if="errors.age" id="sf-err-age" class="student-form__error" role="alert">
        {{ errors.age }}
      </p>
    </div>
    <button type="submit" class="student-form__submit">添加</button>
  </form>
</template>

<style scoped>
.student-form {
  margin-bottom: 1.75rem;
  padding: 1.25rem;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 10px;
  background: var(--surface, #f9fafb);
}

.student-form__title {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--heading, #0f172a);
}

.student-form__row {
  margin-bottom: 0.85rem;
}

.student-form__label {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--muted, #475569);
}

.student-form__input {
  box-sizing: border-box;
  width: 100%;
  max-width: 16rem;
  padding: 0.5rem 0.65rem;
  font: inherit;
  border: 1px solid var(--border, #cbd5e1);
  border-radius: 8px;
  background: var(--bg, #fff);
  color: var(--heading, #0f172a);
}

.student-form__input:focus {
  outline: 2px solid #6366f1;
  outline-offset: 1px;
}

.student-form__error {
  margin: 0.35rem 0 0;
  font-size: 0.85rem;
  color: #b91c1c;
}

.student-form__submit {
  margin-top: 0.35rem;
  font: inherit;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 0.5rem 1.1rem;
  border: none;
  border-radius: 8px;
  background: #4f46e5;
  color: #fff;
  cursor: pointer;
}

.student-form__submit:hover {
  background: #4338ca;
}
</style>
