package kz.attractor.java.lesson44;

import java.time.LocalDate;

public class User {
    private String name;
    private String surname;
    private String book;
    private String date_start;
    private String date_end;
    private String password;
    private String email;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String name, String surname, String email, String password) {
        this.name = name;
        this.surname = surname;
        this.book = "";
        this.date_start = "";
        this.date_end = "";
        this.password = password;
        this.email = email;
    }
    public User(String name, String surname, String book, String date_start, String date_end) {
        this.name = name;
        this.surname = surname;
        this.book = book;
        this.date_start = date_start;
        this.date_end = date_end;
        this.password = "12345";
        this.email = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getDate_start() {
        return date_start;
    }

    public void setDate_start(String date_start) {
        this.date_start = date_start;
    }

    public String getDate_end() {
        return date_end;
    }

    public void setDate_end(String date_end) {
        this.date_end = date_end;
    }
}
