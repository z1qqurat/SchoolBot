package org.teodor;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.teodor.bot.SchoolScheduleBot;
import org.teodor.config.ConfigManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

@Log4j2
public class Application {
    private static final String BOT_TOKEN = ConfigManager.getConfig().getBotToken();

    public static void main(String[] args) {

        int port = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("5050"));

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", exchange -> {
            String response = "School bot is running";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });
        server.start();
        log.info("HTTP server started on port {}", port);

        try (TelegramBotsLongPollingApplication botsApplication =
                     new TelegramBotsLongPollingApplication()) {

            skipOldUpdates();

            botsApplication.registerBot(
                    BOT_TOKEN,
                    new SchoolScheduleBot(BOT_TOKEN)
            );

            log.info("SchoolBot started — old updates ignored");
            Thread.currentThread().join();

        } catch (Exception e) {
            log.error("Bot startup error", e);
        }

    }

    private static void skipOldUpdates() throws Exception {
        TelegramClient client = new OkHttpTelegramClient(BOT_TOKEN);

        GetUpdates getUpdates = GetUpdates.builder()
                .limit(100)
                .build();

        List<Update> updates;
        int lastUpdateId = 0;

        do {
            updates = client.execute(getUpdates);
            if (!updates.isEmpty()) {
                lastUpdateId = updates.get(updates.size() - 1).getUpdateId() + 1;
                getUpdates.setOffset(lastUpdateId);
            }
        } while (!updates.isEmpty());
    }

}