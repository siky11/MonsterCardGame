package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;
import org.CardGame.model.HttpRequestInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HttpRequestHandler {

    public HttpRequestParser requestParser;
    public UserRegistrationService userRegistrationService;
    public UserLoginService userLoginService;
    public HttpResponseSender responseSender;
    public PackageCreationService packageCreationService;
    public PackageTransactionService packageTransactionService;
    public UserStackService userStackService;
    public UserShowDeckService userShowDeckService;
    public UserConfigureDeckService userConfigureDeckService;
    public UserProfileService userProfileService;

    public HttpRequestHandler(DBAccess dbAccess, AuthDBInterface authDB, UserDB userDB, PackageCreationDB packageCreationDB, PackageTransactionDB packageTransactionDB, CardDB cardDB, DeckDB deckDB) {

        this.userRegistrationService = new UserRegistrationService(dbAccess, userDB);
        this.userLoginService = new UserLoginService(dbAccess, authDB);
        this.packageCreationService = new PackageCreationService(authDB, packageCreationDB);
        this.userStackService = new UserStackService(dbAccess, authDB, cardDB, userDB);
        this.userShowDeckService = new UserShowDeckService(dbAccess, authDB, deckDB, userDB);
        this.userConfigureDeckService = new UserConfigureDeckService(deckDB, authDB, userDB);
        this.userProfileService = new UserProfileService(dbAccess, authDB, userDB);
        this.packageTransactionService = new PackageTransactionService(dbAccess, authDB, packageTransactionDB, userDB);
        this.responseSender = new HttpResponseSender();
        this.requestParser = new HttpRequestParser(null, null); // Später in handle() initialisieren
    }

    public void handle(BufferedReader in, OutputStream out, Socket clientSocket) throws IOException {
        // Initialisierung des HttpRequestParsers mit input und output Streams
        requestParser = new HttpRequestParser(in, out);

        // Parsen der Anfrage
        HttpRequestInterface request = requestParser.parse();

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

        } else if("GET".equalsIgnoreCase(method) && "/cards".equals(path)){
            responseBody = userStackService.getStackCards(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;

        }else if("GET".equalsIgnoreCase(method) && "/deck".equals(path)){
            responseBody = userShowDeckService.getDeckCards(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;

        }else if("GET".equalsIgnoreCase(method) && "/deck?format=plain".equals(path)){
            responseBody = userShowDeckService.getDeckCardsPlain(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;

        }else if("GET".equalsIgnoreCase(method) && path.matches("/users/.+")) {
            String username = path.split("/")[2]; // Den Benutzernamen aus dem URL-Pfad extrahieren
            responseBody = userProfileService.getUserProfile(request, username); // Methode, die den Benutzer abrufen wird
            status = responseBody.startsWith("{\"error\"") ? 400 : 200;

        }else if("GET".equalsIgnoreCase(method) && "/stats".equals(path)) {
            responseBody = userProfileService.getUserStats(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 200;

        }else if("GET".equalsIgnoreCase(method) && "/scoreboard".equals(path)) {
            responseBody = userProfileService.getScoreboard(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;

        }else if("PUT".equalsIgnoreCase(method) && "/deck".equals(path)){
            responseBody = userConfigureDeckService.configureDeck(request);
            status = responseBody.startsWith("{\"error\"") ? 400 : 201;

        }else if("PUT".equalsIgnoreCase(method) && path.matches("/users/.+")) {
            String username = path.split("/")[2];
            responseBody = userProfileService.editProfile(request, username);
            status = responseBody.startsWith("{\"error\"") ? 400 : 200;

        }else {
            responseBody = "{\"error\": \"Not Found\"}";
            status = 404; // Not Found
        }

        // Sendet die Antwort an den Client
        responseSender.send(out, responseBody, status);
    }

}
