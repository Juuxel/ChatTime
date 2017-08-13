package chattime.server.event

import chattime.server.ChatServer
import chattime.server.User

abstract class Event(val server: ChatServer)

class ServerEvent(server: ChatServer) : Event(server)
class MessageEvent(server: ChatServer,
                   var msg: String,
                   val sender: User) : Event(server)
class UserEvent(server: ChatServer, val user: User) : Event(server)
