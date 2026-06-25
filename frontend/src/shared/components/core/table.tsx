import * as React from "react"

import { cn } from "@/lib/utils"

interface TableProps extends React.HTMLAttributes<HTMLTableElement> {
  ref?: React.Ref<HTMLTableElement>
}

const Table = ({ className, ref, ...props }: TableProps) => (
  <div className="relative w-full overflow-auto">
    <table
      ref={ref}
      className={cn("w-full caption-bottom text-sm", className)}
      {...props}
    />
  </div>
)
Table.displayName = "Table"

interface TableHeaderProps extends React.HTMLAttributes<HTMLTableSectionElement> {
  ref?: React.Ref<HTMLTableSectionElement>
}

const TableHeader = ({ className, ref, ...props }: TableHeaderProps) => (
  <thead ref={ref} className={cn("[&_tr]:border-b", className)} {...props} />
)
TableHeader.displayName = "TableHeader"

interface TableBodyProps extends React.HTMLAttributes<HTMLTableSectionElement> {
  ref?: React.Ref<HTMLTableSectionElement>
}

const TableBody = ({ className, ref, ...props }: TableBodyProps) => (
  <tbody
    ref={ref}
    className={cn("[&_tr:last-child]:border-0", className)}
    {...props}
  />
)
TableBody.displayName = "TableBody"

interface TableRowProps extends React.HTMLAttributes<HTMLTableRowElement> {
  ref?: React.Ref<HTMLTableRowElement>
}

const TableRow = ({ className, ref, ...props }: TableRowProps) => (
  <tr
    ref={ref}
    className={cn(
      "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted",
      className
    )}
    {...props}
  />
)
TableRow.displayName = "TableRow"

interface TableHeadProps extends React.ThHTMLAttributes<HTMLTableCellElement> {
  ref?: React.Ref<HTMLTableCellElement>
}

const TableHead = ({ className, ref, ...props }: TableHeadProps) => (
  <th
    ref={ref}
    className={cn(
      "h-12 px-4 text-left align-middle font-medium text-muted-foreground [&:has([role=checkbox])]:pr-0",
      className
    )}
    {...props}
  />
)
TableHead.displayName = "TableHead"

interface TableCellProps extends React.TdHTMLAttributes<HTMLTableCellElement> {
  ref?: React.Ref<HTMLTableCellElement>
}

const TableCell = ({ className, ref, ...props }: TableCellProps) => (
  <td
    ref={ref}
    className={cn("p-4 align-middle [&:has([role=checkbox])]:pr-0", className)}
    {...props}
  />
)
TableCell.displayName = "TableCell"

export {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
}
