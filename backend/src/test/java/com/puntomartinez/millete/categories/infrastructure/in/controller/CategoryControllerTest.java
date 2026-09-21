package com.puntomartinez.millete.categories.infrastructure.in.controller;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.DeleteCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.GetCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryUseCase;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.CategoryResponseDTO;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.RegisterCategoryRequestDTO;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.UpdateCategoryRequestDTO;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController")
class CategoryControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private RegisterCategoryUseCase registerCategoryUseCase;

    @Mock
    private UpdateCategoryUseCase updateCategoryUseCase;

    @Mock
    private GetCategoryUseCase getCategoryUseCase;

    @Mock
    private DeleteCategoryUseCase deleteCategoryUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CategoryController controller;

    private void mockAuthenticatedUser() {
        JwtUser jwtUser = new JwtUser(USER_ID, "username", "user@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private Category validCategory() {
        return Category.create(
                USER_ID,
                "Food",
                "#FF5733",
                BigDecimal.TEN
        );
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("Should create category and return 201")
        void shouldCreateCategoryAndReturnCreated() {
            mockAuthenticatedUser();

            RegisterCategoryRequestDTO request =
                    new RegisterCategoryRequestDTO("Food", "#FF5733", BigDecimal.TEN);
            Category category = validCategory();

            when(registerCategoryUseCase.register(any(RegisterCategoryCommand.class)))
                    .thenReturn(category);

            ResponseEntity<CategoryResponseDTO> response =
                    controller.createCategory(request, authentication);

            ArgumentCaptor<RegisterCategoryCommand> captor =
                    ArgumentCaptor.forClass(RegisterCategoryCommand.class);
            verify(registerCategoryUseCase).register(captor.capture());

            RegisterCategoryCommand command = captor.getValue();

            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.name()).isEqualTo("Food");
            assertThat(command.color()).isEqualTo("#FF5733");
            assertThat(command.budgetLimit()).isEqualByComparingTo(BigDecimal.TEN);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(category.getId());
            assertThat(response.getBody().userId()).isEqualTo(USER_ID);
            assertThat(response.getBody().name()).isEqualTo("Food");
            assertThat(response.getBody().color()).isEqualTo("#FF5733");
            assertThat(response.getBody().budgetLimit()).isEqualByComparingTo(BigDecimal.TEN);
            assertThat(response.getBody().active()).isTrue();
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @ParameterizedTest
        @ValueSource(ints = {-1, -100})
        @DisplayName("Should reject negative page")
        void shouldRejectNegativePage(int page) {
            assertThatThrownBy(() -> controller.getAll(authentication, page, 10, null))
                    .isInstanceOf(InvalidInputException.class);

            verifyNoInteractions(getCategoryUseCase);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 201})
        @DisplayName("Should reject invalid page size")
        void shouldRejectInvalidPageSize(int size) {
            assertThatThrownBy(() -> controller.getAll(authentication, 0, size, null))
                    .isInstanceOf(InvalidInputException.class);

            verifyNoInteractions(getCategoryUseCase);
        }

        @Test
        @DisplayName("Should throw when requested page is out of range")
        void shouldThrowWhenRequestedPageIsOutOfRange() {
            mockAuthenticatedUser();

            when(getCategoryUseCase.countByUserIdAndFilters(USER_ID, null))
                    .thenReturn(0L);

            assertThatThrownBy(() -> controller.getAll(authentication, 1, 10, null))
                    .isInstanceOf(InvalidInputException.class);

            verify(getCategoryUseCase, never())
                    .findAllByUserId(any(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("Should return single page result")
        void shouldReturnSinglePageResult() {
            mockAuthenticatedUser();

            Category category = validCategory();

            when(getCategoryUseCase.countByUserIdAndFilters(USER_ID, "food"))
                    .thenReturn(1L);
            when(getCategoryUseCase.findAllByUserId(USER_ID, 0, 10, "food"))
                    .thenReturn(List.of(category));

            ResponseEntity<PaginatedResponseDTO<CategoryResponseDTO>> response =
                    controller.getAll(authentication, 0, 10, "food");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            PaginatedResponseDTO<CategoryResponseDTO> body = response.getBody();

            assertThat(body.content()).hasSize(1);
            assertThat(body.content().get(0).id()).isEqualTo(category.getId());
            assertThat(body.currentPage()).isZero();
            assertThat(body.totalPages()).isEqualTo(1);
            assertThat(body.totalElements()).isEqualTo(1L);
            assertThat(body.size()).isEqualTo(10);
            assertThat(body.first()).isTrue();
            assertThat(body.last()).isTrue();
        }

        @Test
        @DisplayName("Should return first page of multiple pages")
        void shouldReturnFirstPageOfMultiplePages() {
            mockAuthenticatedUser();

            Category category = validCategory();

            when(getCategoryUseCase.countByUserIdAndFilters(USER_ID, null))
                    .thenReturn(25L);
            when(getCategoryUseCase.findAllByUserId(USER_ID, 0, 10, null))
                    .thenReturn(List.of(category));

            ResponseEntity<PaginatedResponseDTO<CategoryResponseDTO>> response =
                    controller.getAll(authentication, 0, 10, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            PaginatedResponseDTO<CategoryResponseDTO> body = response.getBody();

            assertThat(body.currentPage()).isZero();
            assertThat(body.totalPages()).isEqualTo(3);
            assertThat(body.totalElements()).isEqualTo(25L);
            assertThat(body.first()).isTrue();
            assertThat(body.last()).isFalse();
        }

        @Test
        @DisplayName("Should return last page of multiple pages")
        void shouldReturnLastPageOfMultiplePages() {
            mockAuthenticatedUser();

            Category category = validCategory();

            when(getCategoryUseCase.countByUserIdAndFilters(USER_ID, null))
                    .thenReturn(25L);
            when(getCategoryUseCase.findAllByUserId(USER_ID, 2, 10, null))
                    .thenReturn(List.of(category));

            ResponseEntity<PaginatedResponseDTO<CategoryResponseDTO>> response =
                    controller.getAll(authentication, 2, 10, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            PaginatedResponseDTO<CategoryResponseDTO> body = response.getBody();

            assertThat(body.currentPage()).isEqualTo(2);
            assertThat(body.totalPages()).isEqualTo(3);
            assertThat(body.totalElements()).isEqualTo(25L);
            assertThat(body.first()).isFalse();
            assertThat(body.last()).isTrue();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update category and return 200")
        void shouldUpdateCategoryAndReturnOk() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();
            UpdateCategoryRequestDTO request =
                    new UpdateCategoryRequestDTO("Updated", "#00FF00", BigDecimal.ONE);
            Category category = Category.create(USER_ID, "Updated", "#00FF00", BigDecimal.ONE);

            when(updateCategoryUseCase.update(
                    eq(categoryId),
                    eq(USER_ID),
                    any(UpdateCategoryCommand.class)
            )).thenReturn(category);

            ResponseEntity<CategoryResponseDTO> response =
                    controller.update(categoryId, request, authentication);

            ArgumentCaptor<UpdateCategoryCommand> captor =
                    ArgumentCaptor.forClass(UpdateCategoryCommand.class);
            verify(updateCategoryUseCase).update(eq(categoryId), eq(USER_ID), captor.capture());

            UpdateCategoryCommand command = captor.getValue();

            assertThat(command.name()).isEqualTo("Updated");
            assertThat(command.color()).isEqualTo("#00FF00");
            assertThat(command.budgetLimit()).isEqualByComparingTo(BigDecimal.ONE);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(category.getId());
            assertThat(response.getBody().name()).isEqualTo("Updated");
            assertThat(response.getBody().color()).isEqualTo("#00FF00");
            assertThat(response.getBody().budgetLimit()).isEqualByComparingTo(BigDecimal.ONE);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Should delete category and return 204")
        void shouldDeleteCategoryAndReturnNoContent() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();

            ResponseEntity<Void> response = controller.delete(categoryId, authentication);

            verify(deleteCategoryUseCase).deleteByIdAndUserId(categoryId, USER_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
        }
    }
}