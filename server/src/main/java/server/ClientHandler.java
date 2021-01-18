package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;
    private String login;

    private class ClientTask implements Runnable {

        @Override
        public void run() {
            try {
//                    socket.setSoTimeout(5000);
                //цикл аутентификации
                while (true) {
                    String str = in.readUTF();

                    if (str.startsWith("/")) {
                        if (str.startsWith("/reg ")) {
                            String[] token = str.split("\\s", 4);
                            boolean b = server.getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMsg("/regok");
                            } else {
                                sendMsg("/regno");
                            }
                        }

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s", 3);
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1];
                                if (!server.isloginAuthenticated(login)) {
                                    nickname = newNick;
                                    out.writeUTF("/authok " + nickname);
                                    server.subscribe(ClientHandler.this);
                                    break;
                                } else {
                                    out.writeUTF("Учетная запись уже используется");
                                }
                            } else {
                                out.writeUTF("Неверный логин / пароль");
                            }
                        }
                    }
                }

                //Цикл работы
                while (true) {
                    String str = in.readUTF();

                    if (str.startsWith("/")) {
                        if (str.startsWith("/w")) {
                            String[] token = str.split("\\s+", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            server.privateMsg(ClientHandler.this, token[1], token[2]);
                        }

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }

                        if (str.startsWith("/nick")) {
                            String[] token = str.split("\\s+", 2);
                            if (server.getAuthService().changeNickname(login, token[1])) {
                                nickname = token[1];
                                server.broadcastClientList();
                                out.writeUTF("Никнейм успешно изменен");
                            } else {
                                out.writeUTF("Не удалось изменить никнейм");
                            }
                        }

                    } else {
                        server.broadcastMsg(ClientHandler.this, str);
                    }
                }
                //catch SocketTimeoutException
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client disconnected!");
                server.unsubscribe(ClientHandler.this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            server.getThreadPool().submit(new ClientTask());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
