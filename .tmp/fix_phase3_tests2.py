#!/usr/bin/env python3
import re, pathlib

BASE = pathlib.Path("/workspace/backend/src/test/java/com/puntomartinez/millete")
TP_IMPORT = "import com.puntomartinez.millete.shared.domain.time.TimeProvider;"
FT_IMPORT = "import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;"
INSTANT_IMPORT = "import java.time.Instant;"

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
    m = re.search(r"(class \w+Test \{\n)", src)
    if not m:
        return src
    ins = m.group(1) + "    static final TimeProvider TIME = new FixedTimeProvider(\n            Instant.parse(\"2024-01-15T10:00:00Z\"));\n\n"
    return src.replace(m.group(0), ins, 1)

for path in sorted(BASE.rglob("*.java")):
    src = path.read_text()
    orig = src
    rel = str(path.relative_to(BASE))

    # A. LocalDateTime.now() suelto -> Instant.now() (los LocalDate ya fueron convertidos en pasada 1)
    src = src.replace("LocalDateTime.now()", "Instant.now()")

    # B. Cierres de array de rango: Instant.now() como fin de LocalDate[] -> LocalDate.now()
    src = re.sub(r"(LocalDate\.now\(\)\.minusDays\(\d+\),\s*)Instant\.now\(\)", r"\1LocalDate.now()", src)

    # C. SavingsGoalData createdAt (ultimo arg antes de ')')
    src = re.sub(r'"(HIGH|LOW|MEDIUM|INVALID)",\s*\n?\s*Instant\.now\(\)', r'"\1",\n                            Instant.now()', src)

    # D. Transaction.reconstitute(..., date, date, true) -> audit a Instant
    src = re.sub(r"(\bdate,\s*)date,\s*true", r"\1Instant.now(), Instant.now(), true", src)

    # E. Constructores con TimeProvider primero
    src = re.sub(r"\bCategory\.create\(", "Category.create(TIME, ", src)
    src = re.sub(r"\bSavingsGoal\.create\((?!TIME)", "SavingsGoal.create(TIME, ", src)
    src = re.sub(r"\bnew DashboardPeriodService\(\)", "new DashboardPeriodService(TIME)", src)

    # F. limpiar import LocalDateTime si no se usa
    body = re.sub(r"import java\.time\.LocalDateTime;\n", "", src)
    if "LocalDateTime" not in body:
        src = body.replace("import java.time.LocalDateTime;\n", "")

    # G. imports TIME si se usa
    if "TIME" in re.sub(r"TIME\b", "", "") or re.search(r"\bTIME[,)]|\bTIME\.", src) or "(TIME," in src or "TIME)" in src:
        if "FixedTimeProvider TIME" not in src and ("(TIME," in src or "TIME)" in src):
            src = add_time_field(src)
            src = ensure_import(src, TP_IMPORT)
            src = ensure_import(src, FT_IMPORT)
            src = ensure_import(src, INSTANT_IMPORT)

    if src != orig:
        path.write_text(src)
        changed = True
print("done")
