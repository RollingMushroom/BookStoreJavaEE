import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.List;

public void create() {
    // Check for duplicate book
    if (isDuplicateBook(booksController.selected)) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Duplicate book found!", "A book with the same title already exists."));
        return;
    }
    
    // existing code to create the book...
}

private boolean isDuplicateBook(Book book) {
    // Logic to check if the book already exists in the database based on title
    // Example query (pseudo-code):
    // return bookService.findByTitle(book.getTitle()) != null;
}

public List<Book> getRecentBooks() {
    // Logic to retrieve the most recently added books from the database
    // Example query (pseudo-code):
    // return bookService.findRecentBooks(); // This should return a list of recent books
} 