// API Base URLs
const API_BASE = 'http://localhost:8080';
const OPENAI_API_KEY = 'Sid_123'; // Placeholder Key

// State
let portfolios = [];
let marketStocks = [];
let transactions = [];

// DOM Elements
document.addEventListener('DOMContentLoaded', () => {
    initDashboard();
    setupEventListeners();
});

function setupEventListeners() {
    // Modal Close Buttons
    window.onclick = function (event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = "none";
        }
    }

    // Forms
    document.getElementById('create-portfolio-form').addEventListener('submit', handleCreatePortfolio);
    document.getElementById('create-stock-form').addEventListener('submit', handleCreateMarketStock);
    document.getElementById('buy-stock-form').addEventListener('submit', handleBuyStock);
    document.getElementById('sell-stock-form').addEventListener('submit', handleSellStock);

    // Buy/Sell Calculators
    document.getElementById('buy-quantity').addEventListener('input', updateBuyTotal);
    document.getElementById('sell-quantity').addEventListener('input', updateSellTotal);

    // Chat Enter Key
    document.getElementById('chat-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
}

// --- Navigation ---
function showSection(sectionId) {
    document.querySelectorAll('main > section').forEach(sec => {
        sec.style.display = 'none';
        sec.classList.remove('animate-fade-in');
    });

    const target = document.getElementById(sectionId);
    target.style.display = 'block';
    void target.offsetWidth;
    target.classList.add('animate-fade-in');

    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('onclick').includes(sectionId)) {
            link.classList.add('active');
        }
    });

    if (sectionId === 'dashboard') initDashboard();
    if (sectionId === 'portfolios') loadPortfolios();
    if (sectionId === 'stocks') loadMarketStocks();
}

// --- Dashboard ---
async function initDashboard() {
    try {
        await Promise.all([loadPortfolios(false), loadMarketStocks(false)]);

        let totalCurrentValue = 0;
        let totalInvestedValue = 0;
        let globalStockMap = {};
        let allHoldings = [];

        // Calculate total value for each portfolio
        for (let p of portfolios) {
            const holdings = await calculatePortfolioHoldings(p.id);

            p.calculatedValue = holdings.reduce((sum, h) => sum + h.currentValue, 0);
            p.investedValue = holdings.reduce((sum, h) => sum + (h.avgPrice * h.quantity), 0);

            totalCurrentValue += p.calculatedValue;
            totalInvestedValue += p.investedValue;

            holdings.forEach(h => {
                if (globalStockMap[h.symbol]) globalStockMap[h.symbol] += h.currentValue;
                else globalStockMap[h.symbol] = h.currentValue;

                // Track unique stocks for the graph
                if (!allHoldings.find(ah => ah.symbol === h.symbol)) {
                    allHoldings.push(h);
                }
            });
        }

        // Render Stats
        document.getElementById('dashboard-total-value').textContent = formatCurrency(totalCurrentValue);
        document.getElementById('dashboard-total-invested').textContent = formatCurrency(totalInvestedValue);

        const profit = totalCurrentValue - totalInvestedValue;
        const profitPercent = totalInvestedValue > 0 ? (profit / totalInvestedValue) * 100 : 0;

        const profitEl = document.getElementById('dashboard-total-profit');
        profitEl.textContent = formatCurrency(profit);
        profitEl.style.color = profit >= 0 ? 'var(--success-color)' : 'var(--danger-color)';

        const percentEl = document.getElementById('dashboard-profit-percent');
        percentEl.textContent = (profit >= 0 ? '+' : '') + profitPercent.toFixed(2) + '%';
        percentEl.style.color = profit >= 0 ? 'var(--success-color)' : 'var(--danger-color)';

        renderCharts(globalStockMap, portfolios, allHoldings);
    } catch (error) {
        console.error("Dashboard Init Error:", error);
    }
}

