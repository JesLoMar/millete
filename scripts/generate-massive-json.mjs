#!/usr/bin/env node
/**
 * ============================================================================
 * MILLETE - GENERADOR DE JSON MASIVO DESDE EXPORT REAL (v5)
 * ============================================================================
 * Correcciones aplicadas:
 *   - LocalDate     → "YYYY-MM-DD"
 *   - LocalDateTime → "YYYY-MM-DDTHH:MM:SS"
 *   - Timestamp     → "YYYY-MM-DDTHH:MM:SS.microsegundos"
 *   - Amounts       → SIEMPRE positivos (Transaction constructor exige > 0)
 *   - GoalMembers   → Solo 1 por goalUnit (backend sanitiza todos al mismo userId)
 * ============================================================================
 */

import { readFileSync, writeFileSync } from "fs";
import { randomUUID } from "crypto";

const INPUT = process.argv[2] || "C:/Users/Chus/Downloads/familybudget_export.json";
const OUTPUT = process.argv[3] || "millete_massive.json";

const CONFIG = {
  categories: 30,
  transactions: 500,
  plannedTransactions: 20,
  investments: 20,
  savingsGoals: 10,
  goalUnits: 5,
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

function generateCategory(userId, idx) {
  const created = randDate(yearAgo, now);
  return {
    active: true,
    budgetLimit: Math.random() > 0.3 ? randFloat(50, 1000) : null,
    color: COLORS[idx % COLORS.length],
    createdAt: fmtTimestamp(created),
    id: randomUUID(),
    modifiedAt: fmtTimestamp(created),
    name: CAT_NAMES[idx % CAT_NAMES.length] + (idx >= CAT_NAMES.length ? ` #${Math.floor(idx / CAT_NAMES.length) + 1}` : ""),
    userId,
  };
}

function generateTransaction(userId, categoryIds, idx) {
  const type = Math.random() > 0.35 ? "EXPENSE" : "INCOME";
  const amount = randFloat(5, 500);
  const date = randDate(yearAgo, now);
  const created = randDate(yearAgo, now);
  return {
    id: randomUUID(),
    userId,
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

function generatePlannedTransaction(userId, categoryIds, idx) {
  const type = Math.random() > 0.4 ? "EXPENSE" : "INCOME";
  const start = randDate(yearAgo, now);
  const end = Math.random() > 0.5 ? new Date(start.getTime() + randInt(30, 365) * 24 * 60 * 60 * 1000) : null;
  const created = randDate(yearAgo, now);
  const lastExec = Math.random() > 0.5 ? randDate(start, now) : null;
  return {
    id: randomUUID(),
    userId,
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

function generateInvestment(userId, idx) {
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
    userId,
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

function generateSavingsGoal(userId, idx) {
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
    userId,
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

function generateGoalMember(goalUnit, userId, isAdmin) {
  const created = randDate(yearAgo, now);
  return {
    active: true,
    admin: isAdmin,
    createdAt: fmtTimestamp(created),
    customPercentage: goalUnit.distributionMode === "CUSTOM" ? randFloat(5, 95) : null,
    goalId: goalUnit.id,
    id: randomUUID(),
    joinedAt: fmtTimestamp(created),
    modifiedAt: fmtTimestamp(created),
    role: isAdmin ? "ADMIN" : "MEMBER",
    salary: randFloat(800, 5000),
    userId,
  };
}

function generateGoalContribution(goalUnit, userId, idx) {
  const created = randDate(yearAgo, now);
  return {
    id: randomUUID(),
    goalId: goalUnit.id,
    userId,
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

console.log(`📖 Leyendo export original: ${INPUT}`);
const raw = readFileSync(INPUT, "utf-8");
const original = JSON.parse(raw);

const userId = original.categories?.[0]?.userId || original.transactions?.[0]?.userId;
if (!userId) {
  console.error("❌ No se pudo determinar el userId del export. ¿Está vacío?");
  process.exit(1);
}

console.log(`👤 User ID detectado: ${userId}`);

// Copiar datos originales y sanitizar amounts negativos
const categories = [...(original.categories || [])];
const transactions = [...(original.transactions || [])];
for (const tx of transactions) { if (tx.amount < 0) tx.amount = Math.abs(tx.amount); }

const plannedTransactions = [...(original.plannedTransactions || [])];
for (const ptx of plannedTransactions) { if (ptx.amount < 0) ptx.amount = Math.abs(ptx.amount); }

const investments = [...(original.investments || [])];
const savingsGoals = [...(original.savingsGoals || [])];
const goalUnits = [...(original.goalUnits || [])];

// FIX: El backend sanitiza TODOS los goalMembers al userId logueado.
// Si hay múltiples miembros para la misma meta, el (goalId, userId) se duplica
// y viola la constraint uq_goal_user. Solo conservamos 1 miembro por goalUnit.
const goalMembersByGoal = new Map();
for (const gm of (original.goalMembers || [])) {
  const existing = goalMembersByGoal.get(gm.goalId);
  if (!existing || gm.admin || gm.userId === userId) {
    goalMembersByGoal.set(gm.goalId, gm);
  }
}
const goalMembers = [...goalMembersByGoal.values()];

const goalContributions = [...(original.goalContributions || [])];

// Generar datos nuevos
for (let i = 0; i < CONFIG.categories; i++) categories.push(generateCategory(userId, i));
const categoryIds = categories.map(c => c.id);

for (let i = 0; i < CONFIG.transactions; i++) transactions.push(generateTransaction(userId, categoryIds, i));
for (let i = 0; i < CONFIG.plannedTransactions; i++) plannedTransactions.push(generatePlannedTransaction(userId, categoryIds, i));
for (let i = 0; i < CONFIG.investments; i++) investments.push(generateInvestment(userId, i));
for (let i = 0; i < CONFIG.savingsGoals; i++) savingsGoals.push(generateSavingsGoal(userId, i));

for (let i = 0; i < CONFIG.goalUnits; i++) {
  const unit = generateGoalUnit(i);
  goalUnits.push(unit);
  // FIX: Solo 1 miembro (admin) por goalUnit. El backend sanitiza TODOS los
  // userIds al usuario logueado, así que múltiples miembros causarían
  // violación de uq_goal_user (goal_id, user_id duplicado).
  goalMembers.push(generateGoalMember(unit, userId, true));
  for (let c = 0; c < CONFIG.goalContributionsPerUnit; c++) {
    goalContributions.push(generateGoalContribution(unit, Math.random() > 0.5 ? userId : randomUUID(), c));
  }
}

const snapshot = {
  metadata: {
    version: original.metadata?.version || "0.1.0",
    exportDate: fmtTimestamp(now),
    appVersion: original.metadata?.appVersion || "0.1.2",
  },
  categories,
  transactions,
  plannedTransactions,
  investments,
  savingsGoals,
  userPreferences: original.userPreferences || null,
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
console.log("\n📊 Resumen de registros totales:");
for (const [k, v] of Object.entries(stats)) {
  const originalCount = Array.isArray(original[k]) ? original[k].length : (original[k] === null ? 0 : 1);
  console.log(`   ${k.padEnd(22)} ${String(v).padStart(5)}  (orig: ${originalCount}, +${v - originalCount})`);
}

console.log(`\n📤 Para importar: Dashboard → Importar datos → Seleccionar ${OUTPUT}`);
