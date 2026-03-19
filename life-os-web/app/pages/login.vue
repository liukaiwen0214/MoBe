<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { loginApi, getCaptchaApi } from '~/api/auth'
import { useUserStore } from '~/stores/user'

definePageMeta({
  layout: 'auth',
  middleware: 'guest'
})

const form = reactive({
  account: '',
  password: '',
  captchaCode: '',
  rememberMe: false
})

const captcha = reactive({
  captchaId: '',
  captchaCode: '',
  expiresAt: ''
})

const userStore = useUserStore()
const loading = ref(false)

const errors = reactive({
  account: '',
  password: '',
  captchaCode: ''
})

const touched = reactive({
  account: false
})
const showPassword = ref(false)

async function handleLogin() {
  console.log('login form', form)
  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    const res = await loginApi({
      account: form.account.trim(),
      password: form.password,
      rememberMe: form.rememberMe ? 1 : 0
    })

    if (!res?.data || !res?.data?.sessionToken) {
      throw new Error(res?.data?.message || '登录失败')
    }

    userStore.setToken(res.data.sessionToken)
    await userStore.fetchMe()

    await navigateTo('/')
  } catch (error: any) {
    console.error('登录失败', error)

    userStore.logout()
    await fetchCaptcha()

    const message =
      error?.response?.data?.message ||
      error?.message ||
      '登录失败，请稍后重试'

    alert(message)
  } finally {
    loading.value = false
  }
}
async function fetchCaptcha() {
  try {
    const res = await getCaptchaApi()
    if (res?.data) {
      captcha.captchaId = res.data.captchaId
      captcha.captchaCode = res.data.captchaCode
      captcha.expiresAt = res.data.expiresAt
    }
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}
function validateAccountOnBlur() {
  touched.account = true
  errors.account = form.account.trim() ? '' : '请输入有效的用户名或邮箱'
}

function validateForm() {
  let valid = true

  if (!form.account.trim()) {
    errors.account = '请输入有效的用户名或邮箱'
    valid = false
  } else {
    errors.account = ''
  }

  if (!form.password.trim()) {
    errors.password = '请输入密码'
    valid = false
  } else {
    errors.password = ''
  }

  if (!form.captchaCode.trim()) {
    errors.captchaCode = '请输入验证码'
    valid = false
  } else {
    errors.captchaCode = ''
  }

  return valid
}
onMounted(() => {
  fetchCaptcha()
})
</script>

<template>
  <div class="login-page">
    <div class="auth-card">
      <div class="auth-left">
        <div class="auth-badge">
          MoBe
        </div>
        <h1>欢迎回来</h1>
        <p>让生活、任务、日程和记录都安静地归位。</p>
      </div>

      <div class="auth-right">
        <div class="login-panel">
          <div class="login-head">
            <div class="login-kicker">
              欢迎回来
            </div>
            <h2>登录 MoBe</h2>
            <p>继续管理你的任务、日程与个人节奏</p>
          </div>

          <UForm :state="form" class="login-form" @submit="handleLogin">
            <UFormField name="account" size="xl" class="form-item"
              :error="touched.account && errors.account ? errors.account : undefined" :ui="{
                error: 'text-[12px] mt-1'
              }">
              <UInput v-model="form.account" placeholder="" size="xl" :ui="{ base: 'peer' }" variant="outline"
                class="w-full login-input" @blur="validateAccountOnBlur"
                @input="touched.account && (errors.account = form.account.trim() ? '' : '请输入有效的用户名或邮箱')">
                <label
                  class="pointer-events-none absolute left-0 -top-2.5 text-highlighted text-xs font-medium px-1.5 transition-all peer-focus:-top-2.5 peer-focus:text-highlighted peer-focus:text-xs peer-focus:font-medium peer-placeholder-shown:text-sm peer-placeholder-shown:text-dimmed peer-placeholder-shown:top-1/2 peer-placeholder-shown:-translate-y-1/2 peer-placeholder-shown:font-normal">
                  <span
                    class="inline-flex px-1 rounded-full bg-white/70 backdrop-blur-sm text-[#6f6974]">请输入用户名或邮箱</span>
                </label>
              </UInput>
            </UFormField>

            <UFormField name="password" required size="xl" class="form-item" :error="errors.password || undefined"
  :ui="{ error: 'text-[12px] mt-1' }">
              <UInput v-model="form.password" placeholder="" size="xl" :ui="{ base: 'peer' }"
                :type="showPassword ? 'text' : 'password'" variant="outline" class="w-full login-input" @input="errors.password = ''">
                <label
                  class="pointer-events-none absolute left-0 -top-2.5 text-highlighted text-xs font-medium px-1.5 transition-all peer-focus:-top-2.5 peer-focus:text-highlighted peer-focus:text-xs peer-focus:font-medium peer-placeholder-shown:text-sm peer-placeholder-shown:text-dimmed peer-placeholder-shown:top-1/2 peer-placeholder-shown:-translate-y-1/2 peer-placeholder-shown:font-normal">
                  <span class="inline-flex px-1 rounded-full bg-white/70 backdrop-blur-sm text-[#6f6974]">请输入密码</span>
                </label>
                <template #trailing>
                  <UButton color="neutral" variant="link" size="sm"
                    :icon="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'" aria-label="切换密码显示"
                    @click="showPassword = !showPassword" />
                </template>
              </UInput>
            </UFormField>
            <UFormField name="captchaCode" size="xl" class="form-item" :error="errors.captchaCode || undefined"
  :ui="{ error: 'text-[12px] mt-1' }">
              <div class="captcha-row">
                <div class="captcha-input-wrap">
                  <UInput v-model="form.captchaCode" placeholder="" size="xl" :ui="{ base: 'peer' }" variant="outline"
                    class="w-full login-input" @input="errors.captchaCode = ''">
                    <label
                      class="pointer-events-none absolute left-0 -top-2.5 text-highlighted text-xs font-medium px-1.5 transition-all peer-focus:-top-2.5 peer-focus:text-highlighted peer-focus:text-xs peer-focus:font-medium peer-placeholder-shown:text-sm peer-placeholder-shown:text-dimmed peer-placeholder-shown:top-1/2 peer-placeholder-shown:-translate-y-1/2 peer-placeholder-shown:font-normal">
                      <span class="inline-flex px-1 rounded-full bg-white/70 backdrop-blur-sm text-[#6f6974]">
                        请输入验证码
                      </span>
                    </label>
                  </UInput>
                </div>

                <div class="captcha-box-wrap">
                  <CommonCaptchaCanvas v-if="captcha.captchaCode" :code="captcha.captchaCode" :width="120" :height="40"
                    @refresh="fetchCaptcha" />
                  <button v-else type="button" class="captcha-box loading" @click="fetchCaptcha">
                    获取中...
                  </button>
                </div>
              </div>
            </UFormField>
            <div class="login-actions">
              <UCheckbox v-model="form.rememberMe" label="记住我" />
              <NuxtLink to="/forgot-password" class="link-btn">
                忘记密码？
              </NuxtLink>
            </div>

            <div class="login-buttons">
              <UButton type="submit" size="xl" block class="login-submit" :loading="loading">
                登录
              </UButton>

              <UButton to="/register" variant="soft" color="neutral" size="xl" block class="login-register">
                注册账号
              </UButton>
            </div>
          </UForm>

          <div class="login-footer">
            登录即表示你同意基础使用规则
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  box-sizing: border-box;
  background: linear-gradient(to top, #fff1eb 0%, #ace0f9 100%);
}

