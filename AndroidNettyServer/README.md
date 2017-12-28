# Android netty server
Start a netty server on android

## Download netty
Download url :[https://netty.io/downloads.html](https://netty.io/downloads.html)

Download netty-all.jar and put it into libs. Then add as library.

## Project structure
> netty
>
>>AppServer
>
>>AppServerHandler
>
>>AppServerInitializer
>
>>IServer
>
>>MessageProtocol
>
>>TcpProtoCodec
>
>MainActivity
>
>MessageEvent

AppServerHandler: handle channel add, remove and activity. in this class, channelRead0 will handle the message which send by client.

AppServerInitializer: initialize AppServer. Then add Tcp Protocol Codec and AppServerHandler.

AppServer: start netty server.

MessageProtocol: netty send and receive message format.

MessageEvent: use for eventbus.

MainActivity: show receive message content.

## Communication protocol
'R' + data.length + data