function renderCharts(stockMap, portfolios, allHoldings) {
    // Allocation Chart (Pie)
    const ctxA = document.getElementById('allocationChart').getContext('2d');
    if (window.allocationChartInstance) window.allocationChartInstance.destroy();

    window.allocationChartInstance = new Chart(ctxA, {
        type: 'doughnut',
        data: {
            labels: Object.keys(stockMap),
            datasets: [{
                data: Object.values(stockMap),
                backgroundColor: ['#d4a373', '#4cc9f0', '#f72585', '#4361ee', '#7209b7', '#2a9d8f', '#e9c46a'],
                borderColor: '#1b263b',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'right', labels: { color: '#e0e1dd' } } }
        }
    });

    // Portfolio Value Chart (Bar)
    const ctxP = document.getElementById('portfolioChart').getContext('2d');
    if (window.portfolioChartInstance) window.portfolioChartInstance.destroy();

    window.portfolioChartInstance = new Chart(ctxP, {
        type: 'bar',
        data: {
            labels: portfolios.map(p => p.name),
            datasets: [{
                label: 'Portfolio Value (₹)',
                data: portfolios.map(p => p.calculatedValue),
                backgroundColor: '#4cc9f0',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { grid: { color: 'rgba(255,255,255,0.1)' }, ticks: { color: '#778da9' } },
                x: { grid: { display: false }, ticks: { color: '#778da9' } }
            }
        }
    });

    // Performance Chart (Real)
    renderPerformanceChart(allHoldings);
}

async function renderPerformanceChart(holdings) {
    // Re-check canvas existence (might have been renamed in index.html)
    const canvas = document.getElementById('performanceChart');
    if (!canvas) {
        console.warn("Performance Chart canvas not found.");
        return;
    }
    const ctxT = canvas.getContext('2d');
    if (window.performanceChartInstance) window.performanceChartInstance.destroy();

    // Limit to top 5 stocks to avoid API rate limits
    const topStocks = holdings
        .sort((a, b) => b.currentValue - a.currentValue)
        .slice(0, 5);

    if (topStocks.length === 0) {
        // Render Empty State if no stocks
        return;
    }

    // Fetch history for each stock (1Y range default for portfolio view)
    // We will aggregate them.

    // Map: Date (String) -> Total Value (Number)
    let aggregatedHistory = {};
    let allDates = new Set();

    try {
        // Fetch histories in parallel (limited)
        const historyPromises = topStocks.map(s =>
            fetch(`${API_BASE}/stocks/history/${s.symbol}/1M`).then(res => res.ok ? res.json() : {})
        );

        const histories = await Promise.all(historyPromises);

        histories.forEach((historyMap, index) => {
            const stock = topStocks[index];
            Object.keys(historyMap).forEach(date => {
                allDates.add(date);
                if (!aggregatedHistory[date]) aggregatedHistory[date] = 0;
                // Add value of this stock on this date: Price * Current Quantity
                // (Assuming constant quantity for simplicity of simulation)
                aggregatedHistory[date] += (historyMap[date] * stock.quantity);
            });
        });

        const sortedDates = Array.from(allDates).sort();
        const dataPoints = sortedDates.map(d => aggregatedHistory[d]);

        // Invested Line (Flat)
        const totalInvested = topStocks.reduce((sum, s) => sum + (s.avgPrice * s.quantity), 0);
        const investedLine = new Array(sortedDates.length).fill(totalInvested);

        window.performanceChartInstance = new Chart(ctxT, {
            type: 'line',
            data: {
                labels: sortedDates,
                datasets: [
                    {
                        label: 'Market Value',
                        data: dataPoints,
                        borderColor: '#2ecc71',
                        backgroundColor: 'rgba(46, 204, 113, 0.1)',
                        fill: true,
                        tension: 0.4,
                        pointRadius: 0
                    },
                    {
                        label: 'Invested',
                        data: investedLine,
                        borderColor: '#95a5a6',
                        borderDash: [5, 5],
                        pointRadius: 0,
                        borderWidth: 1,
                        fill: false
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { grid: { color: 'rgba(255,255,255,0.1)' }, ticks: { color: '#778da9' } },
                    x: { display: false }
                },
                plugins: { legend: { display: true, labels: { color: '#e0e1dd' } } },
                interaction: { mode: 'index', intersect: false }
            }
        });

    } catch (e) {
        console.error("Chart Error", e);
    }
}

// --- Portfolios ---
async function loadPortfolios(render = true) {
    try {
        const res = await fetch(`${API_BASE}/portfolios`);
        portfolios = await res.json();

        if (render) {
            const list = document.getElementById('portfolio-list');
            list.innerHTML = '';

            for (let p of portfolios) {
                // Calculate real value based on transactions
                const holdings = await calculatePortfolioHoldings(p.id);
                const value = holdings.reduce((sum, h) => sum + h.currentValue, 0);

                const card = document.createElement('div');
                card.className = 'card';
                card.innerHTML = `
                    <h3>${p.name}</h3>
                    <p style="color: var(--text-muted); font-size: 0.9rem;">${p.description || 'No description'}</p>
                    <div class="value" style="font-size: 1.5rem; margin-top: 10px;">${formatCurrency(value)}</div>
                    <div style="margin-top: 1rem; display: flex; justify-content: space-between;">
                        <button class="btn btn-primary" style="padding: 0.5rem;" onclick="viewPortfolioDetails(${p.id}, '${p.name}')">View Holdings</button>
                        <button class="btn btn-delete" onclick="deletePortfolio(${p.id})">Delete</button>
                    </div>
                `;
                list.appendChild(card);
            }
        }
    } catch (e) {
        console.error(e);
    }
}

async function handleCreatePortfolio(e) {
    e.preventDefault();
    const name = document.getElementById('p-name').value;
    const desc = document.getElementById('p-desc').value;

    try {
        const res = await fetch(`${API_BASE}/portfolios`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, description: desc, stockIds: [] })
        });
        if (res.ok) {
            closeModal('create-portfolio-modal');
            showSection('portfolios');
        }
    } catch (e) { alert("Error creating portfolio"); }
}

