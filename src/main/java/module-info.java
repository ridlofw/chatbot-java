module com.kampuspintar.chatbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires atlantafx.base;
    requires com.google.gson;
    requires google.genai;
    requires com.google.common;

    opens com.kampuspintar.chatbot to javafx.fxml;
    opens com.kampuspintar.chatbot.controller to javafx.fxml;
    opens com.kampuspintar.chatbot.model to com.google.gson;

    exports com.kampuspintar.chatbot;
    exports com.kampuspintar.chatbot.controller;
    exports com.kampuspintar.chatbot.model;
    exports com.kampuspintar.chatbot.strategy;
    exports com.kampuspintar.chatbot.util;
}
