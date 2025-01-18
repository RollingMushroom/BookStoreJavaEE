import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public void create() {
    // Check for duplicate user
    if (isDuplicateUser(usersController.selected)) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Duplicate user found!", "A user with the same username or email already exists."));
        return;
    }
    
    // existing code to create the user...
}

private boolean isDuplicateUser(User user) {
    // Logic to check if the user already exists in the database based on username or email
    // Example query (pseudo-code):
    // return userService.findByUsername(user.getUsername()) != null || userService.findByEmail(user.getEmail()) != null;
} 