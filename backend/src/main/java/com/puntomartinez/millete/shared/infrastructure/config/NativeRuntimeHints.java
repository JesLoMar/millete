package com.puntomartinez.millete.shared.infrastructure.config;

import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.UUID;

public class NativeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        // UUID
        hints.reflection().registerType(
                UUID[].class
        );

        // PDFBox resources
        hints.resources().registerPattern(
                "org/apache/pdfbox/resources/afm/*.afm"
        );
        hints.resources().registerPattern(
                "org/apache/pdfbox/resources/glyphlist/glyphlist.txt"
        );
        hints.resources().registerPattern(
                "org/apache/pdfbox/resources/glyphlist/zapfdingbats.txt"
        );

        // PDF export DTOs
        hints.reflection().registerType(
                PdfExportData.Summary.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );

        hints.reflection().registerType(
                PdfExportData.InvestmentRow.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );

        hints.reflection().registerType(
                PdfExportData.TransactionRow.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );
    }
}