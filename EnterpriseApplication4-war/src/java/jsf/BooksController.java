package jsf;

import entities.Books;
import jsf.util.JsfUtil;
import jsf.util.PaginationHelper;
import sessions.BooksFacade;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

@Named("booksController")
@SessionScoped
public class BooksController implements Serializable {

    private Books current;
    private DataModel items = null;
    @EJB
    private sessions.BooksFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private String selectedCategory;

    public BooksController() {
    }

    public Books getSelected() {
        if (current == null) {
            current = new Books();
            selectedItemIndex = -1;
        }
        return current;
    }

    public Books getCurrent() {
        return current;
    }

    public void setCurrent(Books current) {
        this.current = current;
    }

    private BooksFacade getFacade() {
        return ejbFacade;
    }
    
    public List<String> getCategories() {
        return getFacade().getEntityManager()
                .createNamedQuery("Books.findDistinctCategories", String.class)
                .getResultList();
    }
    
    public String getSelectedCategory() {
        return selectedCategory;
    }
    
    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        // Reset pagination when category changes
        pagination = null;
        items = null;
    }
    
    public List<Books> getBooksByCategory(String category) {
        return getFacade().getEntityManager()
                .createNamedQuery("Books.findByCategory", Books.class)
                .setParameter("category", category)
                .getResultList();
    }
    
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {
                @Override
                public int getItemsCount() {
                    if (selectedCategory != null && !selectedCategory.isEmpty()) {
                        return getFacade().getEntityManager()
                                .createNamedQuery("Books.findByCategory", Books.class)
                                .setParameter("category", selectedCategory)
                                .getResultList().size();
                    }
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    if (selectedCategory != null && !selectedCategory.isEmpty()) {
                        return new ListDataModel(getFacade().getEntityManager()
                                .createNamedQuery("Books.findByCategory", Books.class)
                                .setParameter("category", selectedCategory)
                                .setMaxResults(getPageSize())
                                .setFirstResult(getPageFirstItem())
                                .getResultList());
                    }
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(),
                        getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }
    
    public String prepareList() {
        pagination = null;
        items = null;
        return "List";
    }
    
    public String prepareView() {
        current = (Books) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Books();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            // Check if book with same ID or title already exists
            Books existingBookById = getFacade().find(current.getBookId());
            Books existingBookByTitle = getFacade().getEntityManager()
                    .createQuery("SELECT b FROM Books b WHERE LOWER(b.title) = LOWER(:title)", Books.class)
                    .setParameter("title", current.getTitle())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
            
            if (existingBookById != null) {
                JsfUtil.addErrorMessage("Book with ID " + current.getBookId() + " already exists");
                return null;
            }
            
            if (existingBookByTitle != null) {
                JsfUtil.addErrorMessage("The book with this title already exists");
                return null;
            }
            
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("BooksCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Books) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/books/Edit";
    }

    public String prepareEdit(Books book) {
        current = book;
        return "/books/Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("BooksUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        try {
            if (current == null) {
                // If current is null, try to get the book ID from request parameter
                String bookId = FacesContext.getCurrentInstance()
                        .getExternalContext().getRequestParameterMap().get("id");
                if (bookId != null) {
                    current = getFacade().find(Integer.valueOf(bookId));
                }
            }
            if (current == null) {
                // If still null, try to get from data table row
                current = (Books) getItems().getRowData();
                selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
            }
            
            if (current != null) {
                performDestroy();
                pagination = null;
                items = null;
            } else {
                JsfUtil.addErrorMessage("Could not find book to delete");
            }
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error deleting book: " + e.getMessage());
            return null;
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("BooksDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public List<Books> getRecentBooks() {
        return getFacade().getEntityManager()
                .createQuery("SELECT b FROM Books b ORDER BY b.bookId DESC", Books.class)
                .setMaxResults(5)
                .getResultList();
    }

    public List<Books> getAllBooks() {
        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            return getFacade().getEntityManager()
                    .createNamedQuery("Books.findByCategory", Books.class)
                    .setParameter("category", selectedCategory)
                    .getResultList();
        }
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Books.class)
    public static class BooksControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BooksController controller = (BooksController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "booksController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Books) {
                Books o = (Books) object;
                return getStringKey(o.getBookId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Books.class.getName());
            }
        }
    }
}
