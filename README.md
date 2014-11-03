netty 实现的 tcp/udt 协议的聊天/推送服务器端, 消息采用 json 格式.<br/>

1. 默认使用 redis 作为session 及消息的存储介质.<br/>

2. 客户端建立起链接后, 不需要主动发起心跳, 服务器会在必要的时候发起要求客户端发生心跳消息的请求, 此时客户端才发送心跳消息.<br/>

3. 客户端与服务器端收到消息后都需要向对方发送回执, 未收到回执则在一定时间内进行重发.

4. 消息协议格式: 8字节(标示 sessionId) + 4字节(int 标示消息类型) + 4字节 (int标示消息体长度) + n 字节(消息体)

5. 通道 channel 的 id 生成方式依赖 Twitter 的 Snowflake, 如果不提供 thrift 服务, 则使用本地的 Snowflake.

<h2>配置说明<h2>

<h4>redis</h4>
<p>redis.pool.type=single // redis 连接池类型, single 表示单一节点, sentinel 表示为哨兵集群</p>
<p>redis.host=127.0.0.1  // redis的 ip</p>
<p>redis.port=6379 // redis 端口</p>
<p>redis.df_db=0 // redis 默认 db</p>
<p>redis.timeout=10 // redis 连接超时时间</p>
<p>redis.sentinel.master.name=redisMaster // 哨兵集群的名称</p>
<p>redis.sentinel.addrs=127.0.0.1:6379,127.0.0.1:6380,127.0.0.1:6381 // 哨兵集群的哨兵地址, 必须为 redis 实力数(n)的 m 倍+1个哨兵进程.</p>

<h4>im</h4>
<p>im.server.name=server0 // 当前服务器名称</p>
<p>im.host=0.0.0.0  // 服务器绑定的 ip</p>
<p>im.port=9000 // 服务器绑定的端口</p>
<p>im.all.idle.secs=15 // socket 的读写全部空闲的时间上限, 单位秒, 超时客户端将下线</p>
<p>im.read.idle.secs=10 // socket 的读空闲时间上限, 单位秒, 超时并且 session 的 overtime 超时时客户端将下线.</p>
<p>im.write.idle.secs=5 // socket 的写空闲时间上限, 单位秒, 超时时向客户端发送要求客户端发送心跳消息的请求消息.</p>
<p>im.re.send.un.ack.pool.size=5 // 处理未收到回执的消息的线程池大小</p>
<p>im.un.ack.resp.msg.wheel.duration.secs=3 // 处理未收到回执的消息的定时轮的一轮时间, 单位秒, 该参数即为消息的回执接收的超时时间, 超时将重发.</p>
<p>im.un.ack.resp.msg.wheel.per.slot.secs=1 // 处理未收到回执的消息的定时轮的一个槽的时间, 单位秒</p>
<p>im.un.ack.resp.msg.wheel.name=un_ack_msg_wheel // 处理未收到回执的消息的定时轮的名称</p>
<p>im.auth.check.so.illegal=false // 是否检测 socket 连接建立之前已通过 http 接口获取 token, 测试时设为 false, 生成应为 true</p>
<p>im.server.type=udt // 服务器采用的协议类型, 可以为 tcp 或 udt</p>
<p>im.server.worker.threads=1000 // 工作线程数</p>
<p>im.server.boss.threads=5 // 主reactor线程数</p>
<p>im.server.socket.backlog.count=1000 // backlog 数</p>
<p>im.client.ping.overtime=10000 // 两次 ping 的最大时间差, 单位毫秒</p>
<p>im.channel.id.thrift.host=127.0.0.1 // Snowflake 的host</p>
<p>im.channel.id.thrift.port=9090 // Snowflake 的端口</p>
<p>im.channel.id.thrift.usragent=im // Snowflake 校验的 useragent</p>
<p>im.counter.thrift.server.port=19000 // channel 数和 online 数的计数器的 thrift 服务端口</p>
<br/>

<h2>消息类型</h2>
0 ---> 服务器端接收, ping/心跳消息, 格式为 {"type" : 0, "sid" : 1}

1 ---> 文本聊天消息, 服务器端收到的格式为 {"type" : 1, "sid" : 1, "data" : {"msg_no" : "111", "from" : "10001", "to" : "10000", "content" : "ahahah"}}, 客户端收到的格式为 {"type" : 1, "data" : {"msg_no" : "111", "time" : "1470000000", "from" : "10001", "to" : "10000", "content" : "ahahah", "from_info" : {"nickname" : "1111", "head" : "...."}}}

2 ---> 语音聊天消息, 服务器端接收到得格式为 {"type" : 2, "sid" : 1, "data" : {"msg_no" : "111", "me" : "10001", "to" : "10000", "content" : "xxx/xxx/xxx.mp3"}},  客户端先将语音上传到某个服务器, 得到语音的 url 发送给服务器. 客户端接收到得格式为{"type" : 2, "data" : {"msg_no" : "111", "time" : "1470000000", "from" : "10001", "to" : "10000", "content" : "xxx/xxx/xxx.mp3", "from_info" : {"nickname" : "1111", "head" : "...."}}}

4 ---> 服务器端接收, 客户端下线消息, 格式为 {"type" : 4, "sid" : 1}

5 ---> 回执消息, 格式为 {"type" : 5, "sid" : 1, "time" : "1470000000", "data" : {"msg_no" : "11111", "src_to" : "10000", "src_from" : "10001"}}, 对于客户端发送回执来说, src_from 为消息的发送者 id, src_to 为自己即客户端的 id, 服务器端发送是则是 src_from 为消息发送者即客户端的 id, src_to 为消息接收者的 id

6 ---> 客户端接收, 客户端被踢下线, 格式为 {"type" : 6, "sid" : 1}

8 ---> 客户端接收, 服务器端会在超过im.write.idle.secs参数值的秒数后向客户端发送要求客户端发送心跳消息的请求. 格式为 {"type" : 8, "sid" : 1}

9 ---> 客户端接收, 服务器出错, 格式为 {"type" : 9, "sid" : 1}

10 ---> 客户端接收, 离线消息, 格式为 {"type" : 10, "sid" : 1, "time" : "1470000000", "data" : { "offline_msgs" : [...(type 为1或2的消息)], "from" : "server_1470000000", "to" : "10000"}, 客户端登陆时发送, 注意此时的 from 不是离线消息的发送者, 而是一串以server_开头的字符串.

11 ---> 客户端接收, 在客户端未登录或者 sessionId 错误或 sessionId 未空时发生, 要求客户端发送登录信息. 格式为 {"type" : 11, "sid" : 1}

12 ---> 登录消息, 客户端在未登录时发送登录消息, 格式为: {"type" : 12, , "sid" : 1, "data" : {"userId" : "10001", "token" : "0000"}}, 在验证登录消息通过后服务器端返回登录成功消息, 格式为 {"type" : 12, , "sid" : 1}