.auth-card {
  width: 1100px;
  max-width: calc(100vw - 80px);
  min-height: 680px;
  border-radius: 36px;
  background: rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.38);
  box-shadow: 0 20px 50px rgba(80, 90, 110, 0.08);
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  overflow: hidden;
  box-sizing: border-box;
}

.auth-left {
  padding: 72px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-badge {
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.34);
  color: #6f6974;
  font-size: 13px;
  font-weight: 600;
}

.auth-left h1 {
  margin: 20px 0 12px;
  font-size: 56px;
  line-height: 1.05;
  letter-spacing: -1px;
  color: #5f5b66;
}

.auth-left p {
  margin: 0;
  max-width: 430px;
  font-size: 16px;
  line-height: 1.8;
  color: #7f7a84;
}

.auth-right {
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-panel {
  width: 100%;
  max-width: 420px;
  padding: 34px 32px 28px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.42);
  box-shadow: 0 16px 40px rgba(88, 96, 110, 0.06);
  box-sizing: border-box;
}

.login-head {
  margin-bottom: 26px;
}

.login-kicker {
  font-size: 13px;
  color: #8c8791;
  margin-bottom: 8px;
}

.login-head h2 {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 600;
  color: #5f5b66;
}

.login-head p {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.7;
  color: #86808a;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-item {
  margin-bottom: 0;
}

.login-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: -2px;
}

.link-btn {
  font-size: 13px;
  color: #7d7681;
  text-decoration: none;
}

.link-btn:hover {
  opacity: 0.85;
}

.login-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.login-submit {
  color: #4f5663;
  border: none;
  box-shadow: 0 10px 24px rgba(144, 160, 190, 0.18);
}

.login-footer {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: #a09aa3;
}

.captcha-row {
  display: flex;
  align-items: stretch;
  gap: 12px;
}

.captcha-input-wrap {
  flex: 1;
}

.captcha-box {
  min-width: 120px;
  height: 40px;
  border: 1px solid rgba(185, 185, 185, 0.46);
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  color: #5f5b66;
  font-size: 14px;
  /* 原来 16px，改小 */
  font-weight: 500;
  /* 原来 600，改轻 */
  letter-spacing: 1px;
  /* 原来 2px，改小 */
  cursor: pointer;
  transition: all 0.2s ease;
}

.captcha-box.loading {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
  color: #8b8690;
}

.captcha-box.ready {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #5f5b66;
}

.captcha-box:hover {
  background: rgba(255, 255, 255, 0.36);
}
.captcha-box-wrap {
  width: 120px;
  min-width: 120px;
  height: 40px;
}

.captcha-tip {
  margin-top: 8px;
  text-align: right;
  font-size: 12px;
  color: #8a8590;
  cursor: pointer;
  user-select: none;
}

.captcha-tip:hover {
  opacity: 0.85;
}
:deep(.form-item label) {
  color: #6f6974;
}
</style>