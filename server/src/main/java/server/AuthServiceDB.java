package server;

import java.sql.*;

public class AuthServiceDB implements AuthService {
    private Connection connection;
    private PreparedStatement stmt;

    public AuthServiceDB() throws ClassNotFoundException, SQLException {
        connect();
    }

    private void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickname = null;

        try {

            stmt = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?");
            stmt.setString(1, login);
            stmt.setString(2, password);
            ResultSet rs =  stmt.executeQuery();

            if (rs.next()) {
                nickname = rs.getString("nickname");
                rs.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nickname;

    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        boolean result;

        try {

            stmt = connection.prepareStatement("SELECT\n" +
                    "1\n" +
                    "FROM users\n" +
                    "WHERE login = ?\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                    "1\n" +
                    "FROM users\n" +
                    "WHERE nickname = ?");

            stmt.setString(1, login);
            stmt.setString(2, nickname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                result = false;
            } else {
                stmt = connection.prepareStatement("INSERT INTO users (login, nickname, password) VALUES (?, ?, ?)");
                stmt.setString(1, nickname);
                stmt.setString(2, login);
                stmt.setString(3, password);
                result = stmt.executeUpdate() > 0;
            }

            rs.close();

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean changeNickname(String login, String newNickname) {
        boolean result;

        try {

            stmt = connection.prepareStatement("SELECT 1 FROM users WHERE nickname = ?");

            stmt.setString(1, newNickname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                result = false;
            } else {
                stmt = connection.prepareStatement("UPDATE users SET nickname = ? WHERE login = ?");
                stmt.setString(1, newNickname);
                stmt.setString(2, login);
                result = stmt.executeUpdate() > 0;
            }

            rs.close();

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }

        return result;

    }
}
