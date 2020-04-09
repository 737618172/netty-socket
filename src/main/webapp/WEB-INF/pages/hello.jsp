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
<script type="text/javascript" src="./js/util.js"></script>
<script type="text/javascript" src="./js/message.js?v=1"></script>
<script type="text/javascript" src="./js/messagebody.js"></script>
<script>
    <%--$(function () {--%>

    <%--    $.ajax({--%>
    <%--        type: "GET",--%>
    <%--        url: "http://localhost:8010/inPage",--%>
    <%--        dataType: 'json',--%>
    <%--        success: function(result) {--%>
    <%--            alert(result);--%>
    <%--        }--%>
    <%--    });--%>
    <%--    // alert(${session.cid});--%>
    <%--})--%>
    var sessionId;
    var receiverId;

    $(function () {
        sessionId = "${pageContext.session.id}";
        console.log(sessionId);
    })

    var socket;

    function send(msg) {
        if (!window.socket) {
            return;
        }

        var message = new proto.Model();
        var content = new proto.MessageBody();
        message.setMsgtype(4);
        message.setCmd(5);
        message.setToken(sessionId);
        message.setSender(sessionId);
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
            //接受服务器消息
            socket.onmessage = function (ex) {
                var reader = new FileReader();
                var ab;
                reader.readAsArrayBuffer(ex.data);
                reader.onload = function() {
                    ab = this.result;
                    if (ab instanceof ArrayBuffer) {
                        var msg = proto.Model.deserializeBinary(ab);
                        console.log(msg.getSender());
                        if(msg.getCmd() == 1){
                            receiverId = msg.getSender();
                            var msgCon = proto.MessageBody.deserializeBinary(msg.getContent());
                            console.log(msgCon.getContent());
                            var rt = document.getElementById("response");
                            rt.value = rt.value + "\n" + msgCon;
                        }else if(msg.getCmd() == 2){

                        }else if(msg.getCmd() == 3){

                        }else if(msg.getCmd() == 4){

                        }else if(msg.getCmd() == 5){
                            var msgCon = proto.MessageBody.deserializeBinary(msg.getContent());
                            var content = msgCon.getContent();
                            var rt = document.getElementById("response");
                            rt.value = rt.value + "\n" + content;
                        }
                    }
                }
            }
            socket.onopen = function (ex) {
                var rt = document.getElementById("response");
                rt.value = rt.value + "\n" + "连接开启";
                var message = new proto.Model();
                var browser = BrowserUtil.info();
                message.setVersion("1.0");
                message.setDeviceid("")
                message.setCmd(1);
                message.setSender(sessionId);
                message.setMsgtype(1);
                message.setFlag(1);
                message.setPlatform(browser.name);
                message.setPlatformversion(browser.version);
                message.setToken(sessionId);
                message.setUtype(1);
                var bytes = message.serializeBinary();
                console.log(bytes);
                socket.send(bytes);
                // showOfflineMsg();
            }

            socket.onclose = function (ex) {
                var rt = document.getElementById("response");
                rt.value = rt.value + "\n" + "连接断开";
            }

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
