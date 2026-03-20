/**
 * 系统提示
 */
export function useSystemToast() {
  const toast = useToast()
  /**
   * 构建提示ID
   */
  function buildId(options: {
    id?: string
    title?: string
    description?: string
    color?: 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error' | 'neutral'
  }) {
    if (options.id) return options.id

    return [
      options.color || 'neutral',
      options.title || '',
      options.description || ''
    ].join('::')
  }
  /**
   * 显示提示
   */
  function show(options: {
    id?: string
    title: string
    description?: string
    icon?: string
    color?: 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error' | 'neutral'
    duration?: number
  }) {
    toast.add({
      id: buildId(options),
      title: options.title,
      description: options.description,
      icon: options.icon,
      color: options.color,
      duration: options.duration
    })
  }
  /**
   * 显示成功提示
   */
  function success(title: string, description?: string, id?: string) {
    show({
      id,
      title,
      description,
      color: 'success',
      icon: 'i-lucide-circle-check'
    })
  }
  /**
   * 显示错误提示
   */
  function error(title: string, description?: string, id?: string) {
    show({
      id,
      title,
      description,
      color: 'error',
      icon: 'i-lucide-circle-alert'
    })
  }

  /**
   * 显示信息提示
   */ 
  function info(title: string, description?: string, id?: string) {
    show({
      id,
      title,
      description,
      color: 'info',
      icon: 'i-lucide-info'
    })
  }
  /**
   * 显示警告提示
   */       
  function warning(title: string, description?: string, id?: string) {
    show({
      id,
      title,
      description,
      color: 'warning',
      icon: 'i-lucide-triangle-alert'
    })
  }

  return {
    show,
    success,
    error,
    info,
    warning
  }
}