package com.xsp.service;

import com.xsp.websocket.WebSocketServer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description:
 * @Author: Xu Shengping
 * @Date: 2022/5/17 7:58 下午
 */
@Service
@EnableScheduling
public class TestServiceImpl {

    // 打印时间
    @Scheduled(fixedRate = 5000) // 1000毫秒执行一次
    public void printTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String date = dateFormat.format(new Date());
        WebSocketServer.sendInfo(date, "10");
        System.out.println(date);
    }
}
