<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})
</script>
<template>
  <div class="page-wrap">
    <div class="hero-card">
      <div>
        <div class="hero-kicker">
          今日概览
        </div>
        <h2>欢迎回来，开始整理今天的生活吧</h2>
        <p>这里后面放仪表盘数据、快捷入口和统计概览。</p>
      </div>

      <UButton size="lg">
        新建内容
      </UButton>
    </div>

    <div class="grid-cards">
      <div class="data-card">
        <div class="card-title">
          任务
        </div>
        <div class="card-value">
          12
        </div>
      </div>

      <div class="data-card">
        <div class="card-title">
          日程
        </div>
        <div class="card-value">
          3
        </div>
      </div>

      <div class="data-card">
        <div class="card-title">
          会话
        </div>
        <div class="card-value">
          2
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-card {
  min-height: 180px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(184, 157, 135, 0.18), rgba(255, 255, 255, 0.72));
  border: 1px solid rgba(120, 90, 60, 0.08);
  padding: 28px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.hero-kicker {
  font-size: 13px;
  color: #9a8370;
  margin-bottom: 10px;
}

.hero-card h2 {
  margin: 0 0 8px;
  font-size: 30px;
  color: #5f4730;
}

.hero-card p {
  margin: 0;
  color: #8b7462;
}

.grid-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.data-card {
  min-height: 160px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(120, 90, 60, 0.08);
  box-shadow: 0 10px 24px rgba(95, 71, 47, 0.04);
  padding: 22px;
}

.card-title {
  color: #9a8370;
  font-size: 14px;
}

.card-value {
  margin-top: 22px;
  font-size: 36px;
  font-weight: 700;
  color: #5f4730;
}
</style>
