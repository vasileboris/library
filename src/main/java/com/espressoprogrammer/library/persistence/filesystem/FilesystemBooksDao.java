package com.espressoprogrammer.library.persistence.filesystem;

import com.espressoprogrammer.library.dto.Book;
import com.espressoprogrammer.library.persistence.BooksDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Repository
public class FilesystemBooksDao extends FilesystemAbstractDao implements BooksDao {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public List<Book> getUserBooks(String user) {
        try {
            String booksFolder = createBooksFolderIfMissing(user);
            logger.debug("Looking for books into {}", booksFolder);

            return Files.list(Paths.get(booksFolder))
                .filter(p -> p.getFileName().toFile().getName().endsWith(FILE_EXTENSION))
                .map(p -> fromJson(p))
                .collect(toList());
        } catch(FilesystemDaoException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new FilesystemDaoException(ex);
        }
    }

    @Override
    public String createUserBook(String user, Book book) {
        try {
            String booksFolder = createBooksFolderIfMissing(user);
            logger.debug("Adding new book into {}", booksFolder);

            String uuid = UUID.randomUUID().toString();
            Files.write(Paths.get(booksFolder, uuid + FILE_EXTENSION), toJson(book).getBytes());
            return uuid;
        } catch(Exception ex) {
            throw new FilesystemDaoException(ex);
        }
    }

    @Override
    public Optional<Book> getUserBook(String user, String uuid) {
        try {
            String booksFolder = createBooksFolderIfMissing(user);
            logger.debug("Looking for book with uuid {} into {}", uuid, booksFolder);

            Path pathToBook = Paths.get(booksFolder, uuid + FILE_EXTENSION);
            if(pathToBook.toFile().exists()) {
                return Optional.of(fromJson(pathToBook));
            } else {
                return Optional.empty();
            }
        } catch(Exception ex) {
            throw new FilesystemDaoException(ex);
        }
    }

    @Override
    public String updateUserBook(String user, Book book) {
        return null;
    }

    private Book fromJson(Path path) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(path.toFile(), Book.class);
        } catch (IOException ex) {
            throw new FilesystemDaoException(ex);
        }
    }

    private String toJson(Book book) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            objectMapper.writeValue(out, book);
            return out.toString();
        } catch (IOException ex) {
            throw new FilesystemDaoException(ex);
        }
    }

}
