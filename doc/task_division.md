# my-netty-gateway

## 任务拆解

| 任务     | 测试用例                                                                                           |
|:---------|:---------------------------------------------------------------------------------------------------|
| 连接管理 | 1. 当有客户端连接时,生成新的会话<br>2.当有客户端断开连接时,会话删除<br>3.当有客户端空闲时,会话删除 |
| 解码器   | 1. 传入以0x7E开头的消息,则解析为对应的消息实体                                                     |
