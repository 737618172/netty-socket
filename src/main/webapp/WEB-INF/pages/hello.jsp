<%--
  Created by IntelliJ IDEA.
  User: mypc
  Date: 2020/4/4
  Time: 17:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<script type="text/javascript" src="https://cdn.bootcss.com/jquery/3.4.1/jquery.js"></script>
<script src="layui/layui.js"></script>
<script type="text/javascript" src="./js/util.js"></script>
<script type="text/javascript" src="./js/message.js?v=1"></script>
<script type="text/javascript" src="./js/messagebody.js"></script>
<script src="js/websocketconfig.js"></script>
<script>
    var token;
    var receiverId;

    $(function () {
        token = "${cookie.token.value }";
        console.log(token);
    })

    var socket;

    var showOfflineMsg = function () {
        $.ajax({
            type: "post",
            url: "hisMessage",
            async: true,
            success: function (data) {
                console.log("showOfflineMsg" + data);
                console.log(data);
                // var dataObj = eval("(" + data + ")");
                // if (dataObj != null && dataObj.length > 0) {
                //     var result = "";
                //     var rt = document.getElementById("response");
                //     for (var i = 0; i < dataObj.length; i++) {
                //         console.log(dataObj[i]);
                //         var username = dataObj[i].id + dataObj[i].sender;
                //         var timestamp = dataObj[i].sendTime;
                //         var msg = dataObj[i].content;
                //         result = result + username + "   " + timestamp +"   : " + msg+ "\n";
                //     }
                //     rt.value = result;
                // }
            }
        });
    }

    function send(msg) {
        if (!window.socket) {
            return;
        }

        var message = new proto.Model();
        var content = new proto.MessageBody();
        message.setMsgtype(4);
        message.setCmd(5);
        message.setToken(token);
        message.setSender(token);
        console.log("receiver is " + receiverId);
        message.setReceiver(receiverId);//机器人ID默认为0
        content.setContent(msg);
        content.setType(0)
        message.setContent(content.serializeBinary())
        console.log(message);
        socket.send(message.serializeBinary());

        document.getElementsByName('message')[0].value = '';
    }

    function init() {
        if (window.WebSocket) {
            socket = new WebSocket("ws://localhost:8040/");
            socket.binaryType = "arraybuffer";
            //接受服务器消息
            socket.onmessage = function (ex) {
                if (ex.data instanceof ArrayBuffer) {
                    var msg = proto.Model.deserializeBinary(ex.data);
                    console.log(msg);
                    if (msg.getCmd() == 1) {
                        receiverId = msg.getSender();
                        var msgCon = proto.MessageBody.deserializeBinary(msg.getContent());
                        console.log(msgCon.getContent());
                        var rt = document.getElementById("response");
                        alert(msgCon.getContent());
                    } else if (msg.getCmd() == 2) {
                        var message1 = new proto.Model();
                        message1.setCmd(2);
                        message1.setMsgtype(4);
                        socket.send(message1.serializeBinary());
                    }  else if (msg.getCmd() == 4) {

                    } else if (msg.getCmd() == 5) {
                        var msgCon = proto.MessageBody.deserializeBinary(msg.getContent());
                        var content = msgCon.getContent();
                        var rt = document.getElementById("response");
                        rt.value = rt.value + "\n" + content;
                    } else if (msg.getCmd() == 6) {//reconn

                    }
                }
            }
            socket.onopen = function (ex) {
                var rt = document.getElementById("response");
                rt.value = rt.value + "\n" + "连接开启";
                var message = new proto.Model();
                var browser = BrowserUtil.info();
                message.setVersion("1.0");
                message.setDeviceid("");
                message.setCmd(3);
                message.setSender(token);
                message.setMsgtype(1);
                message.setFlag(1);
                message.setPlatform(browser.name);
                message.setPlatformversion(browser.version);
                message.setToken(token);
                message.setUtype(1);
                var bytes = message.serializeBinary();
                console.log(bytes);
                socket.send(bytes);
                showOfflineMsg();
            }

            socket.onclose = function (ex) {
                var rt = document.getElementById("response");
                rt.value = rt.value + "\n" + "连接断开";
                console.log("onclose");
                reconnect(websocketurl, init);
            }
            socket.onerror = function () {
                // layer.msg("服务器连接出错，请检查websocketconfig.js里面的IP地址");
                console.log("onerror");
                reconnect(websocketurl, init);
            };
        } else {
            alert("浏览器不支持websocket")
        }
    }


</script>
<form onsubmit="return false;">

    <input type="button" value="在线咨询" onclick="init()"/>

    <textarea name="message" rows="20" cols="50"></textarea>
    <input type="button" value="发送" onclick="send(this.form.message.value)"/>

    <textarea id="response" rows="20" cols="50"></textarea>
    <input type="button" value="清空内容" onclick="document.getElementById('response').value='' "/>
</form>
</body>
</html>
