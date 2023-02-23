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

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Server extends BasicServer {
    private final static Configuration freemarker = initFreeMarker();
    private static Book bookSearch;


    public Server(String host, int port) throws IOException, SQLException {
        super(host, port);
        registerGet("/", this::booksHandler);
        registerGet("/sample", this::freemarkerSampleHandler);
        registerGet("/books", this::booksHandler);
        registerGet("/book", this::bookHandler);
        registerGet("/user", this::userHandler);
//        registerGet("/login",this::searchGet);
//        registerPost("/login",this::searchPost);
//        registerGet("/delete",this::deleteGet);
//        registerPost("/delete",this::deletePost);
//        registerGet("/input",this::inputGet);
//        registerPost("/input",this::inputPost);
//        registerGet("/update",this::updateGet);
//        registerPost("/update",this::updatePost);
        registerGet("/register",this::registerGet);
        registerPost("/register",this::registerPost);
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
    private void registerGet(HttpExchange exchange) {
        Path path = makeFilePath("register.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void registerPost(HttpExchange exchange) throws SQLException {
        String raw = getBody(exchange);
        List<Optional<String>> parsed = Utils.parseInputEncoded(raw,"&");
        List<String> stats = new ArrayList<>();
        for(int i = 0; i< parsed.size();i++){
            stats.add(parsed.get(i).toString().substring(parsed.get(i).toString().indexOf("=")+1, parsed.get(i).toString().indexOf("]")));
        }
        FileService.addUser(stats);
        redirect303(exchange,"/user");
    }

    private void freemarkerSampleHandler(HttpExchange exchange) {
        renderTemplate(exchange, "sample.html", getSampleDataModel());
    }
    private void booksHandler(HttpExchange exchange) throws SQLException {
        renderTemplate(exchange, "books.html", getBooksDataModel());
    }

    private void bookHandler(HttpExchange exchange) throws SQLException {
        renderTemplate(exchange, "book.html", getBookDataModel());
    }
    private void userHandler(HttpExchange exchange) throws SQLException {
        renderTemplate(exchange, "user.html", getUserDataModel());
    }

    private Object getBookDataModel() {
        return new SingleBookDataModel(bookSearch);
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

    private BookDataModel getBooksDataModel() throws SQLException {
        return new BookDataModel();
    }
    private UserDataModel getUserDataModel() throws SQLException {
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
    private void updateGet(HttpExchange exchange) {
        Path path = makeFilePath("update.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void inputGet(HttpExchange exchange) {
        Path path = makeFilePath("input.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void searchGet(HttpExchange exchange) {
        Path path = makeFilePath("login.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void deleteGet(HttpExchange exchange) {
        Path path = makeFilePath("delete.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
}
