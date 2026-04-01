# MarketNest E-commerce API

This is a comprehensive and secure RESTful API for an e-commerce platform called MarketNest. Built with Java and Spring Boot, it provides all the essential backend functionalities for an online store, including user management, product catalog, and order processing.

## Table of Contents

- [About the Project](#about-the-project)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Running Tests](#running-tests)

## About the Project

MarketNest is a robust e-commerce backend designed to be scalable and maintainable. It follows best practices for REST API design and includes features like JWT-based authentication, secure payment processing with Stripe, and cloud-based image storage with Cloudinary. The project is well-structured with a clear separation of concerns, making it easy to extend and customize.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.3.5
- **Database:** PostgreSQL
- **Security:** Spring Security, JWT (JSON Web Tokens)
- **API Documentation:** SpringDoc (OpenAPI/Swagger)
- **Image Storage:** Cloudinary
- **Payments:** Stripe
- **Testing:** JUnit, Mockito, Testcontainers
- **Build Tool:** Maven
- **Libraries:** Lombok, MapStruct, iTextPDF

## Features

-   **User Authentication:** Secure user registration and login with JWT.
-   **Product Management:** Full CRUD functionality for products, categories, and variants.
-   **Image Upload:** Seamless image uploads to Cloudinary.
-   **Shopping Cart:** Add, update, and remove items from the cart.
-   **Order Processing:** Create and manage orders.
-   **Payment Integration:** Secure payment processing with Stripe.
-   **API Documentation:** Interactive API documentation with Swagger UI.

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

-   Java 21
-   Maven
-   PostgreSQL

### Installation

1.  **Clone the repo**
    ```sh
    git clone https://github.com/your_username/MarketNest.git
    ```
2.  **Navigate to the project directory**
    ```sh
    cd MarketNest
    ```
3.  **Install Maven dependencies**
    ```sh
    mvn install
    ```
4.  **Create a PostgreSQL database** named `marketnest`
5.  **Configure the application** by creating an `application.properties` file in `src/main/resources` and adding the necessary properties (see [Configuration](#configuration)).
6.  **Run the application**
    ```sh
    mvn spring-boot:run
    ```

## API Endpoints

The API is documented using OpenAPI, and you can access the interactive Swagger UI at `/swagger-ui.html`.

Here are some of the main endpoints:

-   `POST /api/auth/register`: Register a new user.
-   `POST /api/auth/login`: Authenticate a user and get a JWT.
-   `GET /api/products`: Get a list of all products.
-   `GET /api/products/{id}`: Get a single product by ID.
-   `POST /api/products`: Create a new product (Admin only).
-   `GET /api/categories`: Get all categories.
-   `POST /api/cart`: Add an item to the cart.

## Configuration

You will need to provide the following configuration details in your `application.properties` file:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/marketnest
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# JWT
jwt.secret=your_jwt_secret

# Cloudinary
cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_api_key
cloudinary.api_secret=your_api_secret

# Stripe
stripe.api.key=your_stripe_api_key
