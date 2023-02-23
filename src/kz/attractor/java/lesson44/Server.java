package kz.attractor.java.lesson44;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import kz.attractor.java.server.BasicServer;
import kz.attractor.java.server.ContentType;
import kz.attractor.java.server.ResponseCodes;
import kz.attractor.java.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Server extends BasicServer {
    private final static Configuration freemarker = initFreeMarker();
    private static User userProfile = new User("", "", "", "");


    public Server(String host, int port) throws IOException, SQLException {
        super(host, port);
        registerGet("/", this::booksHandler);
        registerGet("/books", this::booksHandler);
        registerGet("/sample", this::freemarkerSampleHandler);
        registerGet("/failed", this::failedHandler);
        registerGet("/success", this::successHandler);
        registerGet("/failed2", this::failedLoginHandler);
        registerGet("/success2", this::successLoginHandler);
        registerGet("/users", this::usersHandler);
        registerGet("/user", this::userHandler);
        registerGet("/accounts", this::accountsHandler);
        registerGet("/login",this::loginGet);
        registerPost("/login",this::loginPost);
        registerGet("/registration",this::registrationGet);
        registerPost("/registration",this::registrationPost);
    }

    private void userHandler(HttpExchange exchange) {
        renderTemplate(exchange, "user.html", getUserDataModel());
    }
    private Object getUserDataModel() {
        return new SingleUserDataModel(userProfile);
    }

    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            cfg.setDirectoryForTemplateLoading(new File("data"));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void loginGet(HttpExchange exchange) {
        Path path = makeFilePath("login.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void loginPost(HttpExchange exchange){
        try{
            String raw = getBody(exchange);
            List<Optional<String>> parsed = Utils.parseInputEncoded(raw,"&");
            List<String> stats = new ArrayList<>();
            for(int i = 0; i< parsed.size();i++){
                stats.add(parsed.get(i).toString().substring(parsed.get(i).toString().indexOf("=")+1, parsed.get(i).toString().indexOf("]")));
            }
            List<User> users = FileService.readUserFile();
            boolean contains = false;
            for (User user : users) {
                if (user.getEmail() != null && user.getPassword() != null){
                    if (user.getEmail().equals(stats.get(0)) && user.getPassword().equals(stats.get(1))) {
                        contains = true;
                        userProfile = user;
                    }
                }
            }
            if (!contains){
                redirect303(exchange,"/failed2");
            }else {
                redirect303(exchange,"/success2");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void registrationGet(HttpExchange exchange) {
        Path path = makeFilePath("registration.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void registrationPost(HttpExchange exchange){
      try{
          String raw = getBody(exchange);
          List<Optional<String>> parsed = Utils.parseInputEncoded(raw,"&");
          List<String> stats = new ArrayList<>();
          for(int i = 0; i< parsed.size();i++){
              stats.add(parsed.get(i).toString().substring(parsed.get(i).toString().indexOf("=")+1, parsed.get(i).toString().indexOf("]")));
          }
          List<User> users = FileService.readUserFile();
          boolean contains = false;
          for (User user : users) {
             if (user.getEmail() != null){
                 if (user.getEmail().equals(stats.get(2))) {
                     contains = true;
                 }
             }
          }
          if (contains){
              redirect303(exchange,"/failed");
          }else {
              FileService.addUser(stats);
              redirect303(exchange,"/success");
          }
      }catch (Exception e){
          e.printStackTrace();
      }
    }

    private void freemarkerSampleHandler(HttpExchange exchange) {
        renderTemplate(exchange, "sample.html", getSampleDataModel());
    }
    private void booksHandler(HttpExchange exchange) {
        renderTemplate(exchange, "books.html", getBooksDataModel());
    }
    private void successLoginHandler(HttpExchange exchange) {
        renderTemplate(exchange, "success2.html", getUsersDataModel());
    }
    private void failedLoginHandler(HttpExchange exchange) {
        renderTemplate(exchange, "failed2.html", getUsersDataModel());
    }

    private void successHandler(HttpExchange exchange) throws SQLException {
        renderTemplate(exchange, "success.html", getUsersDataModel());
    }
    private void failedHandler(HttpExchange exchange) throws SQLException {
        renderTemplate(exchange, "failed.html", getUsersDataModel());
    }
    private void usersHandler(HttpExchange exchange) throws SQLException {
        renderTemplate(exchange, "users.html", getUsersDataModel());
    }
    private void accountsHandler(HttpExchange exchange) throws SQLException {
        renderTemplate(exchange, "accounts.html", getUsersDataModel());
    }

    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
    try {
        Template temp = freemarker.getTemplate(templateFile);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            temp.process(dataModel, writer);
            writer.flush();
            var data = stream.toByteArray();
            sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
        }
    } catch (IOException | TemplateException e) {
        e.printStackTrace();
    }
}
    private SampleDataModel getSampleDataModel() {
        return new SampleDataModel();
    }
    private BookDataModel getBooksDataModel(){
        return new BookDataModel();
    }
    private UserDataModel getUsersDataModel(){
        return new UserDataModel();
    }

//    private void deletePost(HttpExchange exchange) {
//        String raw = getBody(exchange);
//        String parsed = Utils.parseUrlEncodedBook(raw);
//        try{
//            parsed = parsed.replace("Optional[id=","");
//            parsed = parsed.replace("]","");
//            BaseService.deleteOne(parsed);
//            BaseService.getAllBooks();
//            redirect303(exchange,"/books");
//        }catch (Exception e){
//            e.printStackTrace();
//            redirect303(exchange,"/delete");
//        }
//    }
//    private void searchPost(HttpExchange exchange) {
//        String raw = getBody(exchange);
//        String parsed = Utils.parseUrlEncodedBook(raw);
//        try{
//            parsed = parsed.replace("Optional[id=","");
//            parsed = parsed.replace("]","");
//            Integer index = Integer.parseInt(parsed);
//            BaseService.getAllBooks();
//            for(int i = 0; i < FileService.readBookFile().size(); i++){
//                if (FileService.readBookFile().get(i).getId() == index){
//                    bookSearch = FileService.readBookFile().get(i);
//                }
//            }
//            redirect303(exchange,"/book");
//        }catch (Exception e){
//            e.printStackTrace();
//            redirect303(exchange,"/login");
//        }
//    }

//    private void inputPost(HttpExchange exchange) throws SQLException {
//        String raw = getBody(exchange);
//        List<Optional<String>> parsed = Utils.parseInputEncoded(raw,"&");
//        List<String> stats = new ArrayList<>();
//        for(int i = 0; i< parsed.size();i++){
//            stats.add(parsed.get(i).toString().substring(parsed.get(i).toString().indexOf("=")+1, parsed.get(i).toString().indexOf("]")));
//        }
//        BaseService.postOne(stats);
//        BaseService.getAllBooks();
//        redirect303(exchange,"/books");
//    }
//    private void updatePost(HttpExchange exchange) throws SQLException {
//        String raw = getBody(exchange);
//        List<Optional<String>> parsed = Utils.parseUpdateEncoded(raw,"&");
//        List<String> stats = new ArrayList<>();
//        for(int i = 0; i< parsed.size();i++){
//            stats.add(parsed.get(i).toString().substring(parsed.get(i).toString().indexOf("=")+1, parsed.get(i).toString().indexOf("]")));
//        }
//        BaseService.updateOne(stats);
//        BaseService.getAllBooks();
//        redirect303(exchange,"/books");
//    }
//    private void updateGet(HttpExchange exchange) {
//        Path path = makeFilePath("update.html");
//        sendFile(exchange, path, ContentType.TEXT_HTML);
//    }
//    private void inputGet(HttpExchange exchange) {
//        Path path = makeFilePath("input.html");
//        sendFile(exchange, path, ContentType.TEXT_HTML);
//    }
//    private void searchGet(HttpExchange exchange) {
//        Path path = makeFilePath("login.html");
//        sendFile(exchange, path, ContentType.TEXT_HTML);
//    }
//    private void deleteGet(HttpExchange exchange) {
//        Path path = makeFilePath("delete.html");
//        sendFile(exchange, path, ContentType.TEXT_HTML);
//    }
}
