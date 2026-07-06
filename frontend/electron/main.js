import { app, BrowserWindow } from 'electron'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

async function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 860,
    minWidth: 1100,
    minHeight: 760,
    title: '二手车交易管理台',
    webPreferences: {
      contextIsolation: false,
      nodeIntegration: true,
      // 关闭 webSecurity 以允许 file:// 协议向 http://localhost:8080 发请求
      webSecurity: false
    }
  })

  const indexPath = path.resolve(__dirname, '..', 'dist', 'index.html')
  console.log('Loading frontend from', indexPath)
  await win.loadFile(indexPath)

  if (process.env.NODE_ENV !== 'production') {
    win.webContents.openDevTools({ mode: 'detach' })
  }
}

app.whenReady().then(() => {
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})
