// ========================================
// STATE MENU
// ========================================
const menuState = {
    body: false,
    lock: false,
    speed: false,
    jump: false,
    bypass: false,
    refresh: false
};

// ========================================
// GET MENU NAME
// ========================================
function getMenuName(menu) {
    const names = {
        body: 'aim body',
        lock: 'aim lock',
        speed: 'speed up',
        jump: 'back jump',
        bypass: 'bypass',
        refresh: 'auto refresh'
    };
    return names[menu] || menu;
}

// ========================================
// TOGGLE MENU
// ========================================
function toggleMenu(menu, isOn) {
    menuState[menu] = isOn;

    const statusId = 'status' + menu.charAt(0).toUpperCase() + menu.slice(1);
    const statusEl = document.getElementById(statusId);
    if (isOn) {
        if (statusEl) {
            statusEl.textContent = 'ON';
            statusEl.className = 'menu-status on';
        }
        showToast('✅ ' + getMenuName(menu) + ' AKTIF');
        Android.executeCommand(menu, 'start');
    } else {
        if (statusEl) {
            statusEl.textContent = 'OFF';
            statusEl.className = 'menu-status off';
        }
        showToast('⛔ ' + getMenuName(menu) + ' NONAKTIF');
        Android.executeCommand(menu, 'stop');
    }
}

// ========================================
// MODAL ADD GAME
// ========================================
function openAddGameModal() {
    const modal = document.getElementById('addGameModal');
    if (!modal) return;

    modal.classList.add('active');
    document.getElementById('gameScanResult').innerHTML = '<p style="text-align:center;color:#999;font-size:13px;">🔍 Scanning game...</p>';

    try {
        var gamesJson = Android.scanAllGames();
        var games = JSON.parse(gamesJson);

        if (games.length === 0) {
            document.getElementById('gameScanResult').innerHTML = '<p style="text-align:center;color:#999;font-size:13px;">📭 Tidak ada game terdeteksi</p>';
            return;
        }

        var html = '';
        for (var i = 0; i < games.length; i++) {
            var game = games[i];
            var isAdded = game.isAdded;
            var btnClass = isAdded ? 'btn-add-game added' : 'btn-add-game';
            var btnText = isAdded ? '✅ Sudah' : '➕ Tambah';
            var btnAction = isAdded ? '' : 'onclick="addGame(\'' + game.packageName + '\')"';

            html += `
                <div class="game-item">
                    <div class="game-info">
                        <img class="game-icon" src="data:image/png;base64,${game.iconBase64}" alt="icon">
                        <div>
                            <div class="game-name">${game.appName}</div>
                            <div class="game-pkg">${game.packageName}</div>
                        </div>
                    </div>
                    <button class="${btnClass}" ${btnAction}>${btnText}</button>
                </div>
            `;
        }
        document.getElementById('gameScanResult').innerHTML = html;

    } catch(e) {
        document.getElementById('gameScanResult').innerHTML = '<p style="text-align:center;color:#e74c3c;font-size:13px;">❌ Gagal scan: ' + e.message + '</p>';
    }
}

function closeAddGameModal() {
    var modal = document.getElementById('addGameModal');
    if (modal) modal.classList.remove('active');
    refreshUserGames();
}

// ========================================
// ADD / REMOVE / PLAY GAME
// ========================================
function addGame(packageName) {
    Android.addGame(packageName);
    refreshUserGames();
    openAddGameModal();
}

function removeGameAndRefresh(packageName) {
    Android.removeGame(packageName);
    refreshUserGames();
}

function playGame(packageName) {
    Android.openGame(packageName);
}

// ========================================
// REFRESH USER GAME LIST
// ========================================
function refreshUserGames() {
    var container = document.getElementById('userGameList');
    var count = document.getElementById('gameCount');

    if (!container) return;

    try {
        var gamesJson = Android.getUserGames();
        var games = JSON.parse(gamesJson);

        if (games.length === 0) {
            container.innerHTML = `
                <div class="game-empty-state">
                    <span class="empty-icon">🎯</span>
                    <p class="empty-title">Belum Ada Game</p>
                    <p class="empty-desc">Klik <strong>"Tambah Game"</strong> untuk menambahkan</p>
                </div>
            `;
            if (count) count.textContent = '0';
            return;
        }

        var html = '';
        for (var i = 0; i < games.length; i++) {
            var game = games[i];
            var shortName = game.appName.length > 14 ? game.appName.substring(0, 12) + '..' : game.appName;

            html += `
                <div class="game-card" onclick="playGame('${game.packageName}')">
                    <div class="game-icon-wrapper">
                        <img src="data:image/png;base64,${game.iconBase64}" alt="${game.appName}">
                    </div>
                    <div class="game-name">${shortName}</div>
                    <div class="game-actions">
                        <button class="btn-play" onclick="event.stopPropagation(); playGame('${game.packageName}')">▶ Play</button>
                        <button class="btn-remove" onclick="event.stopPropagation(); removeGameAndRefresh('${game.packageName}')">✕</button>
                    </div>
                    <span class="game-badge">⚡</span>
                </div>
            `;
        }

        container.innerHTML = html;
        if (count) count.textContent = games.length;

    } catch(e) {
        container.innerHTML = `
            <div class="game-empty-state">
                <span class="empty-icon">⚠️</span>
                <p class="empty-title">Gagal Memuat</p>
                <p class="empty-desc">Terjadi kesalahan saat memuat game</p>
            </div>
        `;
        if (count) count.textContent = '0';
    }
}

// ========================================
// START GAME
// ========================================
function startGame() {
    Android.openFreeFire();
}

// ========================================
// TOAST
// ========================================
function showToast(message) {
    const toast = document.getElementById('toast');
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add('show');
    clearTimeout(toast.timeout);
    toast.timeout = setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ========================================
// LOAD SAAT PERTAMA BUKA
// ========================================
document.addEventListener('DOMContentLoaded', function() {
    refreshUserGames();
});
