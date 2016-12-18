# Authentication Demo

This project has two components, the `authentication-backend` and the `authentication-client`.
 
Together, these components demonstrate how to use the OAuth2 "password" grant type between a client and server.  

## Authentication Backend

The backend service is both an OAuth2 Authorization Server and Resource Server. It is also acts as the identity store.

The implementation uses Spring Boot, an H2 in-memory database, and JSON Web Tokens. It keeps an audit log of all successful logins by each user. The audit log of any user can be read by any authenticated user.

H2's [built-in console](http://localhost:8080/h2-console/) is available when the application is running.

## Authentication Client

The client is a CLI that where a users may register, log in, and access resources in the backend securely. The CLI also
allows you to unsuccessfully try to access a secure resource without being logged in.

# Usage

The Java 8 JDK must be available to build and run this project.

To build both `backend` and `client`, issue the following command at the root of the project:

```
$ ./gradlew test bootRepackage
```

Once built, start the `backend` component:

```
$ java -jar authentication-backend/build/libs/authentication-backend-0.0.1-SNAPSHOT.jar
```

In a new Terminal window, start the client with the following command. (NOTE: The client cannot run from inside an IDE. This is due to a limitation of Java's `Console` class.)

```
$ java -jar authentication-client/build/libs/authentication-client-0.0.1-SNAPSHOT.jar
```

Follow the prompts from the CLI to register users, log in, and access secure resources.
 
## Using the H2 console

By logging into H2's [built-in console](http://localhost:8080/h2-console/), 
it is possible to look at the raw data stored by the `backend` component. 

Log in by following the link and using the URL `jdbc:h2:mem:test` and the username and password `sa`.