async function deletePortfolio(id) {
    if (!confirm("Are you sure?")) return;
    try {
        const res = await fetch(`${API_BASE}/portfolios/${id}`, { method: 'DELETE' });
        if (res.ok) {
            loadPortfolios();
        } else {
            alert("Failed to delete portfolio. It may have transactions associated with it.");
            console.error("Delete failed with status:", res.status);
        }
    } catch (e) {
        console.error("Delete error:", e);
        alert("Error deleting portfolio.");
    }
}

// --- Portfolio Details & Holdings Calculation ---
async function viewPortfolioDetails(portfolioId, name) {
    document.getElementById('portfolio-list').style.display = 'none';
    const detailsDiv = document.getElementById('portfolio-details');
    detailsDiv.style.display = 'block';
    document.getElementById('pd-name').textContent = name;

    const holdings = await calculatePortfolioHoldings(portfolioId);
    renderHoldingsTable(holdings, portfolioId);
}

function closePortfolioDetails() {
    document.getElementById('portfolio-details').style.display = 'none';
    document.getElementById('portfolio-list').style.display = 'grid';
}

async function calculatePortfolioHoldings(portfolioId) {
    // 1. Fetch transactions for this portfolio
    const tRes = await fetch(`${API_BASE}/transactions/portfolio/${portfolioId}`);
    const transactions = await tRes.json();

    // 2. Fetch all stocks (Market) to get current prices
    if (marketStocks.length === 0) await loadMarketStocks(false);

    const holdingsMap = {};

    transactions.forEach(t => {
        if (!holdingsMap[t.stockId]) {
            holdingsMap[t.stockId] = { qty: 0, totalCost: 0 };
        }

        if (t.transactionType === 'BUY') {
            holdingsMap[t.stockId].qty += t.amount;
            holdingsMap[t.stockId].totalCost += (t.amount * t.pricePerUnit);
        } else if (t.transactionType === 'SELL') {
            const currentQty = holdingsMap[t.stockId].qty;
            if (currentQty > 0) {
                const avgPrice = holdingsMap[t.stockId].totalCost / currentQty;
                holdingsMap[t.stockId].totalCost -= (t.amount * avgPrice);
                holdingsMap[t.stockId].qty -= t.amount;
            }
        }
    });

    // Convert map to array and enrich with market data
    const holdings = [];
    for (const [stockId, data] of Object.entries(holdingsMap)) {
        if (data.qty > 0) {
            const stock = marketStocks.find(s => s.id == stockId);
            if (stock) {
                holdings.push({
                    stockId: stock.id,
                    symbol: stock.symbol,
                    companyName: stock.companyName,
                    quantity: data.qty,
                    avgPrice: data.totalCost / data.qty,  // Simple Avg
                    currentPrice: stock.currentPrice,
                    currentValue: data.qty * stock.currentPrice
                });
            }
        }
    }
    return holdings;
}

