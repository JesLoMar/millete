#!/usr/bin/env node
/**
 * ============================================================================
 * MILLETE - GENERADOR DE JSON MASIVO STANDALONE (v5.1)
 * ============================================================================
 * Genera datos de prueba sin necesidad de export original.
 * Fix: solo 1 goalMember por goalUnit (backend sanitiza todos al mismo userId).
 * ============================================================================
 */

import { writeFileSync } from "fs";
import { randomUUID } from "crypto";

const OUTPUT = process.argv[2] || "millete_massive.json";
const USER_ID = process.argv[3] || "820d4a6f-fd98-447f-a9d3-3d544567bbcd";

const CONFIG = {
  categories: 32,
  transactions: 505,
  plannedTransactions: 20,
  investments: 22,
  savingsGoals: 11,
  goalUnits: 6,
  goalContributionsPerUnit: 15,
};

const now = new Date();
const yearAgo = new Date(now.getTime() - 365 * 24 * 60 * 60 * 1000);

function pad(n) { return String(n).padStart(2, "0"); }
function fmtLocalDate(d) { return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`; }
function fmtDateTime(d) { return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`; }
function fmtTimestamp(d) {
  const ms = String(d.getMilliseconds()).padStart(3, "0");
  const us = String(randInt(0, 999)).padStart(3, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${ms}${us}`;
}
function randInt(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
function randFloat(min, max, decimals = 2) { return parseFloat((Math.random() * (max - min) + min).toFixed(decimals)); }
function randDate(start, end) { return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime())); }

const COLORS = [
  "#ef4444", "#f97316", "#f59e0b", "#84cc16", "#10b981", "#06b6d4",
  "#3b82f6", "#6366f1", "#8b5cf6", "#d946ef", "#f43f5e", "#fb7185",
  "#fda4af", "#fb923c", "#fcd34d", "#bef264", "#6ee7b7", "#67e8f9",
  "#93c5fd", "#a5b4fc", "#c4b5fd", "#e879f9", "#fdba74", "#d9f99d",
  "#99f6e4", "#bfdbfe", "#ddd6fe", "#fbcfe8", "#fecdd3", "#fed7aa",
  "#991B1B", "#10B981", "#064E3B", "#FEF3C7", "#C2410C",
];

const CAT_NAMES = [
  "Comida", "Transporte", "Ocio", "Salud", "Educación", "Vivienda",
  "Servicios", "Ropa", "Tecnología", "Regalos", "Viajes", "Mascotas",
  "Hogar", "Coche", "Ahorro", "Inversiones", "Freelance", "Consultoría",
  "Dividendos", "Alquiler", "Facturas", "Restaurantes", "Supermercado",
  "Gasolina", "Suscripciones", "Deporte", "Belleza", "Seguros",
  "Imprevistos", "Hobbies", "Café", "Bar", "Libros", "Música", "Jardín",
];

const ASSETS = [
  ["Apple", "AAPL"], ["Tesla", "TSLA"], ["Bitcoin", "BTC"],
  ["Ethereum", "ETH"], ["S&P 500", "SPY"], ["Microsoft", "MSFT"],
  ["Amazon", "AMZN"], ["Google", "GOOGL"], ["NVIDIA", "NVDA"],
  ["Meta", "META"], ["Netflix", "NFLX"], ["AMD", "AMD"],
  ["Intel", "INTC"], ["Coca-Cola", "KO"], ["Pfizer", "PFE"],
  ["JPMorgan", "JPM"], ["Visa", "V"], ["Mastercard", "MA"],
  ["Disney", "DIS"], ["McDonald's", "MCD"],
];

const INV_TYPES = ["STOCK", "CRYPTO", "FUND", "REAL_ESTATE", "OTHER"];
const FREQ_TYPES = ["DAYS", "WEEKS", "MONTHS", "YEARS"];
const PRIORITIES = ["LOW", "MEDIUM", "HIGH"];
const GOAL_STATUSES = ["ACTIVE", "PAUSED", "COMPLETED", "CANCELLED"];
const DIST_MODES = ["EQUITATIVE", "PROPORTIONAL", "CUSTOM"];

function generateCategory(idx) {
  const created = randDate(yearAgo, now);
  return {
    active: true,
    budgetLimit: Math.random() > 0.3 ? randFloat(50, 1000) : null,
    color: COLORS[idx % COLORS.length],
    createdAt: fmtTimestamp(created),
    id: randomUUID(),
    modifiedAt: fmtTimestamp(created),
    name: CAT_NAMES[idx % CAT_NAMES.length] + (idx >= CAT_NAMES.length ? ` #${Math.floor(idx / CAT_NAMES.length) + 1}` : ""),
    userId: USER_ID,
  };
}

