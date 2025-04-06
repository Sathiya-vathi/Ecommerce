# E-Commerce Website (Spring Boot + RBAC)

A Spring Boot-based eCommerce backend with **Role-Based Access Control**, supporting secure user authentication, product management, cart, and order functionalities.

---

## Features

- Role-based login for `USER` and `ADMIN`
- Secure password encryption using BCrypt
- Product and category CRUD operations
- Cart operations (add/remove/clear)
- Order placement and status management
- Review system
- Invoice generation
- RESTful APIs

---

## How to Run the Application (IDE-based)

### Prerequisites

- Java 17 or above
- Maven
- MySQL Server
- Eclipse IDE or IntelliJ IDEA

---

### Steps to Run in **Eclipse**

1. **Open Eclipse** and select your workspace.

2. **Import the Project:**
   - Go to `File` → `Import` → `Maven` → `Existing Maven Projects`
   - Select the project root folder
   - Click **Finish**

3. **Configure MySQL DB:**
   - Open `src/main/resources/application.properties`
   - Update your DB credentials:

     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
     spring.datasource.username=your_mysql_username
     spring.datasource.password=your_mysql_password
     spring.jpa.hibernate.ddl-auto=update
     ```

4. **Create the Database:**
   - Create a database in MySQL with the name `website` (or your preferred name)

     ```sql
     CREATE DATABASE website;
     ```

5. **Run the Application:**
   - Right-click on the main class (e.g., `WebsiteApplication.java`)
   - Select `Run As` → `Spring Boot App`

6. **Test the APIs:**
   - Use Postman or browser to hit the API:  
     `http://localhost:8080/api/users/register`



## Key API Endpoints

| Endpoint                  | Method | Role         | Description                        |
|---------------------------|--------|--------------|------------------------------------|
| `/api/users/register`     | POST   | Public       | Register a new user                |
| `/api/users/login`        | POST   | Public       | Login user                         |
| `/api/products`           | GET    | Public       | View all products                  |
| `/api/products/add`       | POST   | ADMIN        | Add a product                      |
| `/api/cart/add`           | POST   | USER         | Add item to cart                   |
| `/orders/place`           | POST   | USER         | Place an order                     |
| `/orders/all`             | GET    | ADMIN        | View all orders                    |

---

## Notes

- Default users are assigned the role `ROLE_USER`
- Admins must be added manually in DB or seeded via SQL
- Stateless authentication (no sessions used)
- Logout is handled on the client-side (credentials/token are cleared)

---

## To-Do (Future Enhancements)

- JWT Authentication
- Admin dashboard UI


## Author

Developed by Sathiya-vathi
