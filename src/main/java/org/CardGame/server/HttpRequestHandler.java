package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HttpRequestHandler {

    private HttpRequestParser requestParser;
    private UserRegistrationService userRegistrationService;
    private UserLoginService userLoginService;
    private HttpResponseSender responseSender;
    private PackageCreationService packageCreationService;
    private PackageTransactionService packageTransactionService;

    public HttpRequestHandler(DBAccess dbAccess, AuthDB authDB, UserDB userDB, PackageCreationDB packageCreationDB, PackageTransactionDB packageTransactionDB) {

        this.userRegistrationService = new UserRegistrationService(dbAccess, userDB);
        this.userLoginService = new UserLoginService(dbAccess, authDB);
        this.packageCreationService = new PackageCreationService(dbAccess, authDB, packageCreationDB);
        this.packageTransactionService = new PackageTransactionService(dbAccess, authDB, packageTransactionDB, userDB);
        this.responseSender = new HttpResponseSender();
        this.requestParser = new HttpRequestParser(null, null); // Später in handle() initialisieren
    }

    public void handle(BufferedReader in, OutputStream out, Socket clientSocket) throws IOException {
        // Initialisierung des HttpRequestParsers mit input und output Streams
        requestParser = new HttpRequestParser(in, out);

        // Parsen der Anfrage
        HttpRequest request = requestParser.parse();

        // Wenn die Anfrage ungültig oder fehlerhaft ist
        if (request == null) {
            return;
        }

        // Extrahiert die HTTP-Methode und den Pfad
        String method = request.getMethod();
        String path = request.getPath();
        String responseBody;
        int status;

        // Verarbeitet Post-Anfragen für "/users" und "/sessions"
        if ("POST".equalsIgnoreCase(method) && "/users".equals(path)) {
            responseBody = userRegistrationService.registerUser(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;

        } else if ("POST".equalsIgnoreCase(method) && "/sessions".equals(path)) {
            responseBody = userLoginService.authenticateUser(request);
            status = responseBody.startsWith("{\"token\"") ? 200 : 401;

        }else if ("POST".equalsIgnoreCase(method) && "/packages".equals(path)){
            responseBody = packageCreationService.startPackageCreation(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;
        }else if ("POST".equalsIgnoreCase(method) && "/transactions/packages".equals(path)){
            responseBody = packageTransactionService.startPackageTransaction(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;
        } else {
            responseBody = "{\"error\": \"Not Found\"}";
            status = 404; // Not Found
        }

        // Sendet die Antwort an den Client
        responseSender.send(out, responseBody, status);
    }

}