function renderHoldingsTable(holdings, portfolioId) {
    const tbody = document.getElementById('pd-table-body');
    tbody.innerHTML = '';

    if (holdings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No stocks owned in this portfolio. Go to Market to Buy.</td></tr>';
        return;
    }

    holdings.forEach(h => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="color: var(--highlight-color); font-weight: bold;">${h.symbol}</td>
            <td>${h.companyName}</td>
            <td>${h.quantity}</td>
            <td>${formatCurrency(h.avgPrice)}</td>
            <td>${formatCurrency(h.currentValue)}</td>
            <td>
                <button class="btn btn-delete" onclick="openSellModal(${portfolioId}, ${h.stockId}, '${h.symbol}', ${h.quantity}, ${h.currentPrice})">Sell</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// --- Market Stocks (Global) ---
async function loadMarketStocks(render = true) {
    try {
        const res = await fetch(`${API_BASE}/stocks`);
        marketStocks = await res.json();

        if (render) {
            const tbody = document.getElementById('stock-table-body');
            tbody.innerHTML = '';

            // Render rows
            for (const s of marketStocks) {
                const tr = document.createElement('tr');
                tr.style.cursor = 'pointer';
                tr.onclick = (e) => {
                    // Prevent click if clicking the Buy button
                    if (e.target.tagName !== 'BUTTON') openStockDetails(s.id);
                };

                // Determine Change Color
                const change = s.changePercent || 0;
                const colorClass = change >= 0 ? 'var(--success-color)' : 'var(--danger-color)';
                const icon = change >= 0 ? '<i class="fas fa-caret-up"></i>' : '<i class="fas fa-caret-down"></i>';

                tr.innerHTML = `
                    <td style="color: var(--highlight-color); font-weight: bold;">${s.symbol}</td>
                    <td>${s.companyName}</td>
                    <td>${formatCurrency(s.currentPrice)}</td>
                    <td style="color: ${colorClass}; font-weight: bold;">
                        ${icon} ${change.toFixed(2)}%
                    </td>
                    <td>
                        <div style="width: 100px; height: 40px;">
                            <canvas id="chart-${s.id}"></canvas>
                        </div>
                    </td>
                    <td>
                        <button class="btn btn-primary" style="padding: 0.4rem 0.8rem;" onclick="openBuyModal(${s.id}, '${s.symbol}', ${s.currentPrice})">Buy</button>
                    </td>
                `;
                tbody.appendChild(tr);

                // Render Sparkline
                // Since we don't have historical data, we simulate a line that ends at the current price
                // with the appropriate slope based on the change.
                renderSparkline(s.id, s.currentPrice, change);
            }
        }
    } catch (e) {
        console.error(e);
    }
}

function renderSparkline(stockId, currentPrice, changePercent) {
    // Simulate past data points to create a "trend" line
    // If change is positive, ensure the graph ends higher than it starts.
    const ctx = document.getElementById(`chart-${stockId}`).getContext('2d');

    // Generate 10 fake points
    const points = [];
    const volatility = currentPrice * 0.02; // 2% volatility simulation

    // Starting point roughly based on change
    // If change is +10%, start roughly 10% lower.
    let startPrice = currentPrice / (1 + (changePercent / 100));

    if (changePercent === 0) startPrice = currentPrice;

    // Generate points between start and current
    for (let i = 0; i < 9; i++) {
        // Linear interpolation + noise
        const progress = i / 9;
        const trend = startPrice + (currentPrice - startPrice) * progress;
        const noise = (Math.random() - 0.5) * volatility;
        points.push(trend + noise);
    }
    points.push(currentPrice); // Ensure last point is real

    const color = changePercent >= 0 ? '#4cc9f0' : '#f72585'; // Blue/Cyan for profit, Pink/Red for loss (Using theme colors if possible, else green/red)
    // Actually user asked for Green/Red.
    const realColor = changePercent >= 0 ? '#2ecc71' : '#e74c3c';

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
            datasets: [{
                data: points,
                borderColor: realColor,
                borderWidth: 2,
                pointRadius: 0,
                fill: false,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false }, tooltip: { enabled: false } },
            scales: {
                x: { display: false },
                y: { display: false }
            },
            layout: { padding: 0 }
        }
    });
}


