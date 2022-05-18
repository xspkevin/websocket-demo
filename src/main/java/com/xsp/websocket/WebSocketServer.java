package com.xsp.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: websocket处理类，作用相当于HTTP请求中的controller
 * @Author: Xu Shengping
 * @Date: 2022/5/17 5:45 下午
 */
@Component
@Slf4j
@ServerEndpoint("/api/pushMessage/{userId}")
public class WebSocketServer {

    // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的
    private static int onlineCount = 0;

    // concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    // 接收userId
    private String userId = "";

    /**
     * @Description: 连接建立成功调用的方法
     * @Param: [session, userId]
     * @Return: void
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
        } else {
            // 在线数加1
            addOnlineCount();
        }
        // 加入set中
        webSocketMap.put(userId, this);

        log.info("用户连接：" + userId + ", 当前在线人数为：" + getOnlineCount());
        sendMessage("连接成功");
    }

    /**
     * @Description: 连接关闭调用的方法
     * @Param: []
     * @Return: void
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            //从set中删除
            subOnlineCount();
        }
        log.info("用户退出：" + userId + "，当前在线人数为：" + getOnlineCount());
    }

    /**
     * @Description:  收到客户端消息后调用的方法
     * @Param: [message, session]
     * @Return: void
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户消息：" + userId + "，报文：" + message);
        // 可以群发消息, 消息保存到数据库、redis
        if (StringUtils.isNotBlank(message)) {
            try {
                // 解析发送的报文
                JSONObject jsonpObject = JSON.parseObject(message);
                // 追回发送人（防止串改）
                jsonpObject.put("fromUserId", this.userId);
                String toUserId = jsonpObject.getString("toUserId");
                // 传送给对应toUserId用户的websocket
                if (StringUtils.isNoneBlank(toUserId) && webSocketMap.containsKey(toUserId)) {
                    webSocketMap.get(toUserId).sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误：" + this.userId + "，原因：" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * @Description: 实现服务器主动推送
     * @Param: [message]
     * @Return: void
     */
    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: 发送自定义消息
     * @Param: [message, userId]
     * @Date: 2022/5/17
     */
    public static void sendInfo(String message, String userId) {
        log.info("发送消息到：" + userId + "，报文：" + message);
        if (StringUtils.isNotBlank(userId) && webSocketMap.containsKey(userId)) {
            webSocketMap.get(userId).sendMessage(message);
        } else {
            log.error("用户：" + userId + ", 不在线！");
        }
    }

    /**
     * @Description: 获得此时的在线人数
     * @Param: []
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * @Description: 在线人数加1
     * @Param: []
     * @Return: void
     */
    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    /**
     * @Description: 在线人数减1
     * @Param: []
     * @Return: void
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
