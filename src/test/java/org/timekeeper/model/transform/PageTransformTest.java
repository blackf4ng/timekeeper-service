package org.timekeeper.model.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.timekeeper.model.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PageTransformTest {

    private static final Integer PAGE = 1;

    private static final Integer TOTAL_PAGES = 2;

    private static final Integer CONTENT = 3;

    private static final Long TOTAL_ELEMENTS = 4L;

    private static final String CONTENT_STRING = Integer.toString(CONTENT);

    @Mock
    private org.springframework.data.domain.Page<Integer> page;

    @Mock
    private Pageable pageable;

    @Test
    public void testApply_withValidInput_shouldSucceed() {
        Page<String> expected = Page.<String>builder()
            .data(List.of(CONTENT_STRING))
            .page(PAGE)
            .totalElements(TOTAL_ELEMENTS)
            .totalPages(TOTAL_PAGES)
            .build();

        when(page.getTotalPages()).thenReturn(TOTAL_PAGES);
        when(page.getTotalElements()).thenReturn(TOTAL_ELEMENTS);
        when(page.getContent()).thenReturn(List.of(CONTENT));
        when(page.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(PAGE);

        assertEquals(
            expected,
            PageTransform.apply(page, value -> Integer.toString(value))
        );
    }

}
