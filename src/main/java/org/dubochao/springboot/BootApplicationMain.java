package org.dubochao.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.dubochao")
public class BootApplicationMain {

    public static void main(String[] args)throws Exception {
        SpringApplication.run(BootApplicationMain.class, args);
    }
}