function generateTransaction(categoryIds, idx) {
  const type = Math.random() > 0.35 ? "EXPENSE" : "INCOME";
  const amount = randFloat(5, 500);
  const date = randDate(yearAgo, now);
  const created = randDate(yearAgo, now);
  return {
    id: randomUUID(),
    userId: USER_ID,
    categoryId: categoryIds[randInt(0, categoryIds.length - 1)],
    amount,
    date: fmtDateTime(date),
    type,
    description: `Transacción de prueba #${idx + 1}`,
    createdAt: fmtTimestamp(created),
    modifiedAt: fmtTimestamp(created),
    active: true,
  };
}

function generatePlannedTransaction(categoryIds, idx) {
  const type = Math.random() > 0.4 ? "EXPENSE" : "INCOME";
  const start = randDate(yearAgo, now);
  const end = Math.random() > 0.5 ? new Date(start.getTime() + randInt(30, 365) * 24 * 60 * 60 * 1000) : null;
  const created = randDate(yearAgo, now);
  const lastExec = Math.random() > 0.5 ? randDate(start, now) : null;
  return {
    id: randomUUID(),
    userId: USER_ID,
    categoryId: categoryIds[randInt(0, categoryIds.length - 1)],
    amount: randFloat(20, 300),
    type,
    description: `Recurrente #${idx + 1}`,
    frequencyType: FREQ_TYPES[randInt(0, FREQ_TYPES.length - 1)],
    frequencyInterval: randInt(1, 4),
    startDate: fmtLocalDate(start),
    endDate: end ? fmtLocalDate(end) : null,
    createdAt: fmtTimestamp(created),
    modifiedAt: fmtTimestamp(created),
    active: true,
    lastExecutedDate: lastExec ? fmtLocalDate(lastExec) : null,
  };
}

function generateInvestment(idx) {
  const [assetName, ticker] = ASSETS[idx % ASSETS.length];
  const qty = randFloat(0.001, 100, 8);
  const purchasePrice = randFloat(10, 60000);
  const currentPrice = purchasePrice * randFloat(0.5, 2.0);
  const invested = parseFloat((qty * purchasePrice).toFixed(2));
  const currentValue = parseFloat((qty * currentPrice).toFixed(2));
  const profitOrLoss = parseFloat((currentValue - invested).toFixed(2));
  const roi = invested > 0 ? parseFloat(((profitOrLoss / invested) * 100).toFixed(2)) : 0;
  const purchaseDate = randDate(yearAgo, now);
  const created = randDate(yearAgo, now);
  return {
    id: randomUUID(),
    userId: USER_ID,
    assetName,
    ticker,
    quantity: qty,
    purchasePrice,
    currentPrice,
    type: INV_TYPES[randInt(0, INV_TYPES.length - 1)],
    purchaseDate: fmtDateTime(purchaseDate),
    createdAt: fmtTimestamp(created),
    modifiedAt: fmtTimestamp(created),
    active: true,
    currentValue,
    investedCapital: invested,
    profitOrLoss,
    returnOnInvestmentPercentage: roi,
  };
}

function generateSavingsGoal(idx) {
  const names = [
    "Viaje a Japón", "Coche nuevo", "Fondo de emergencia", "Entrada piso",
    "Boda", "Master", "Renovar cocina", "Nuevo portátil",
    "Bicicleta eléctrica", "Invertir 10K",
  ];
  const target = randFloat(500, 20000);
  const created = randDate(yearAgo, now);
  const deadline = Math.random() > 0.3 ? new Date(now.getTime() + randInt(30, 730) * 24 * 60 * 60 * 1000) : null;
  return {
    id: randomUUID(),
    userId: USER_ID,
    name: names[idx % names.length] + (idx >= names.length ? ` #${Math.floor(idx / names.length) + 1}` : ""),
    targetAmount: target,
    currentAmount: randFloat(0, target * 0.7),
    deadline: deadline ? fmtLocalDate(deadline) : null,
    priority: PRIORITIES[randInt(0, PRIORITIES.length - 1)],
    status: GOAL_STATUSES[randInt(0, GOAL_STATUSES.length - 1)],
    link: null,
    createdAt: fmtTimestamp(created),
    modifiedAt: fmtTimestamp(created),
    active: true,
  };
}

