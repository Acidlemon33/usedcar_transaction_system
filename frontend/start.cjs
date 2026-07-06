/**
 * 最简可靠方式：构建 -> 直接用 Electron 加载 dist/index.html
 * 跳过 Vite 开发服务器的复杂性
 */
const { spawn } = require('child_process');
const path = require('path');

const isWin = process.platform === 'win32';
const npmCmd = isWin ? 'npm.cmd' : 'npm';
const electronCmd = isWin ? 'electron.cmd' : 'electron';
const root = __dirname;

function run(command, args, options = {}) {
  // 使用 shell: true 以确保 Windows 兼容
  return spawn(command, args, { cwd: root, stdio: 'inherit', shell: true, ...options });
}

async function main() {
  // 1. 构建生产版本
  console.log('>>> [1/3] Building dist...');
  await new Promise((resolve, reject) => {
    const proc = run(npmCmd, ['run', 'build']);
    proc.on('exit', (code) => {
      if (code === 0) resolve();
      else reject(new Error(`build exited with code ${code}`));
    });
  });
  console.log('>>> dist built successfully');

  // 2. 不启动 Vite 开发服务器，直接加载 dist 文件
  //    设置 VITE_DEV_SERVER_URL 为空，让 electron/main.js 使用 loadFile
  console.log('>>> [2/3] Starting Electron (loading from dist/index.html)...');
  process.env.VITE_DEV_SERVER_URL = '';

  const electron = run(electronCmd, ['.'], {
    env: { ...process.env, VITE_DEV_SERVER_URL: '' }
  });

  // 3. 等待用户关闭
  console.log('>>> [3/3] Electron is running. Close the app window to exit.');
  electron.on('exit', () => process.exit(0));
}

main().catch(err => {
  console.error('Fatal error:', err.message);
  process.exit(1);
});