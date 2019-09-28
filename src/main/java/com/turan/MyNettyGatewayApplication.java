package com.turan;

import com.turan.mq.MyInput;
import com.turan.mq.MyOutput;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding({MyOutput.class, MyInput.class})
public class MyNettyGatewayApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(MyNettyGatewayApplication.class, args);
    }

}
