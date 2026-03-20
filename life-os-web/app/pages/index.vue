<script setup lang="ts">
/**
 * 页面元数据配置
 * @middleware auth 仅认证用户可访问
 */
definePageMeta({
  middleware: 'auth'
})

/**
 * 首页组件
 * <p>
 * 功能：应用首页，展示用户概览信息
 * 说明：包含欢迎区域和数据统计卡片
 * 应用场景：用户登录后看到的第一个页面
 */
</script>

<template>
  <!-- 页面容器 -->
  <div class="page-wrap">
    <!-- 欢迎区域 -->
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

    <!-- 数据卡片网格 -->
    <div class="grid-cards">
      <!-- 任务数据卡片 -->
      <div class="data-card">
        <div class="card-title">
          任务
        </div>
        <div class="card-value">
          12
        </div>
      </div>

      <!-- 日程数据卡片 -->
      <div class="data-card">
        <div class="card-title">
          日程
        </div>
        <div class="card-value">
          3
        </div>
      </div>

      <!-- 会话数据卡片 -->
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
/**
 * 页面容器样式
 * 说明：设置页面的基本布局
 * - display: flex 设置为 Flex 容器
 * - flex-direction: column 垂直排列子元素
 * - gap: 18px 设置子元素间距
 */
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 18px;
  
}

/**
 * 欢迎卡片样式
 * 说明：设置欢迎区域的样式
 * - min-height: 180px 最小高度
 * - border-radius: 24px 设置圆角
 * - background: linear-gradient 设置渐变背景
 * - border: 1px solid 设置边框
 * - padding: 28px 设置内边距
 * - display: flex 设置为 Flex 容器
 * - align-items: flex-end 垂直对齐到底部
 * - justify-content: space-between 水平两端对齐
 * - gap: 16px 设置子元素间距
 */
.hero-card {
  min-height: 180px;
  border-radius: 24px;
  border: 1px solid rgba(120, 90, 60, 0.08);
  padding: 28px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

/**
 * 欢迎卡片小标题样式
 * 说明：设置欢迎卡片小标题的样式
 * - font-size: 13px 字体大小
 * - color: #9a8370 字体颜色
 * - margin-bottom: 10px 下边距
 */
.hero-kicker {
  font-size: 13px;
  color: #9a8370;
  margin-bottom: 10px;
}

/**
 * 欢迎卡片标题样式
 * 说明：设置欢迎卡片标题的样式
 * - margin: 0 0 8px 外边距
 * - font-size: 30px 字体大小
 * - color: #5f4730 字体颜色
 */
.hero-card h2 {
  margin: 0 0 8px;
  font-size: 30px;
  color: #5f4730;
}

/**
 * 欢迎卡片描述样式
 * 说明：设置欢迎卡片描述的样式
 * - margin: 0 外边距
 * - color: #8b7462 字体颜色
 */
.hero-card p {
  margin: 0;
  color: #8b7462;
}

/**
 * 数据卡片网格样式
 * 说明：设置数据卡片网格的样式
 * - display: grid 设置为 Grid 容器
 * - grid-template-columns: repeat(3, minmax(0, 1fr)) 三列网格
 * - gap: 18px 设置网格间距
 */
.grid-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

/**
 * 数据卡片样式
 * 说明：设置数据卡片的样式
 * - min-height: 160px 最小高度
 * - border-radius: 22px 设置圆角
 * - background: rgba(255, 255, 255, 0.84) 设置背景颜色
 * - border: 1px solid 设置边框
 * - box-shadow: 0 10px 24px rgba(95, 71, 47, 0.04) 设置阴影
 * - padding: 22px 设置内边距
 */
.data-card {
  min-height: 160px;
  border-radius: 22px;
  border: 1px solid rgba(120, 90, 60, 0.08);
  box-shadow: 0 10px 24px rgba(95, 71, 47, 0.04);
  padding: 22px;
}

/**
 * 卡片标题样式
 * 说明：设置卡片标题的样式
 * - color: #9a8370 字体颜色
 * - font-size: 14px 字体大小
 */
.card-title {
  color: #9a8370;
  font-size: 14px;
}

/**
 * 卡片数值样式
 * 说明：设置卡片数值的样式
 * - margin-top: 22px 上边距
 * - font-size: 36px 字体大小
 * - font-weight: 700 字体粗细
 * - color: #5f4730 字体颜色
 */
.card-value {
  margin-top: 22px;
  font-size: 36px;
  font-weight: 700;
  color: #5f4730;
}
</style>
