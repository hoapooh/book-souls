package com.example.book_souls_project.api;

import android.content.Context;

import com.example.book_souls_project.api.repository.AuthRepository;
import com.example.book_souls_project.api.repository.BookRepository;
import com.example.book_souls_project.api.repository.UserRepository;
import com.example.book_souls_project.api.repository.CategoryRepository;
import com.example.book_souls_project.api.repository.PublisherRepository;
import com.example.book_souls_project.api.service.AuthenticationService;

/**
 * Central API Repository Factory
 * Provides instances of specific repositories
 */
public class ApiRepository {
    private static ApiRepository instance;
    private Context context;
    
    // Repository instances
    private AuthRepository authRepository;
    private BookRepository bookRepository;
    private CategoryRepository categoryRepository;
    private PublisherRepository publisherRepository;

    private ApiRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized ApiRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ApiRepository(context);
        }
        return instance;
    }

    // Get AuthRepository instance
    public AuthRepository getAuthRepository() {
        if (authRepository == null) {
            authRepository = new AuthRepository(context);
        }
        return authRepository;
    }

    // Get BookRepository instance
    public BookRepository getBookRepository() {
        if (bookRepository == null) {
            bookRepository = new BookRepository(context);
        }
        return bookRepository;
    }

    // Get UserRepository instance
    public UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new UserRepository(context);
        }
        return userRepository;
    }

    // Get CategoryRepository instance
    public CategoryRepository getCategoryRepository() {
        if (categoryRepository == null) {
            categoryRepository = new CategoryRepository(context);
        }
        return categoryRepository;
    }

    // Get PublisherRepository instance
    public PublisherRepository getPublisherRepository() {
        if (publisherRepository == null) {
            publisherRepository = new PublisherRepository(context);
        }
        return publisherRepository;
    }

    // Deprecated methods for backward compatibility
    @Deprecated
    public static ApiService getRandomService() {
        throw new UnsupportedOperationException("Use getInstance(context) pattern instead");
    }

    @Deprecated
    public static AuthenticationService getAuthenticationService() {
        throw new UnsupportedOperationException("Use getInstance(context).getAuthRepository() instead");
    }
}