async function handleCreateMarketStock(e) {
    e.preventDefault();
    const stock = {
        symbol: document.getElementById('s-symbol').value,
        companyName: document.getElementById('s-company').value,
        currentPrice: parseFloat(document.getElementById('s-price').value),
        quantity: 0 // Market listing, quantity is infinite/irrelevant
    };

    try {
        const res = await fetch(`${API_BASE}/stocks`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(stock)
        });
        if (res.ok) {
            closeModal('create-stock-modal');
            loadMarketStocks();
        }
    } catch (e) { alert("Error adding stock"); }
}

// --- Buy / Sell Logic ---
async function openBuyModal(stockId, symbol, price) {
    await loadPortfolios(false); // Refresh portfolios for dropdown
    const select = document.getElementById('buy-portfolio-select');
    select.innerHTML = portfolios.map(p => `<option value="${p.id}">${p.name}</option>`).join('');

    document.getElementById('buy-stock-id').value = stockId;
    document.getElementById('buy-stock-symbol').textContent = symbol;
    document.getElementById('buy-stock-price').value = price;
    document.getElementById('buy-quantity').value = 1;
    updateBuyTotal();

    openModal('buy-stock-modal');
}

function updateBuyTotal() {
    const qty = document.getElementById('buy-quantity').value;
    const price = document.getElementById('buy-stock-price').value;
    document.getElementById('buy-total-cost').textContent = formatCurrency(qty * price);
}

async function handleBuyStock(e) {
    e.preventDefault();
    const stockId = document.getElementById('buy-stock-id').value;
    const portfolioId = document.getElementById('buy-portfolio-select').value;
    const qty = parseFloat(document.getElementById('buy-quantity').value);
    const price = parseFloat(document.getElementById('buy-stock-price').value);

    const transaction = {
        portfolioId: parseInt(portfolioId),
        stockId: parseInt(stockId),
        transactionType: 'BUY',
        amount: qty,
        pricePerUnit: price,
        transactionDate: null // Backend will set date
    };

    await postTransaction(transaction, 'buy-stock-modal');
}

function openSellModal(portfolioId, stockId, symbol, maxQty, currentPrice) {
    document.getElementById('sell-portfolio-id').value = portfolioId;
    document.getElementById('sell-stock-id').value = stockId;
    document.getElementById('sell-stock-symbol').textContent = symbol;
    document.getElementById('sell-max-qty').textContent = maxQty;
    document.getElementById('sell-quantity').max = maxQty;
    document.getElementById('sell-quantity').value = 1;
    document.getElementById('sell-current-price').value = currentPrice;

    updateSellTotal();
    openModal('sell-stock-modal');
}

function updateSellTotal() {
    const qty = document.getElementById('sell-quantity').value;
    const price = document.getElementById('sell-current-price').value;
    document.getElementById('sell-total-value').textContent = formatCurrency(qty * price);
}

async function handleSellStock(e) {
    e.preventDefault();
    const portfolioId = document.getElementById('sell-portfolio-id').value;
    const stockId = document.getElementById('sell-stock-id').value;
    const qty = parseFloat(document.getElementById('sell-quantity').value);
    const price = parseFloat(document.getElementById('sell-current-price').value);
    const max = parseFloat(document.getElementById('sell-quantity').max);

    if (qty > max) {
        alert("Cannot sell more than you own!");
        return;
    }

    const transaction = {
        portfolioId: parseInt(portfolioId),
        stockId: parseInt(stockId),
        transactionType: 'SELL',
        amount: qty,
        pricePerUnit: price,
        transactionDate: null // Backend will set date
    };

    await postTransaction(transaction, 'sell-stock-modal');
    // Refresh the portfolio view
    const portfolio = portfolios.find(p => p.id == portfolioId);
    viewPortfolioDetails(portfolioId, portfolio.name);
}

