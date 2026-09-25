#!/usr/bin/env python3
"""Adapta tests residuales de dashboard/dataexport/categories a LocalDate/Instant."""
import re, pathlib

BASE = pathlib.Path("/workspace/backend/src/test/java/com/puntomartinez/millete")

FILES = sorted(
    list((BASE / "dashboard").rglob("*.java")) +
    list((BASE / "dataexport").rglob("*.java")) +
    [BASE / "categories/domain/model/CategoryTest.java"]
)

FIXEDTIME_IMPORT = "import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;"
TIMEPROVIDER_IMPORT = "import com.puntomartinez.millete.shared.domain.time.TimeProvider;"
INSTANT_IMPORT = "import java.time.Instant;"
LOCALDATE_IMPORT = "import java.time.LocalDate;"
LDT_IMPORT = "import java.time.LocalDateTime;"


def ensure_import(src, imp):
    if imp in src:
        return src
    lines = src.splitlines(keepends=True)
    last_imp = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last_imp + 1, imp + "\n")
    return "".join(lines)


def add_time_field(src):
    if "FixedTimeProvider TIME" in src:
        return src
    m = re.search(r"(public class \w+Test \{\n)", src) or re.search(r"(class \w+Test \{\n)", src)
    if not m:
        return src
    ins = m.group(1) + "    static final TimeProvider TIME = new FixedTimeProvider(\n            Instant.parse(\"2024-01-15T10:00:00Z\"));\n\n"
    return src.replace(m.group(0), ins, 1)


for path in FILES:
    src = path.read_text()
    orig = src

    # 1. Literales LocalDateTime.of(...) -> Instant.parse(...)
    def lit_repl(m):
        y, mo, d, h, mi = (m.group(i) or "0" for i in range(1, 6))
        return f'Instant.parse("{y}-{int(mo):02d}-{int(d):02d}T{int(h):02d}:{int(mi):02d}:00Z")'
    src = re.sub(r"LocalDateTime\.of\((\d{4}),\s*(\d+),\s*(\d+)(?:,\s*(\d+)(?:,\s*(\d+))?)?\)", lit_repl, src)

    # 2. Variables locales de fecha -> LocalDate
    src = src.replace("LocalDateTime date = LocalDateTime.now();", "LocalDate date = LocalDate.now();")
    src = src.replace("LocalDateTime start = LocalDateTime.now()", "LocalDate start = LocalDate.now()")
    src = src.replace("LocalDateTime end = LocalDateTime.now()", "LocalDate end = LocalDate.now()")
    src = src.replace("LocalDateTime todayStart = today.atStartOfDay();\n", "")
    src = re.sub(r"\btodayStart\b", "today", src)

    # 3. Rangos -> LocalDate[]
    src = re.sub(r"LocalDateTime\[\] (\w+) = \{", r"LocalDate[] \1 = {", src)
    src = src.replace("LocalDateTime[] result = service.getDateRange", "LocalDate[] result = service.getDateRange")
    src = src.replace("LocalDateTime[] result = service.getPreviousPeriod", "LocalDate[] result = service.getPreviousPeriod")

    # 4. Contenido de arrays de rango y llamadas now()->minus/plus en contexto de fecha -> LocalDate
    src = src.replace("LocalDateTime.now().minusDays(", "LocalDate.now().minusDays(")
    src = src.replace("LocalDateTime.now().minusMonths(", "LocalDate.now().minusMonths(")
    src = src.replace("LocalDateTime.now().minusWeeks(", "LocalDate.now().minusWeeks(")
    src = src.replace("LocalDateTime.now().minusYears(", "LocalDate.now().minusYears(")
    src = src.replace("LocalDateTime.now().withDayOfYear(", "LocalDate.now().withDayOfYear(")
    src = src.replace("LocalDateTime.now().withDayOfMonth(", "LocalDate.now().withDayOfMonth(")
    src = src.replace("LocalDateTime.now().with(", "LocalDate.now().with(")
    src = src.replace("LocalDateTime.now().plusDays(", "LocalDate.now().plusDays(")
    src = src.replace("LocalDateTime.now().truncatedTo(", "LocalDate.now().truncatedTo(")
    src = src.replace("new TransactionQueryPort.TransactionData(\n", "new TransactionQueryPort.TransactionData(\n")

    # 5. Mocks any(LocalDateTime.class) en Between -> any(LocalDate.class)
    src = re.sub(r"findByUserIdAndDateBetween\(([^)]*?), any\(LocalDateTime\.class\), any\(LocalDateTime\.class\)\)",
                 r"findByUserIdAndDateBetween(\1, any(LocalDate.class), any(LocalDate.class))", src)

    # 6. Setters de auditoria -> Instant
    src = re.sub(r"setCreatedAt\(LocalDateTime\.now\(\)\)", "setCreatedAt(Instant.now())", src)
    src = re.sub(r"setModifiedAt\(LocalDateTime\.now\(\)\)", "setModifiedAt(Instant.now())", src)

    # 7. Quedan LocalDateTime.now() sueltos: si es arg de date (posicion tras amount) no lo sabemos aqui;
    #    los marcamos para revision manual despues del compile.
    if "LocalDateTime" in re.sub(LDT_IMPORT, "", src):
        print(f"[MANUAL] {path.relative_to(BASE.parent)}: aun usa LocalDateTime")

    # 8. Limpieza de import si ya no se usa
    body_wo_import = re.sub(LDT_IMPORT, "", src)
    if "LocalDateTime" not in body_wo_import:
        src = src.replace(LDT_IMPORT + "\n", "")
        if "LocalDate" in src and LOCALDATE_IMPORT not in src:
            src = ensure_import(src, LOCALDATE_IMPORT)
        if "Instant." in src and INSTANT_IMPORT not in src:
            src = ensure_import(src, INSTANT_IMPORT)

    if src != orig:
        path.write_text(src)
        print(f"[OK] {path.relative_to(BASE.parent)}")

print("done")
