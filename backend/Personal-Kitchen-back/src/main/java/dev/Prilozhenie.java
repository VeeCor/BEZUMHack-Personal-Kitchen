package dev;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Prilozhenie {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1111)) {
            System.out.println("Сервер запущен на порту " + 1111);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    handleClientRequest(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream output = clientSocket.getOutputStream()) {

            String requestLine = input.readLine();
            if (requestLine == null) {
                return;
            }

            if (requestLine.startsWith("POST")) {
                String line;
                while (!(line = input.readLine()).isEmpty()) {
                    System.out.println(line);
                }

                StringBuilder requestBody = new StringBuilder();
                while (input.ready()) {
                    requestBody.append((char) input.read());
                }

                String data = requestBody.toString();
                System.out.println("Получены данные от клиента: " + data);

                if (requestLine.contains("/add-data")) {
                    String randomId;
                    Path directoryPath = Paths.get("src", "main", "resources", "soiskateli");
                    do {
                        randomId = String.valueOf((int) (Math.random() * 9000) + 1000);
                        Path filePath = directoryPath.resolve(randomId + ".json");
                        if (!Files.exists(filePath)) {
                            break;
                        }
                    } while (true);

                    JSONObject jsonObject = new JSONObject(data);
                    jsonObject.put("id", randomId);

                    try {
                        String id = jsonObject.getString("id");
                        String filename = id + ".json";

                        Path directoryPath1 = Paths.get("src", "main", "resources", "soiskateli");
                        Path filePath = directoryPath1.resolve(filename);

                        if (!Files.exists(directoryPath1)) {
                            Files.createDirectories(directoryPath1);
                        }

                        Files.write(filePath, jsonObject.toString(4).getBytes());

                        String ToEmail = jsonObject.getString("email");

                        Email email = new Email();

                        email.setFrom("Old New", "no-reply@trial-2p0347z5oyklzdrn.mlsender.net");
                        email.addRecipient("Someone", ToEmail);
                        email.setSubject("ILOVEYOU");
                        email.setPlain("Открой файл, чтобы узнать подробности.");
                        email.attachFile("src/main/resources/email/LOVE-LETTER-FOR-YOU.txt");

                        MailerSend ms = new MailerSend();

                        ms.setToken("mlsn.02d4caf4f9dd5e216a01eeaa48579aa77b8d34822c833385c420731b0db2e626");

                        try {
                            ms.emails().send(email);
                        } catch (MailerSendException e) {
                            e.printStackTrace();
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Connection: close\r\n\r\n" +
                        "Голубь с топографическим кретинизмом уже вылетел, пожалуйста, ожидайте ответа.\r\n";
                    output.write(httpResponse.getBytes());
                } else if (requestLine.contains("/send-email")) {
                    if (data.contains("\"id\"")) {
                        JSONObject jsonObject = new JSONObject(data);
                        String id = jsonObject.getString("id");
                        try {
                            Path filePath = Paths.get("src", "main", "resources", "soiskateli", id + ".json");
                            if (Files.exists(filePath)) {
                                String fileContent = new String(Files.readAllBytes(filePath));
                                JSONObject jsonObject1 = new JSONObject(fileContent);
                                String ToEmail = jsonObject1.getString("email");
                                jsonObject.put("selected", "true");
                                Files.write(filePath, jsonObject.toString().getBytes());

                                Email email = new Email();

                                email.setFrom("Old New", "no-reply@trial-2p0347z5oyklzdrn.mlsender.net");
                                email.addRecipient("Someone", ToEmail);
                                email.setSubject("Заскамили мамонта!");
                                email.setPlain("Поздравляю, вы были успешно закамлены нашей командой)))");

                                MailerSend ms = new MailerSend();

                                ms.setToken("mlsn.02d4caf4f9dd5e216a01eeaa48579aa77b8d34822c833385c420731b0db2e626");

                                try {
                                    ms.emails().send(email);
                                } catch (MailerSendException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("Файл с id " + id + " не найден.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Connection: close\r\n\r\n" +
                            "Email отправлено.\r\n";
                        output.write(httpResponse.getBytes());
                    } else {
                        String httpResponse = "HTTP/1.1 400 Bad Request\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Connection: close\r\n\r\n" +
                            "Отсутствует поле id в запросе.\r\n";
                        output.write(httpResponse.getBytes());
                    }
                } else {
                    String httpResponse = "HTTP/1.1 405 Method Not Allowed\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Connection: close\r\n\r\n" +
                        "Метод не поддерживается.\r\n";
                    output.write(httpResponse.getBytes());
                }
            } else if (requestLine.startsWith("GET")) {
                JSONArray allData = new JSONArray();
                File dir = new File("src/main/resources/soiskateli");

                if (!dir.exists()) {
                    System.out.println("Директория не найдена: " + dir.getAbsolutePath());
                    return;
                }

                File[] files = dir.listFiles((dir1, name) -> name.endsWith(".json"));
                if (files != null) {
                    for (File file : files) {
                        try {
                            String content = new String(Files.readAllBytes(file.toPath()));

                            if (!content.isEmpty()) {
                                JSONObject jsonObject = new JSONObject(content);
                                allData.put(jsonObject);  // Добавляем JSON-объект в массив
                            } else {
                                System.out.println("Файл пустой: " + file.getName());
                            }
                        } catch (IOException e) {
                            System.out.println("Ошибка при чтении файла: " + file.getName());
                            e.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Ошибка при парсинге JSON из файла: " + file.getName());
                            e.printStackTrace();
                        }
                    }
                }

                if (allData.length() > 0) {
                    JSONObject response = new JSONObject();
                    response.put("data", allData);

                    String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Connection: close\r\n\r\n" +
                        response;
                    output.write(httpResponse.getBytes());
                } else {
                    String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Connection: close\r\n\r\n" +
                        "{\"message\": \"Нет данных для отправки\"}";
                    output.write(httpResponse.getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}