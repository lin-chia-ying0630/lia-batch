package com.alinlin.liabatch;

import com.alinlin.liabatch.controller.LiaReportBatchController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LIA 批次程式啟動入口。
 * <p>
 * Spring Boot 啟動後透過 CommandLineRunner 將命令列參數交給LIA通報批次 controller。
 */
@SpringBootApplication
public class LiaBatchApplication implements CommandLineRunner {

    private final LiaReportBatchController liaReportBatchController;

    @Autowired
    public LiaBatchApplication(LiaReportBatchController liaReportBatchController) {
        this.liaReportBatchController = liaReportBatchController;
    }

    public static void main(String[] args) {
        SpringApplication.run(LiaBatchApplication.class, args);
    }

    @Override
    public void run(String... args) {
        liaReportBatchController.run(args);
    }
}