async function postTransaction(transaction, modalId) {
    try {
        const res = await fetch(`${API_BASE}/transactions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(transaction)
        });
        if (res.ok) {
            closeModal(modalId);
            // If buying, maybe show success or redirect?
            if (transaction.transactionType === 'BUY') {
                alert("Purchase Successful!");
                showSection('portfolios');
            } else {
                alert("Sale Successful!");
            }
        } else {
            alert("Transaction failed.");
        }
    } catch (e) { console.error(e); }
}

// --- Demo Data ---
async function seedDemoData() {
    if (!confirm("Create demo data? This will add portfolios and stocks.")) return;

    try {
        // 1. Create Portfolios
        const p1Res = await fetch(`${API_BASE}/portfolios`, {
            method: 'POST', body: JSON.stringify({ name: 'Tech Growth', description: 'High risk tech stocks' }), headers: { 'Content-Type': 'application/json' }
        });
        const p2Res = await fetch(`${API_BASE}/portfolios`, {
            method: 'POST', body: JSON.stringify({ name: 'Retirement Fund', description: 'Safe dividend stocks' }), headers: { 'Content-Type': 'application/json' }
        });
        const p1 = await p1Res.json();
        const p2 = await p2Res.json();

        // 2. Create Market Stocks
        const stocks = [
            { symbol: 'AAPL', companyName: 'Apple Inc', currentPrice: 15000, quantity: 0 },
            { symbol: 'TSLA', companyName: 'Tesla Inc', currentPrice: 18500, quantity: 0 },
            { symbol: 'MSFT', companyName: 'Microsoft', currentPrice: 25000, quantity: 0 },
            { symbol: 'TATA', companyName: 'Tata Motors', currentPrice: 850, quantity: 0 }
        ];

        let createdStocks = [];
        for (let s of stocks) {
            const res = await fetch(`${API_BASE}/stocks`, {
                method: 'POST', body: JSON.stringify(s), headers: { 'Content-Type': 'application/json' }
            });
            createdStocks.push(await res.json());
        }

        // 3. Create Transactions (Buy stocks for portfolios)
        // Buy AAPL for Tech
        await fetch(`${API_BASE}/transactions`, {
            method: 'POST', body: JSON.stringify({ portfolioId: p1.id, stockId: createdStocks[0].id, transactionType: 'BUY', amount: 10, pricePerUnit: createdStocks[0].currentPrice, transactionDate: null }), headers: { 'Content-Type': 'application/json' }
        });
        // Buy TSLA for Tech
        await fetch(`${API_BASE}/transactions`, {
            method: 'POST', body: JSON.stringify({ portfolioId: p1.id, stockId: createdStocks[1].id, transactionType: 'BUY', amount: 5, pricePerUnit: createdStocks[1].currentPrice, transactionDate: null }), headers: { 'Content-Type': 'application/json' }
        });
        // Buy TATA for Retirement
        await fetch(`${API_BASE}/transactions`, {
            method: 'POST', body: JSON.stringify({ portfolioId: p2.id, stockId: createdStocks[3].id, transactionType: 'BUY', amount: 100, pricePerUnit: createdStocks[3].currentPrice, transactionDate: null }), headers: { 'Content-Type': 'application/json' }
        });

        alert("Demo Data Created! Reloading...");
        location.reload();

    } catch (e) {
        console.error(e);
        alert("Error seeding data.");
    }
}

// --- Chatbot ---
function toggleChat() {
    const widget = document.getElementById('chat-widget');
    const toggleIcon = document.getElementById('chat-toggle-icon');

    if (widget.classList.contains('collapsed')) {
        widget.classList.remove('collapsed');
        toggleIcon.classList.remove('fa-chevron-up');
        toggleIcon.classList.add('fa-chevron-down');
    } else {
        widget.classList.add('collapsed');
        toggleIcon.classList.remove('fa-chevron-down');
        toggleIcon.classList.add('fa-chevron-up');
    }
}

async function sendMessage() {
    const input = document.getElementById('chat-input');
    const message = input.value.trim();
    if (!message) return;

    addChatMessage('user', message);
    input.value = '';
    addChatMessage('bot', "Thinking...");

    try {
        let reply = "";
        if (OPENAI_API_KEY === 'Sid_123') {
            await new Promise(r => setTimeout(r, 1000));
            // Enhanced Mock Response with real portfolio data
            const totalValue = document.getElementById('dashboard-total-value').textContent;
            reply = `I see you have ${portfolios.length} portfolios worth ${totalValue}. Based on market trends, tech stocks are performing well. How can I help you manage your risk?`;
        } else {
            // Real Call Logic would go here
            reply = "I am a simulated AI.";
        }
        const chatBody = document.getElementById('chat-body');
        chatBody.removeChild(chatBody.lastElementChild);
        addChatMessage('bot', reply);
    } catch (error) {
        const chatBody = document.getElementById('chat-body');
        chatBody.removeChild(chatBody.lastElementChild);
        addChatMessage('bot', "Sorry, I encountered an error connecting to the AI.");
    }
}

function addChatMessage(sender, text) {
    const chatBody = document.getElementById('chat-body');
    const div = document.createElement('div');
    div.classList.add('chat-message', sender);
    div.textContent = text;
    chatBody.appendChild(div);
    chatBody.scrollTop = chatBody.scrollHeight;
}

// --- Stock Details & History ---
let currentDetailSymbol = null;
let historyChartInstance = null;

function openStockDetails(stockId) {
    const stock = marketStocks.find(s => s.id === stockId);
    if (!stock) return;

    currentDetailSymbol = stock.symbol;

    // Populate Header
    document.getElementById('sd-symbol').textContent = stock.symbol;
    document.getElementById('sd-company').textContent = stock.companyName;
    document.getElementById('sd-price').textContent = formatCurrency(stock.currentPrice);

    const change = stock.changePercent || 0;
    const changeEl = document.getElementById('sd-change');
    changeEl.textContent = (change >= 0 ? '+' : '') + change.toFixed(2) + '%';
    changeEl.style.color = change >= 0 ? 'var(--success-color)' : 'var(--danger-color)';

    openModal('stock-details-modal');

    // Increased delay to 350ms to ensure modal animation completes
    setTimeout(() => {
        updateChartRange('1D');
    }, 350);
}

async function updateChartRange(range) {
    if (!currentDetailSymbol) return;

    const canvas = document.getElementById('historyChart');
    if (!canvas) {
        console.error("Canvas element not found!");
        return;
    }
    const ctx = canvas.getContext('2d');

    // Destroy previous chart
    if (historyChartInstance) {
        historyChartInstance.destroy();
    }

    try {
        const res = await fetch(`${API_BASE}/stocks/history/${currentDetailSymbol}/${range}`);

        let historyMap = {};
        if (res.ok) {
            historyMap = await res.json();
        } else {
            console.error("Failed to fetch history, using fallback.");
        }

        // Final check on dates
        const dates = Object.keys(historyMap);

        if (dates.length === 0) {
            console.warn("No data for chart.");
            return;
        }

        const labels = dates;
        const prices = dates.map(d => historyMap[d]);

        // Color based on trend (First vs Last)
        const first = prices[0];
        const last = prices[prices.length - 1];
        const color = last >= first ? '#2ecc71' : '#e74c3c';

        historyChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Price',
                    data: prices,
                    borderColor: color,
                    backgroundColor: color + '33',
                    fill: true,
                    tension: 0.1,
                    pointRadius: (range === '1D' || range === '1M') ? 0 : 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        display: true,
                        ticks: { maxTicksLimit: 6, color: '#778da9' },
                        grid: { display: false }
                    },
                    y: {
                        display: true,
                        grid: { color: 'rgba(255,255,255,0.1)' },
                        ticks: { color: '#778da9' }
                    }
                },
                plugins: {
                    legend: { display: false },
                    tooltip: { mode: 'index', intersect: false }
                },
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                }
            }
        });

    } catch (e) {
        console.error("History Error", e);
    }
}

// --- Utils ---
function formatCurrency(num) {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(num);
}

function openModal(id) {
    document.getElementById(id).style.display = 'flex';
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}
