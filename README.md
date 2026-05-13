# MarketNest

A scalable e-commerce platform built with Java/Spring Boot, featuring comprehensive product management, secure payment
processing, real-time observability, and a complete shopping experience with order management, reviews, and wishlists.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [Backend Setup](#backend-setup)
- [Running the Project](#running-the-project)
- [API Documentation](#api-documentation)

## Overview

MarketNest is an enterprise-grade e-commerce platform built with Spring Boot. It provides comprehensive services for
product catalog management, shopping cart operations, order processing, payment handling via Stripe, customer reviews,
wishlists, and administrative functionality including coupon management and analytics.

## Features

- **Product Management** - Comprehensive product catalog with variants, categories, and images
- **Shopping Cart** - Full-featured shopping cart with add/remove/update operations
- **Order Processing** - Complete order lifecycle management and tracking
- **Payment Processing** - Secure Stripe integration for payments and webhooks
- **Product Reviews & Ratings** - Customer review system with ratings
- **Wishlist Management** - Save favorite products for later
- **Coupon System** - Admin-managed coupons with validation and discount application
- **Address Management** - Multiple address handling for users
- **Authentication & Authorization** - Secure user authentication with role-based access
- **Analytics** - Order and sales analytics for business insights
- **Cloudinary Integration** - Cloud-based image management and optimization
- **Validation Framework** - Custom validators for data integrity
- **Aspect-Oriented Programming** - AOP-based logging and cross-cutting concerns
- **API Documentation** - Built-in Swagger/OpenAPI support via SpringDoc
- **Multi-Environment Support** - Dev, UAT, and Production configurations
- **Security & Filters** - Custom security filters and OAuth2 support

## Project Structure

```
MarketNest/
  src/
    main/
      java/com/marketnest/ecommerce/
        annotation/              # Custom annotations for validation
        aspects/                 # AOP aspects for logging and cross-cutting concerns
        config/                  # Configuration classes
          ApplicationContextProvider.java
          CloudinaryConfig.java
          MapperConfig.java
          OpenAPIConfig.java
          SecurityConfig.java
          StripeConfig.java
        controller/              # REST controllers
          AddressController.java
          AnalyticsController.java
          AuthController.java
          CartController.java
          CategoryController.java
          CouponAdminController.java
          CouponController.java
          OrderController.java
          PaymentController.java
          ProductController.java
          ProductImageController.java
          ReviewController.java
          StripeWebhookController.java
          UserController.java
          VariantController.java
          WishlistController.java
        dto/                     # Data Transfer Objects
        event/                   # Event handling and publishing
        exception/               # Custom exceptions
        filter/                  # Security and request filters
        mapper/                  # Object mapping (MapStruct)
        model/                   # JPA entities
        repository/              # Data access layer
        security/                # Security components
        service/                 # Business logic layer
        util/                    # Utility classes
        validation/              # Validation logic
        MarketNestApplication.java  # Application entry point
      resources/
        application.properties        # Default configuration
        application-dev.properties    # Development profile
        application-uat.properties    # UAT profile
        application-prod.properties   # Production profile
    test/
      java/com/marketnest/ecommerce/
        controller/              # Controller tests
        service/                 # Service tests
        repository/              # Repository tests
  pom.xml                         # Maven configuration
  mvnw / mvnw.cmd                 # Maven wrapper scripts
```

## Technologies Used

- **Backend**: Java 21, Spring Boot 3.x, Spring Framework
- **Web**: Spring Web (REST APIs), Spring MVC
- **Security**: Spring Security, OAuth2, JWT support
- **Data Access**: Spring Data JPA, Hibernate
- **Database**: PostgreSQL / MySQL
- **Payment Processing**: Stripe API
- **Image Management**: Cloudinary
- **Object Mapping**: MapStruct, Lombok
- **API Documentation**: SpringDoc OpenAPI / Swagger
- **Configuration**: Spring Cloud Config support
- **Event Handling**: Spring Events, Event publishing
- **AOP**: Spring AOP for cross-cutting concerns
- **Validation**: Spring Validation, Custom validators
- **Build Tool**: Maven
- **Containerization**: Docker support
- **Testing**: JUnit 5, Spring Boot Test
- **Logging**: SLF4J with Logback

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.6+** (or use the bundled `mvnw` scripts)
- **PostgreSQL or MySQL** database
- **Stripe Account** (for payment processing)
- **Cloudinary Account** (for image management)
- **Docker** (optional, for containerization)

### Backend Setup

1. **Clone or navigate to the repository**

   ```powershell
   cd "F:\my projects\MarketNest"
   ```

2. **Build the project**

   ```powershell
   .\mvnw.cmd clean install -DskipTests
   ```

   Or on Unix-like systems:

   ```bash
   ./mvnw clean install -DskipTests
   ```

3. **Configure application properties**

   Edit `src/main/resources/application.properties` and set:

    - **Database Configuration**:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/marketnest
      spring.datasource.username=postgres
      spring.datasource.password=your_password
      ```

    - **Stripe Configuration**:
      ```properties
      stripe.api.key=your_stripe_secret_key
      stripe.webhook.secret=your_webhook_secret
      ```

    - **Cloudinary Configuration**:
      ```properties
      cloudinary.cloud.name=your_cloud_name
      cloudinary.api.key=your_api_key
      cloudinary.api.secret=your_api_secret
      ```

4. **Environment-Specific Configurations**

    - **Development**: `src/main/resources/application-dev.properties`
    - **UAT**: `src/main/resources/application-uat.properties`
    - **Production**: `src/main/resources/application-prod.properties`

   Set active profile in `application.properties`:
   ```properties
   spring.profiles.active=dev
   ```

## Running the Project

### Option 1: Using Maven

```powershell
# Development profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# UAT profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=uat"

# Production profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

Or on Unix-like systems:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Option 2: Using Built JAR

```powershell
# Build
.\mvnw.cmd clean package

# Run
java -jar target/marketnest-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Expected Output

Once running, the application will start on: `http://localhost:8080`

## API Documentation

### Swagger UI

Access the interactive API documentation:

- **URL**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Available Endpoints

#### Products

- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Create new product (Admin)
- `PUT /api/products/{id}` - Update product (Admin)
- `DELETE /api/products/{id}` - Delete product (Admin)

#### Shopping Cart

- `GET /api/cart` - Get current cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{itemId}` - Update cart item
- `DELETE /api/cart/items/{itemId}` - Remove cart item
- `DELETE /api/cart` - Clear cart

#### Orders

- `GET /api/orders` - Get user's orders
- `GET /api/orders/{orderId}` - Get order details
- `POST /api/orders` - Create new order
- `PUT /api/orders/{orderId}` - Update order status (Admin)

#### Payments

- `POST /api/payments/process` - Process payment via Stripe
- `POST /api/payments/webhook` - Stripe webhook endpoint

#### Reviews

- `GET /api/reviews/product/{productId}` - Get product reviews
- `POST /api/reviews` - Add new review
- `PUT /api/reviews/{reviewId}` - Update review
- `DELETE /api/reviews/{reviewId}` - Delete review

#### Wishlist

- `GET /api/wishlist` - Get user's wishlist
- `POST /api/wishlist/items` - Add item to wishlist
- `DELETE /api/wishlist/items/{productId}` - Remove from wishlist

#### Coupons

- `GET /api/coupons` - List available coupons
- `POST /api/coupons/validate` - Validate coupon code

#### Admin - Coupons

- `POST /api/admin/coupons` - Create coupon
- `PUT /api/admin/coupons/{couponId}` - Update coupon
- `DELETE /api/admin/coupons/{couponId}` - Delete coupon

#### Analytics

- `GET /api/analytics/sales` - Get sales analytics
- `GET /api/analytics/orders` - Get order analytics

#### Authentication

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

#### Users

- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/addresses` - Get user's addresses
- `POST /api/users/addresses` - Add new address

## Building and Deployment

### Create Executable JAR

```powershell
.\mvnw.cmd clean package
```

Output: `target/marketnest-0.0.1-SNAPSHOT.jar`

### Docker Build (if Dockerfile is available)

```powershell
docker build -t marketnest:latest .
docker run -p 8080:8080 --env-file .env marketnest:latest
```

## Testing

### Run All Tests

```powershell
.\mvnw.cmd test
```

### Run Specific Test Class

```powershell
.\mvnw.cmd test -Dtest=UserControllerTest
```

### Skip Tests During Build

```powershell
.\mvnw.cmd clean package -DskipTests
```

## Development Guidelines

### Code Style

- Follow Spring Boot best practices
- Use consistent naming conventions (camelCase for variables, PascalCase for classes)
- Add Javadoc for public APIs and complex methods
- Keep classes focused and follows Single Responsibility Principle
- Use dependency injection throughout

### Custom Annotations

- `@FieldsValueMatch` - Validates that two fields match (e.g., password confirmation)
- `@PasswordValidator` - Validates password strength requirements

## License

This project is licensed under the MIT License - see the LICENSE file for details.