function generateGoalUnit(idx) {
  const names = ["Viaje", "Regalo", "Proyecto", "Fiesta", "Reunión"];
  const created = randDate(yearAgo, now);
  return {
    active: true,
    createdAt: fmtTimestamp(created),
    distributionMode: DIST_MODES[randInt(0, DIST_MODES.length - 1)],
    id: randomUUID(),
    members: [],
    modifiedAt: fmtTimestamp(created),
    monthlyTarget: randFloat(100, 1000),
    name: `Grupo #${idx + 1} - ${names[idx % names.length]}`,
  };
}

function generateGoalMember(goalUnit) {
  const created = randDate(yearAgo, now);
  return {
    active: true,
    admin: true,
    createdAt: fmtTimestamp(created),
    customPercentage: goalUnit.distributionMode === "CUSTOM" ? randFloat(5, 95) : null,
    goalId: goalUnit.id,
    id: randomUUID(),
    joinedAt: fmtTimestamp(created),
    modifiedAt: fmtTimestamp(created),
    role: "ADMIN",
    salary: randFloat(800, 5000),
    userId: USER_ID,
  };
}

function generateGoalContribution(goalUnit, idx) {
  const created = randDate(yearAgo, now);
  return {
    id: randomUUID(),
    goalId: goalUnit.id,
    userId: Math.random() > 0.5 ? USER_ID : randomUUID(),
    amount: randFloat(10, 200),
    date: fmtDateTime(created),
    createdAt: fmtTimestamp(created),
    modifiedAt: fmtTimestamp(created),
    active: true,
  };
}

// =============================================================================
// MAIN
// =============================================================================

console.log(`👤 Generando datos para usuario: ${USER_ID}`);

const categories = [];
const transactions = [];
const plannedTransactions = [];
const investments = [];
const savingsGoals = [];
const goalUnits = [];
const goalMembers = [];
const goalContributions = [];

for (let i = 0; i < CONFIG.categories; i++) categories.push(generateCategory(i));
const categoryIds = categories.map(c => c.id);

for (let i = 0; i < CONFIG.transactions; i++) transactions.push(generateTransaction(categoryIds, i));
for (let i = 0; i < CONFIG.plannedTransactions; i++) plannedTransactions.push(generatePlannedTransaction(categoryIds, i));
for (let i = 0; i < CONFIG.investments; i++) investments.push(generateInvestment(i));
for (let i = 0; i < CONFIG.savingsGoals; i++) savingsGoals.push(generateSavingsGoal(i));

for (let i = 0; i < CONFIG.goalUnits; i++) {
  const unit = generateGoalUnit(i);
  goalUnits.push(unit);
  // Solo 1 miembro (admin) por goalUnit. El backend sanitiza TODOS los
  // userIds al usuario logueado, así que múltiples miembros causarían
  // violación de uq_goal_user.
  goalMembers.push(generateGoalMember(unit));
  for (let c = 0; c < CONFIG.goalContributionsPerUnit; c++) {
    goalContributions.push(generateGoalContribution(unit, c));
  }
}

const snapshot = {
  metadata: {
    version: "0.1.0",
    exportDate: fmtTimestamp(now),
    appVersion: "0.1.2",
  },
  categories,
  transactions,
  plannedTransactions,
  investments,
  savingsGoals,
  userPreferences: null,
  goalUnits,
  goalMembers,
  goalContributions,
};

writeFileSync(OUTPUT, JSON.stringify(snapshot, null, 2), "utf-8");

const stats = {
  categories: categories.length,
  transactions: transactions.length,
  plannedTransactions: plannedTransactions.length,
  investments: investments.length,
  savingsGoals: savingsGoals.length,
  goalUnits: goalUnits.length,
  goalMembers: goalMembers.length,
  goalContributions: goalContributions.length,
};

console.log("\n✅ JSON masivo generado:");
console.log(`   Archivo: ${OUTPUT}`);
console.log(`   Tamaño:  ${(Buffer.byteLength(JSON.stringify(snapshot)) / 1024).toFixed(1)} KB`);
console.log("\n📊 Resumen de registros:");
for (const [k, v] of Object.entries(stats)) {
  console.log(`   ${k.padEnd(22)} ${String(v).padStart(5)}`);
}

console.log(`\n📤 Para importar: Dashboard → Importar datos → Seleccionar ${OUTPUT}`);
