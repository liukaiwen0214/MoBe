/**
 * 应用配置文件
 * <p>
 * 功能：配置应用的全局设置
 * 说明：定义应用的UI配置，包括颜色主题等
 * 应用场景：全局UI主题配置
 */
export default defineAppConfig({
  /**
   * UI配置
   */
  ui: {
    /**
     * 颜色配置
     */
    colors: {
      /**
       * 主色调
       * @value 'green' 绿色主题
       */
      primary: 'green',
      /**
       * 中性色调
       * @value 'slate' 石板灰色调
       */
      neutral: 'slate'
    }
  }
})
