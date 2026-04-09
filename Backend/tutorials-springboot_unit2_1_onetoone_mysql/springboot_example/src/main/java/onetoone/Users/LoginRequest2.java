package onetoone.Users;

//import onetoone.Laptops.Laptop;


/**
 * @author Matthew Smith
 */

class LoginRequest2 {

    private String accountName;
    private String userPassword;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
