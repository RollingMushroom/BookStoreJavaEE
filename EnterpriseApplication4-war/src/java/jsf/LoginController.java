package jsf;

import entities.Users;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import sessions.UsersFacade;

@Named("loginController")
@SessionScoped
public class LoginController implements Serializable {
    
    @EJB
    private UsersFacade usersFacade;
    
    @PersistenceContext(unitName = "EnterpriseApplication4-warPU")
    private EntityManager em;
    
    private String username;
    private String password;
    private boolean loggedIn;
    private Users currentUser;
    
    public LoginController() {
    }
    
    public String login() {
        try {
            TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.username = :username AND u.password = :password", 
                Users.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            
            currentUser = query.getSingleResult();
            loggedIn = true;
            return "/index?faces-redirect=true";
        } catch (NoResultException e) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid username or password", null));
            return null;
        }
    }
    
    public String logout() {
        loggedIn = false;
        currentUser = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
    
    public Users getCurrentUser() {
        return currentUser;
    }
} 