package test.controls;

import org.springframework.context.ApplicationEvent;

/**
 * <b>function:</b>发送聊天信息事件
 * @author hoojo
 * @createDate 2011-6-7 上午11:24:21
 * @file MessageEvent.java
 * @package com.hoo.util
 * @project DWRComet
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
public class ChatMessageEvent extends ApplicationEvent {
 
    private static final long serialVersionUID = 1L;
 
    public ChatMessageEvent(Object source) {
        super(source);
    }
}
