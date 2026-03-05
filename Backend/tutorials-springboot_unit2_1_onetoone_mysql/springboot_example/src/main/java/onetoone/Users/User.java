package onetoone.Users;

import jakarta.persistence.*;

import onetoone.Laptops.Laptop;

import java.time.LocalDate;

/**
 * 
 * @author Chris Lopez
 * 
 */ 

@Entity
@Table(name = "users")
public class User {
    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private String accountName;
    private String userPassword;
    private LocalDate birthDate;
    private boolean isEnabled;


    public User() {
        this.isEnabled = true;
    }


    public User(String accountName, String userPassword, LocalDate birthDate) {

        this.accountName = accountName;
        this.userPassword = userPassword;
        this.birthDate = birthDate;
        this.isEnabled = true;

    }

    public int getUserId() {
        return id;
    }

    public void setUserId(int userId) {

        this.id = userId;
    }

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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {

        this.birthDate = birthDate;
    }

    public boolean isEnabled() {

        return isEnabled;

    }

    public void setEnabled(boolean enabled){

        isEnabled = enabled;
}
 @Override
 public String toString(){
        return "User {" +
                "UserId =" + id + ", accountName='" + accountName + '\'' + ", birthDate=" + birthDate + " isEnabled=" +
                isEnabled + '}';
 }

 /*
    /*
     * @OneToOne creates a relation between the current entity/table(Laptop) with the entity/table defined below it(User)
     * cascade is responsible propagating all changes, even to children of the class Eg: changes made to laptop within a user object will be reflected
     * in the database (more info : https://www.baeldung.com/jpa-cascade-types)
     * @JoinColumn defines the ownership of the foreign key i.e. the user table will have a field called laptop_id

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "laptop_id")
    private Laptop laptop;

    public User(String name, String emailId) {
        this.name = name;
        this.emailId = emailId;
        this.ifActive = true;
    }

    public User() {
    }

    // =============================== Getters and Setters for each field ================================== //

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmailId(){
        return emailId;
    }

    public void setEmailId(String emailId){
        this.emailId = emailId;
    }

    public boolean getIsActive(){
        return ifActive;
    }

    public void setIfActive(boolean ifActive){
        this.ifActive = ifActive;
    }

    public Laptop getLaptop(){
        return laptop;
    }

    public void setLaptop(Laptop laptop){
        this.laptop = laptop;
    }

    */
    
}
