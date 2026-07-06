/**
 * 现代 Toast 弹窗通知系统
 * 支持 success / error / info 三种类型
 */
const TOAST_DURATION = 3000

let toastContainer = null
let toastId = 0

function getContainer() {
  if (!toastContainer) {
    toastContainer = document.createElement('div')
    toastContainer.id = 'toast-container'
    toastContainer.style.cssText = `
      position: fixed;
      top: 24px;
      right: 24px;
      z-index: 99999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      pointer-events: none;
    `
    document.body.appendChild(toastContainer)
  }
  return toastContainer
}

export function showToast(message, type = 'info') {
  const id = ++toastId
  const container = getContainer()

  const toast = document.createElement('div')
  toast.id = `toast-${id}`
  toast.style.cssText = `
    pointer-events: auto;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 20px;
    border-radius: 14px;
    font-size: 14px;
    font-weight: 500;
    color: white;
    box-shadow: 0 8px 30px rgba(0,0,0,0.15);
    transform: translateX(120%);
    transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.3s ease;
    opacity: 0;
    max-width: 380px;
    font-family: Inter, system-ui, -apple-system, sans-serif;
    line-height: 1.4;
  `

  const colors = {
    success: 'linear-gradient(135deg, #059669, #047857)',
    error: 'linear-gradient(135deg, #dc2626, #b91c1c)',
    info: 'linear-gradient(135deg, #2563eb, #1d4ed8)'
  }

  toast.style.background = colors[type] || colors.info
  toast.innerHTML = `<span>${message}</span>`
  container.appendChild(toast)

  // 触发入场动画
  requestAnimationFrame(() => {
    toast.style.transform = 'translateX(0)'
    toast.style.opacity = '1'
  })

  // 自动移除
  setTimeout(() => {
    toast.style.transform = 'translateX(120%)'
    toast.style.opacity = '0'
    setTimeout(() => {
      if (toast.parentNode) toast.parentNode.removeChild(toast)
    }, 350)
  }, TOAST_DURATION)

  return id
}