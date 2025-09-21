package org.timekeeper.database.postgresql.model.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PageRequestTransformTest {

    private static final Integer PAGE = 1;

    private static final Integer PAGE_SIZE = 2;

    private static final PageRequest PAGE_REQUEST = PageRequest.of(PAGE, PAGE_SIZE);

    @Mock
    private Pageable pageable;

    @Test
    public void testApply_withValidInput_shouldSucceed() {
        assertEquals(
            PAGE_REQUEST,
            PageRequestTransform.apply(
                org.timekeeper.model.request.PageRequest.builder()
                    .page(PAGE)
                    .pageSize(PAGE_SIZE)
                    .build()
            )
        );
    }

}
