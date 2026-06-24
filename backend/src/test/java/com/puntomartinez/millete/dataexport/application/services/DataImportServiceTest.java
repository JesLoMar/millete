package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.migration.MigrationChain;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataImportServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;
    @Mock
    private InvestmentRepository investmentRepository;
    @Mock
    private MigrationChain migrationChain;

    @InjectMocks
    private DataImportService dataImportService;

    private UUID sourceUserId;
    private UUID destUserId;
    private UUID sourceCategoryId;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sourceUserId = UUID.randomUUID();
        destUserId = UUID.randomUUID();
        sourceCategoryId = UUID.randomUUID();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void importUserData_shouldRemapCategoryIds_whenImportingToDifferentUser() throws Exception {
        // Arrange: crear snapshot con 1 categoría y 1 transacción
        Category sourceCategory = new Category(
                sourceCategoryId, sourceUserId, "Comida", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(), sourceUserId, sourceCategoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Almuerzo",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(sourceCategory),
                List.of(sourceTransaction),
                List.of(),
                List.of(),
                List.of()
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        // Mock: no hay categorías existentes para el usuario destino
        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        // Verificar que la categoría guardada tiene userId del destino y UUID nuevo
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertEquals(destUserId, savedCategory.getUserId());
        assertNotEquals(sourceCategoryId, savedCategory.getId()); // UUID nuevo

        // Verificar que la transacción guardada apunta a la nueva categoría y userId del destino
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();
        assertEquals(destUserId, savedTx.getUserId());
        assertNotEquals(sourceCategoryId, savedTx.getCategoryId()); // ID remapeado
        assertNotNull(savedTx.getCategoryId()); // No debe ser null
    }

    @Test
    void importUserData_shouldReuseExistingCategory_whenNameMatches() throws Exception {
        // Arrange: usuario destino ya tiene categoría "Comida"
        UUID existingCategoryId = UUID.randomUUID();
        Category existingCategory = new Category(
                existingCategoryId, destUserId, "Comida", "#00FF00",
                new BigDecimal("300.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        Category sourceCategory = new Category(
                sourceCategoryId, sourceUserId, "Comida", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(), sourceUserId, sourceCategoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Almuerzo",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(sourceCategory),
                List.of(sourceTransaction),
                List.of(),
                List.of(),
                List.of()
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(List.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        // Verificar que NO se creó nueva categoría (solo se actualizó la existente)
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertEquals(existingCategoryId, savedCategory.getId()); // Mismo ID existente

        // Verificar que la transacción apunta al ID existente
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();
        assertEquals(existingCategoryId, savedTx.getCategoryId());
    }

    @Test
    void importUserData_shouldSkipInactiveEntities() throws Exception {
        // Arrange: categoría inactiva y transacción inactiva
        Category inactiveCategory = new Category(
                sourceCategoryId, sourceUserId, "Inactiva", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), false
        );

        Transaction inactiveTransaction = new Transaction(
                UUID.randomUUID(), sourceUserId, sourceCategoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Inactiva",
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(inactiveCategory),
                List.of(inactiveTransaction),
                List.of(),
                List.of(),
                List.of()
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));
        assertTrue(result.contains("0 registros importados")); // Nada se importó

        // Verificar que NO se guardó nada
        verify(categoryRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void importUserData_shouldSanitizeUserIds() throws Exception {
        // Arrange: snapshot con userId de origen
        Category sourceCategory = new Category(
                sourceCategoryId, sourceUserId, "Comida", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(sourceCategory),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        dataImportService.importUserData(file, destUserId);

        // Assert: verificar que la categoría guardada tiene userId del destino
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertEquals(destUserId, categoryCaptor.getValue().getUserId());
        assertNotEquals(sourceUserId, categoryCaptor.getValue().getUserId());
    }
}
