package com.alinlin.liabatch;

import com.alinlin.liabatch.controller.LiaReportBatchController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LiaBatchApplicationTests {

    @MockitoBean
    private LiaReportBatchController liaReportBatchController;

    @Test
    void contextLoads() {
    }

}
