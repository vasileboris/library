package com.espressoprogrammer.library.persistence.filesystem;

import com.espressoprogrammer.library.dto.Book;
import com.espressoprogrammer.library.persistence.BooksDao;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FilesystemBooksDao extends FilesystemAbstractDao<Book> implements BooksDao {

    @Override
    public List<Book> getUserBooks(String user) {
        return getUserItems(user);
    }

    @Override
    public String createUserBook(String user, Book book) {
        return createUserItem(user, book);
    }

    @Override
    public Optional<Book> getUserBook(String user, String uuid) {
        return getUserItem(user, uuid);
    }

    @Override
    public Optional<String> updateUserBook(String user, String uuid, Book book) {
        return updateUserItem(user, uuid, book);
    }

    @Override
    public Optional<String> deleteUserBook(String user, String uuid) {
        return deleteUserItem(user, uuid);
    }

    @Override
    protected Book createItem(String uuid, Book book) {
        return new Book(uuid,
            book.getIsbn10(),
            book.getIsbn13(),
            book.getTitle(),
            new ArrayList<>(book.getAuthors()),
            book.getPages());
    }

    @Override
    protected String getStorageFolder(String user) {
        return filesystemConfiguration.getLibraryFolder() + "/" + user + "/books";
    }

}
