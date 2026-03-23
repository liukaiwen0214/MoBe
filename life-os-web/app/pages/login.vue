<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { getCaptchaApi, loginApi, registerApi, sendRegisterCodeApi } from '~/api/auth'
import { useUserStore } from '~/stores/user'
import { useSystemToast } from '~/composables/useSystemToast'

definePageMeta({
  layout: 'auth',
  middleware: 'guest'
})

const userStore = useUserStore()
const systemToast = useSystemToast()

const showRegister = ref(false)
const showLoginPassword = ref(false)
const showRegisterPassword = ref(false)
const showRegisterConfirmPassword = ref(false)

const loginLoading = ref(false)
const registerLoading = ref(false)
const sendingCode = ref(false)
const pageReady = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
  captchaCode: '',
  rememberMe: false
})

const registerForm = reactive({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const captcha = reactive({
  captchaId: '',
  captchaCode: '',
  expiresAt: ''
})

const registerCodeState = reactive({
  countdown: 0
})

const loginErrors = reactive({
  account: '',
  password: '',
  captchaCode: ''
})

const registerErrors = reactive({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: '',
  agree: ''
})

const loginTouched = reactive({
  account: false
})

let countdownTimer: ReturnType<typeof setInterval> | null = null

function unwrapResponse(res: any) {
  if (res?.data && typeof res.data === 'object' && ('success' in res.data || 'data' in res.data || 'message' in res.data)) {
    return res.data
  }
  return res
}

function isValidEmail(email: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

async function fetchCaptcha() {
  try {
    const res = await getCaptchaApi()
    const payload = unwrapResponse(res)

    if (!payload?.success || !payload?.data?.captchaId) {
      systemToast.error('获取验证码失败', payload?.message || '请稍后重试', 'captcha-error')
      return
    }

    captcha.captchaId = payload.data.captchaId
    captcha.captchaCode = payload.data.captchaCode
    captcha.expiresAt = payload.data.expiresAt
  } catch (error: any) {
    systemToast.error('获取验证码失败', error?.message || '请稍后重试', 'captcha-error')
  }
}

function validateLoginAccountOnBlur() {
  loginTouched.account = true
  loginErrors.account = loginForm.account.trim() ? '' : '请输入有效的用户名或邮箱'
}

function validateLoginForm() {
  let valid = true

  if (!loginForm.account.trim()) {
    loginErrors.account = '请输入有效的用户名或邮箱'
    valid = false
  } else {
    loginErrors.account = ''
  }

  if (!loginForm.password.trim()) {
    loginErrors.password = '请输入密码'
    valid = false
  } else {
    loginErrors.password = ''
  }

  if (!loginForm.captchaCode.trim()) {
    loginErrors.captchaCode = '请输入验证码'
    valid = false
  } else {
    loginErrors.captchaCode = ''
  }

  return valid
}

async function handleLogin() {
  if (!validateLoginForm()) return

  loginLoading.value = true

  try {
    const res = await loginApi({
      account: loginForm.account.trim(),
      password: loginForm.password,
      rememberMe: loginForm.rememberMe ? 1 : 0,
      captchaId: captcha.captchaId,
      code: loginForm.captchaCode.trim()
    })
    const payload = unwrapResponse(res)

    if (!payload?.success || !payload?.data?.sessionToken) {
      userStore.logout()
      await fetchCaptcha()
      systemToast.error('登录失败', payload?.message || '登录失败，请稍后重试', 'login-error')
      return
    }

    userStore.setToken(payload.data.sessionToken)
    await userStore.fetchMe()

    systemToast.success('登录成功', '欢迎回来', 'login-success')
    await navigateTo('/')
  } catch (error: any) {
    userStore.logout()
    await fetchCaptcha()

    const message =
      error?.response?.data?.message ||
      error?.message ||
      '登录失败，请稍后重试'

    systemToast.error('登录失败', message, 'login-error')
  } finally {
    loginLoading.value = false
  }
}

function validateRegisterForm() {
  let valid = true

  if (!registerForm.username.trim()) {
    registerErrors.username = '请输入用户名'
    valid = false
  } else {
    registerErrors.username = ''
  }

  if (!registerForm.email.trim()) {
    registerErrors.email = '请输入邮箱'
    valid = false
  } else if (!isValidEmail(registerForm.email.trim())) {
    registerErrors.email = '请输入正确的邮箱格式'
    valid = false
  } else {
    registerErrors.email = ''
  }

  if (!registerForm.code.trim()) {
    registerErrors.code = '请输入验证码'
    valid = false
  } else {
    registerErrors.code = ''
  }

  if (!registerForm.password.trim()) {
    registerErrors.password = '请输入密码'
    valid = false
  } else if (registerForm.password.length < 6) {
    registerErrors.password = '密码长度至少 6 位'
    valid = false
  } else {
    registerErrors.password = ''
  }

  if (!registerForm.confirmPassword.trim()) {
    registerErrors.confirmPassword = '请再次输入密码'
    valid = false
  } else if (registerForm.confirmPassword !== registerForm.password) {
    registerErrors.confirmPassword = '两次输入的密码不一致'
    valid = false
  } else {
    registerErrors.confirmPassword = ''
  }

  if (!registerForm.agree) {
    registerErrors.agree = '请先勾选同意'
    valid = false
  } else {
    registerErrors.agree = ''
  }

  return valid
}

function startCountdown() {
  registerCodeState.countdown = 60

  if (countdownTimer) {
    clearInterval(countdownTimer)
  }

  countdownTimer = setInterval(() => {
    if (registerCodeState.countdown <= 1) {
      registerCodeState.countdown = 0
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
      return
    }

    registerCodeState.countdown -= 1
  }, 1000)
}

async function handleSendRegisterCode() {
  if (sendingCode.value || registerCodeState.countdown > 0) return

  if (!registerForm.email.trim()) {
    registerErrors.email = '请输入邮箱'
    return
  }

  if (!isValidEmail(registerForm.email.trim())) {
    registerErrors.email = '请输入正确的邮箱格式'
    return
  }

  sendingCode.value = true

  try {
    const res = await sendRegisterCodeApi({
      email: registerForm.email.trim()
    })
    const payload = unwrapResponse(res)

    if (!payload?.success) {
      systemToast.error('发送失败', payload?.message || '发送验证码失败', 'register-code-error')
      return
    }

    startCountdown()
    systemToast.success('发送成功', '验证码已发送，请注意查收', 'register-code-success')
  } catch (error: any) {
    const message =
      error?.response?.data?.message ||
      error?.message ||
      '发送验证码失败'

    systemToast.error('发送失败', message, 'register-code-error')
  } finally {
    sendingCode.value = false
  }
}

async function handleRegister() {
  if (!validateRegisterForm()) return

  registerLoading.value = true

  try {
    const res = await registerApi({
      username: registerForm.username.trim(),
      email: registerForm.email.trim(),
      code: registerForm.code.trim(),
      password: registerForm.password
    })
    const payload = unwrapResponse(res)

    if (!payload?.success) {
      systemToast.error('注册失败', payload?.message || '注册失败，请稍后重试', 'register-error')
      return
    }

    systemToast.success('注册成功', '现在可以登录了', 'register-success')

    loginForm.account = registerForm.email.trim()

    registerForm.username = ''
    registerForm.email = ''
    registerForm.code = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.agree = false

    setTimeout(() => {
      showRegister.value = false
    }, 220)
  } catch (error: any) {
    const message =
      error?.response?.data?.message ||
      error?.message ||
      '注册失败，请稍后重试'

    systemToast.error('注册失败', message, 'register-error')
  } finally {
    registerLoading.value = false
  }
}

const passwordStrength = computed(() => {
  const pwd = registerForm.password

  if (!pwd) {
    return {
      score: 0,
      text: '请输入密码',
      color: '#b0a8b7'
    }
  }

  let score = 0

  if (pwd.length >= 6) score++
  if (/[A-Z]/.test(pwd) || /[a-z]/.test(pwd)) score++
  if (/\d/.test(pwd)) score++
  if (/[^A-Za-z0-9]/.test(pwd)) score++

  if (pwd.length < 6 || score <= 1) {
    return {
      score: 1,
      text: '弱',
      color: '#d08b95'
    }
  }

  if (score === 2 || score === 3) {
    return {
      score: 2,
      text: '中',
      color: '#d7b57c'
    }
  }

  return {
    score: 3,
    text: '强',
    color: '#86a98f'
  }
})

onMounted(() => {
  fetchCaptcha()

  requestAnimationFrame(() => {
    pageReady.value = true
  })
})

onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<template>
  <div class="login-page">
    <div class="auth-card" :class="{ 'is-register': showRegister, 'is-ready': pageReady }">
      <div class="auth-left">
        <div class="auth-badge">
          MoBe
        </div>

        <Transition name="fade-slide" mode="out-in">
          <div :key="showRegister ? 'register-left' : 'login-left'">
            <h1>{{ showRegister ? '欢迎加入' : '欢迎回来' }}</h1>
            <p>
              {{
                showRegister
                  ? '从任务、日程到个人节奏，把生活慢慢整理清楚。'
                  : '让生活、任务、日程和记录都安静地归位。'
              }}
            </p>
          </div>
        </Transition>
      </div>

      <div class="auth-right">
        <Transition name="panel-switch" mode="out-in">
          <div v-if="!showRegister" key="login" class="login-panel">
            <div class="login-head">
              <div class="login-kicker">欢迎回来</div>
              <h2>登录 MoBe</h2>
              <p>继续管理你的任务、日程与个人节奏</p>
            </div>

            <UForm :state="loginForm" class="login-form" @submit="handleLogin">
              <UFormField
                name="account"
                size="xl"
                class="form-item"
                :error="loginTouched.account && loginErrors.account ? loginErrors.account : undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <UInput
                  v-model="loginForm.account"
                  placeholder=" "
                  size="xl"
                  variant="outline"
                  class="w-full"
                  :ui="{ base: 'peer' }"
                  @blur="validateLoginAccountOnBlur"
                  @input="loginTouched.account && (loginErrors.account = loginForm.account.trim() ? '' : '请输入有效的用户名或邮箱')"
                >
                  <label class="floating-label">
                    <span class="floating-label-bg">账号</span>
                  </label>
                </UInput>
              </UFormField>

              <UFormField
                name="password"
                size="xl"
                class="form-item"
                :error="loginErrors.password || undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <UInput
                  v-model="loginForm.password"
                  :type="showLoginPassword ? 'text' : 'password'"
                  placeholder=" "
                  size="xl"
                  variant="outline"
                  class="w-full"
                  :ui="{ base: 'peer', trailing: 'pe-1' }"
                  @input="loginErrors.password = ''"
                >
                  <label class="floating-label">
                    <span class="floating-label-bg">密码</span>
                  </label>

                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="showLoginPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      aria-label="切换密码显示"
                      @click="showLoginPassword = !showLoginPassword"
                    />
                  </template>
                </UInput>
              </UFormField>

              <UFormField
                name="captchaCode"
                size="xl"
                class="form-item"
                :error="loginErrors.captchaCode || undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <div class="captcha-row">
                  <div class="captcha-input-wrap">
                    <UInput
                      v-model="loginForm.captchaCode"
                      placeholder=" "
                      size="xl"
                      variant="outline"
                      class="w-full"
                      :ui="{ base: 'peer' }"
                      @input="loginErrors.captchaCode = ''"
                    >
                      <label class="floating-label">
                        <span class="floating-label-bg">验证码</span>
                      </label>
                    </UInput>
                  </div>

                  <div class="captcha-box-wrap">
                    <CommonCaptchaCanvas
                      v-if="captcha.captchaCode"
                      :code="captcha.captchaCode"
                      :width="120"
                      :height="40"
                      @refresh="fetchCaptcha"
                    />
                    <button
                      v-else
                      type="button"
                      class="captcha-box loading"
                      @click="fetchCaptcha"
                    >
                      获取中...
                    </button>
                  </div>
                </div>

                <div class="captcha-tip" @click="fetchCaptcha">
                  看不清？换一个
                </div>
              </UFormField>

              <div class="login-actions">
                <UCheckbox v-model="loginForm.rememberMe" label="记住我" />
                <button type="button" class="link-btn pseudo-link">
                  忘记密码？
                </button>
              </div>

              <div class="login-buttons">
                <UButton
                  type="submit"
                  size="xl"
                  block
                  class="login-submit"
                  :loading="loginLoading"
                >
                  登录
                </UButton>

                <UButton
                  variant="soft"
                  color="neutral"
                  size="xl"
                  block
                  class="login-register"
                  @click="showRegister = true"
                >
                  创建账号
                </UButton>
              </div>
            </UForm>

            <div class="login-footer">
              登录即表示你同意基础使用规则
            </div>
          </div>

          <div v-else key="register" class="login-panel">
            <div class="login-head">
              <div class="login-kicker">欢迎加入</div>
              <h2>创建你的 MoBe</h2>
              <p>从任务、日程到个人节奏，把生活慢慢整理清楚。</p>
            </div>

            <UForm :state="registerForm" class="login-form" @submit="handleRegister">
              <UFormField
                name="username"
                size="xl"
                class="form-item"
                :error="registerErrors.username || undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <UInput
                  v-model="registerForm.username"
                  placeholder=" "
                  size="xl"
                  variant="outline"
                  class="w-full"
                  :ui="{ base: 'peer' }"
                  @input="registerErrors.username = ''"
                >
                  <label class="floating-label">
                    <span class="floating-label-bg">用户名</span>
                  </label>
                </UInput>
              </UFormField>

              <UFormField
                name="email"
                size="xl"
                class="form-item"
                :error="registerErrors.email || undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <UInput
                  v-model="registerForm.email"
                  placeholder=" "
                  size="xl"
                  variant="outline"
                  class="w-full"
                  :ui="{ base: 'peer' }"
                  @input="registerErrors.email = ''"
                >
                  <label class="floating-label">
                    <span class="floating-label-bg">邮箱</span>
                  </label>
                </UInput>
              </UFormField>

              <UFormField
                name="code"
                size="xl"
                class="form-item"
                :error="registerErrors.code || undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <div class="captcha-row">
                  <div class="captcha-input-wrap">
                    <UInput
                      v-model="registerForm.code"
                      placeholder=" "
                      size="xl"
                      variant="outline"
                      class="w-full"
                      :ui="{ base: 'peer' }"
                      @input="registerErrors.code = ''"
                    >
                      <label class="floating-label">
                        <span class="floating-label-bg">验证码</span>
                      </label>
                    </UInput>
                  </div>

                  <UButton
                    type="button"
                    color="neutral"
                    variant="ghost"
                    class="send-code-btn"
                    :loading="sendingCode"
                    :disabled="sendingCode || registerCodeState.countdown > 0"
                    @click="handleSendRegisterCode"
                  >
                    {{ registerCodeState.countdown > 0 ? `${registerCodeState.countdown}s` : '发送验证码' }}
                  </UButton>
                </div>
              </UFormField>

              <UFormField
                name="password"
                size="xl"
                class="form-item"
                :error="registerErrors.password || undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <UInput
                  v-model="registerForm.password"
                  :type="showRegisterPassword ? 'text' : 'password'"
                  placeholder=" "
                  size="xl"
                  variant="outline"
                  class="w-full"
                  :ui="{ base: 'peer', trailing: 'pe-1' }"
                  @input="registerErrors.password = ''"
                >
                  <label class="floating-label">
                    <span class="floating-label-bg">密码</span>
                  </label>

                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="showRegisterPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      aria-label="切换密码显示"
                      @click="showRegisterPassword = !showRegisterPassword"
                    />
                  </template>
                </UInput>

                <div class="password-strength">
                  <div class="strength-bar">
                    <div
                      class="strength-fill"
                      :style="{
                        width: `${passwordStrength.score * 33.33}%`,
                        background: passwordStrength.color
                      }"
                    />
                  </div>
                  <div class="strength-text" :style="{ color: passwordStrength.color }">
                    密码强度：{{ passwordStrength.text }}
                  </div>
                </div>
              </UFormField>

              <UFormField
                name="confirmPassword"
                size="xl"
                class="form-item"
                :error="registerErrors.confirmPassword || undefined"
                :ui="{ error: 'text-[12px] mt-1 text-[#d08b95]' }"
              >
                <UInput
                  v-model="registerForm.confirmPassword"
                  :type="showRegisterConfirmPassword ? 'text' : 'password'"
                  placeholder=" "
                  size="xl"
                  variant="outline"
                  class="w-full"
                  :ui="{ base: 'peer', trailing: 'pe-1' }"
                  @input="registerErrors.confirmPassword = ''"
                >
                  <label class="floating-label">
                    <span class="floating-label-bg">确认密码</span>
                  </label>

                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="showRegisterConfirmPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      aria-label="切换密码显示"
                      @click="showRegisterConfirmPassword = !showRegisterConfirmPassword"
                    />
                  </template>
                </UInput>
              </UFormField>

              <div class="register-agree">
                <UCheckbox v-model="registerForm.agree" label="我已阅读并同意基础使用规则" />
              </div>

              <div v-if="registerErrors.agree" class="agree-error">
                {{ registerErrors.agree }}
              </div>

              <div class="login-buttons">
                <UButton
                  type="submit"
                  size="xl"
                  block
                  class="login-submit"
                  :loading="registerLoading"
                >
                  注册
                </UButton>

                <UButton
                  variant="soft"
                  color="neutral"
                  size="xl"
                  block
                  class="login-register"
                  @click="showRegister = false"
                >
                  返回登录
                </UButton>
              </div>
            </UForm>

            <div class="login-footer">
              注册成功后即可立即登录
            </div>
          </div>
        </Transition>
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
  background-image: linear-gradient(120deg, #fdfbfb 0%, #ebedee 100%);
  background-blend-mode: screen;
}

.auth-card {
  width: 1120px;
  max-width: calc(100vw - 80px);
  min-height: 700px;
  border-radius: 36px;
  background: rgba(255, 255, 255, 0.32);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(145, 132, 126, 0.18);
  box-shadow:
    0 24px 60px rgba(80, 90, 110, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.35);
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  overflow: hidden;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(18px) scale(0.988);
  transition:
    opacity 0.55s ease,
    transform 0.55s ease,
    box-shadow 0.35s ease;
}

.auth-card.is-ready {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.auth-card.is-register {
  transform: translateY(-2px);
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
  max-width: 460px;
  padding: 40px 36px 32px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(145, 132, 126, 0.12);
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
  position: relative;
}

.floating-label {
  pointer-events: none;
  position: absolute;
  left: 0;
  top: -10px;
  padding: 0 6px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.peer-placeholder-shown ~ .floating-label {
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  font-weight: 400;
}

.peer-focus ~ .floating-label,
.peer:not(:placeholder-shown) ~ .floating-label {
  top: -10px;
  transform: none;
  font-size: 12px;
  font-weight: 500;
}

.floating-label-bg {
  display: inline-flex;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #6f6974;
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

.pseudo-link {
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
}

.pseudo-link:hover {
  opacity: 0.85;
}

.login-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.login-submit {
  border: none;
  color: #525a68;
  box-shadow: 0 10px 24px rgba(144, 160, 190, 0.14);
}

.login-register {
  color: #6f6974;
  border: 1px solid rgba(255, 255, 255, 0.36);
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
  position: relative;
}

.captcha-box-wrap {
  width: 120px;
  min-width: 120px;
  height: 40px;
}

.captcha-box {
  width: 120px;
  height: 40px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.32);
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #7e7884;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
  transition: all 0.2s ease;
}

.captcha-box:hover {
  background: rgba(255, 255, 255, 0.22);
  border-color: rgba(255, 255, 255, 0.4);
}

.captcha-tip {
  margin-top: 8px;
  text-align: right;
  font-size: 12px;
  color: #8a8590;
  cursor: pointer;
}

.send-code-btn {
  min-width: 120px;
  height: 40px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.32);
  color: #6f6974;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.send-code-btn:hover {
  background: rgba(255, 255, 255, 0.22);
  border-color: rgba(255, 255, 255, 0.4);
}

.send-code-btn:disabled {
  opacity: 0.72;
}

.password-strength {
  margin-top: 10px;
}

.strength-bar {
  height: 6px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.38);
}

.strength-fill {
  height: 100%;
  border-radius: 999px;
  transition: all 0.2s ease;
}

.strength-text {
  margin-top: 8px;
  font-size: 12px;
}

.register-agree {
  margin-top: -4px;
}

.agree-error {
  margin-top: -10px;
  font-size: 12px;
  color: #d08b95;
}
@media (max-width: 1023px) {
  .login-page {
    position: relative;
    min-height: 100vh;
    padding: 20px 14px;
    align-items: stretch;
  }

  .auth-card {
    width: 100%;
    max-width: 100%;
    min-height: auto;
    grid-template-columns: 1fr;
    border-radius: 24px;
    transform: none !important;
  }

  .auth-left {
    padding: 28px 22px 12px;
  }

  .auth-left h1 {
    margin: 14px 0 8px;
    font-size: 34px;
    line-height: 1.12;
  }

  .auth-left p {
    max-width: none;
    font-size: 14px;
    line-height: 1.7;
  }

  .auth-right {
    padding: 12px 14px 16px;
  }

  .login-panel {
    max-width: 100%;
    padding: 24px 18px 20px;
    border-radius: 22px;
  }

  .login-head {
    margin-bottom: 20px;
  }

  .login-head h2 {
    font-size: 26px;
  }

  .login-head p {
    font-size: 13px;
    line-height: 1.65;
  }

  .login-form {
    gap: 16px;
  }

  .login-buttons {
    margin-top: 4px;
  }

  .login-footer {
    margin-top: 14px;
  }
}

@media (max-width: 767px) {
  .login-page {
    padding: 12px;
  }

  .auth-card {
    border-radius: 20px;
  }

  .auth-left {
    padding: 22px 16px 8px;
  }

  .auth-badge {
    padding: 6px 12px;
    font-size: 12px;
  }

  .auth-left h1 {
    font-size: 28px;
    margin: 12px 0 6px;
  }

  .auth-left p {
    font-size: 13px;
    line-height: 1.6;
  }

  .auth-right {
    padding: 8px 10px 12px;
  }

  .login-panel {
    padding: 18px 14px 16px;
    border-radius: 18px;
  }

  .login-head {
    margin-bottom: 16px;
  }

  .login-kicker {
    font-size: 12px;
    margin-bottom: 6px;
  }

  .login-head h2 {
    font-size: 24px;
  }

  .login-head p {
    font-size: 12px;
    margin-top: 8px;
  }

  .captcha-row {
    flex-direction: column;
    gap: 10px;
  }

  .captcha-box-wrap,
  .send-code-btn {
    width: 100%;
    min-width: 100%;
  }

  .captcha-box {
    width: 100%;
  }

  .login-actions {
    gap: 10px;
    align-items: center;
  }

  .link-btn {
    font-size: 12px;
  }

  .floating-label {
    font-size: 11px;
  }

  .peer-placeholder-shown ~ .floating-label {
    font-size: 13px;
  }
}

@media (max-width: 479px) {
  .login-page {
    padding: 10px;
  }

  .auth-card {
    border-radius: 18px;
  }

  .auth-left {
    padding: 18px 14px 6px;
  }

  .auth-left h1 {
    font-size: 24px;
  }

  .auth-left p {
    font-size: 12px;
  }

  .login-panel {
    padding: 16px 12px 14px;
    border-radius: 16px;
  }

  .login-head h2 {
    font-size: 22px;
  }

  .login-actions {
    flex-wrap: wrap;
    justify-content: space-between;
  }

  .login-submit,
  .login-register {
    min-height: 44px;
  }
}
</style>
