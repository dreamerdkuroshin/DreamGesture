import { invoke } from '@tauri-apps/api/tauri';
import { listen } from '@tauri-apps/api/event';

const toggleBtn = document.getElementById('toggle-detection');
const statusIndicator = document.getElementById('status-indicator');
const statusText = statusIndicator.querySelector('.status-text');
const devicesList = document.getElementById('devices-list');
const transferStatus = document.getElementById('transfer-status');
const transferProgress = document.getElementById('transfer-progress');
const progressFill = document.getElementById('progress-fill');
const progressText = document.getElementById('progress-text');

let isDetecting = false;

toggleBtn.addEventListener('click', async () => {
    try {
        if (!isDetecting) {
            await invoke('start_gesture_detection');
            await invoke('start_discovery');
            toggleBtn.textContent = 'Stop Detection';
            toggleBtn.classList.add('active');
            statusIndicator.classList.add('active');
            statusText.textContent = 'Active';
            isDetecting = true;
        } else {
            await invoke('stop_gesture_detection');
            await invoke('stop_discovery');
            toggleBtn.textContent = 'Start Detection';
            toggleBtn.classList.remove('active');
            statusIndicator.classList.remove('active');
            statusText.textContent = 'Inactive';
            isDetecting = false;
        }
    } catch (error) {
        console.error('Toggle error:', error);
    }
});

listen('tray-start-detection', () => {
    if (!isDetecting) toggleBtn.click();
});

listen('tray-stop-detection', () => {
    if (isDetecting) toggleBtn.click();
});

async function refreshDevices() {
    try {
        const devices = await invoke('get_discovered_devices');
        if (devices && devices.length > 0) {
            devicesList.innerHTML = devices.map(d => `
                <div class="device-item">
                    <span class="device-icon">${getDeviceIcon(d.device_type)}</span>
                    <div class="device-info">
                        <div class="device-name">${d.name}</div>
                        <div class="device-type">${d.device_type} • ${d.address}:${d.port}</div>
                    </div>
                </div>
            `).join('');
        }
    } catch (e) {
        // Not critical
    }
}

function getDeviceIcon(type) {
    const icons = {
        'AndroidPhone': '📱',
        'AndroidTablet': '📱',
        'WindowsDesktop': '💻',
        'WindowsLaptop': '💻',
        'LinuxDesktop': '🖥️',
        'LinuxLaptop': '💻',
        'MacOSDesktop': '🖥️',
        'MacOSLaptop': '💻',
    };
    return icons[type] || '📲';
}

setInterval(refreshDevices, 3000);

console.log('GestureShare Desktop initialized');
