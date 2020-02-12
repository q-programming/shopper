Shopper
=========================================
Latest build: [![CircleCI](https://circleci.com/gh/q-programming/shopper.svg?style=svg)](https://circleci.com/gh/q-programming/shopper)

About
---
Application to create and share shopping lists.
If you quickly need to add something, open webpage or app add it, and it will automatically show on shared user lists 

It's also possible login using companion app for Tizen.
First device is registered by calling `/auth/new-device` with existing application email. 
IF account exist , token will be returned , which is used to authenticate all calls comming from companion app by REST API.
Until confirmed by email, all calls will be rejected 

##  Accounts

Application accounts can be created either with login password, or login using social media : facebook or google
In case of social media created account, all data like name, surname and photo will be automatically used


## Instalation
Application settings are stored in `application.yml` (please see sample `db/application.yml properties` to have full set of required properties)

Customise following entries : 
* Point to your database `spring.datasource.*`
* Facebook `facebook.client.clientId` and app `facebook.client.clientSecret` updated with facebook app values 
(same value goes to `spring.social.facebook.*` 
* Google `google.client.clientId`  and app `google.client.clientSecret` updated
* Default `spring.mail.*` mail server information ( can be then  overwrote with databse based parameter via application ) 
* Change `jwt.secret` secret token with some random value
* Default categories sorting is set via `app.category.order`
* Default language `app.default.lang`

Point to correct properties, using one of methods - order of looking for properties file 
1. Edit context value . For Apache Tomcat 8.x  `context.xml` adding following parameter: 
    `<Parameter name="shopper.properties.path" value="MY_PROPERTY_PATH/application.yml" override="true"/>`
2. Set system property `-Dshopper.properties.path=MY_PROPERTY_PATH/application.yml`
3. If none above is set, package built in properties from `src/main/resources/application.yml` will be used. 
    
Build package (or grab latest artefact built by CircleCI to use defaults) and deploy to Tomcat container
Create database in your datasource and all tables will be created automatically on first run

First logged in user will be made administrator

Stack: Spring Boot + Angular 8.x

Licence
----------
This application was created only be me , if you would like to change something , please notify me . I would love to see it :) All application is under GNU GPL License and uses some components under Apache License

Please note that application is not unique, there are other application similar in both behaviour and look ( angular material)
Purpose of this application was only for my usage and self development
