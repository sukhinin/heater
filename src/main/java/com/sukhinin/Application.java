package com.sukhinin;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Application {
    public static void main(String[] args) {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        int threads = Integer.getInteger("threads", 4);
        Gauge.builder("app.heater.threads", () -> threads).register(registry);
        for (int i = 1; i <= threads; i++) {
            Thread thread = new Thread(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("Thread '" + threadName + "' is starting to heat the world");
                Counter counter = Counter.builder("app.heater.counter")
                        .tag("thread", threadName)
                        .register(registry);
                while (true) {
                    counter.increment();
                }
            });
            thread.setDaemon(true);
            thread.setName("worker-" + i);
            thread.start();
        }

        try {
            int port = Integer.getInteger("port", 8080);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", httpExchange -> {
                String response = registry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
