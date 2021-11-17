Shopper
=========================================
![Build , Test and Publish](https://github.com/q-programming/shopper/workflows/Build/badge.svg)
[![codecov](https://codecov.io/gh/q-programming/shopper/graph/badge.svg)](https://codecov.io/gh/q-programming/shopper)

About
---
Application to create and share shopping lists.
If you quickly need to add something, open webpage or app add it, and it will automatically show on shared user lists

![alt text](https://q-programming.pl/assets/images/projects/shopper_1.png)
 

##  Accounts

Application accounts can be created either with login password, or login using social media : facebook or google
In case of social media created account, all data like name, surname and photo will be automatically used

It's also possible login using companion app for Tizen.
First device is registered by calling `/auth/new-device` with existing application email. 
IF account exist , token will be returned , which is used to authenticate all calls comming from companion app by REST API.
Until confirmed by email, all calls will be rejected 


## Instalation
Application settings are stored in `application.yml` (please see sample `db/application.yml properties` to have full set of required properties)

Customise following entries : 
* Point to your database `spring.datasource.*`
* Facebook `facebook.client.clientId` and app `facebook.client.clientSecret` updated with facebook app values 
(same value goes to `spring.social.facebook.*` 
* Google `google.client.clientId`  and app `google.client.clientSecret` updated
* Default `spring.mail.*` mail server information ( can be then overwrote with databse based parameter via application )
* Change `jwt.secret` secret token with some random value
* Default categories sorting is set via `app.category.order`
* Default language `app.default.lang`

Point to correct properties, using one of methods - order of looking for properties file

1. Edit context value . For Apache Tomcat 8.x  `context.xml` adding following parameter:
   `<Parameter name="shopper.properties.path" value="MY_PROPERTY_PATH/application.yml" override="true"/>`
2. Set system property `-Dshopper.properties.path=MY_PROPERTY_PATH/application.yml`
3. If none above is set, package built in properties from `src/main/resources/application.yml` will be used.

Build package (or grab latest artefact built by CircleCI to use defaults) and deploy to Tomcat container Create database
in your datasource and all tables will be created automatically on first run

First logged in user will be made administrator

Stack: Spring Boot + Angular 12.x

## Android Companion app

Simple Web view wrapper around whole application showing mobile version of whole app, aligned to work best on mobile
Requires extra sign in with email /password for google/facebooks accounts

Please note that browser session might be then shown as Samsung Galaxy S5 on your google activity  :)

apk can be built from `shopper-android` or grabbed from

https://q-programming.pl/apps/shopper-0.7.0.apk

## WearOs Watch Companion app

WearOS 3 Companion app for watches with latest Android wear os system ( Android 11 )
Allows to view all current user lists and items within. Items can be marked as done / not yet done

App is constructed to refresh contents on watch wake up and includes AOD as well

Go into `shopper-watch-companion` , and build project to produce APK which can be side loaded to watch Or grab apk
directly from

https://q-programming.pl/apps/shopper_watch_companion.1.0.0.apk

## Watch Companion app ( depreciated)

For Samsung Galaxy Gear S3 and Galaxy Watch there is dedicated companion app. Sources for it are available
in `shopper-companion`
You can build it and sign it with own certificate, or can try to use one build with my certificate, available here:

https://q-programming.pl/apps/ShopperCompanion.wgt

If you are interested to in integrating any other devices, feel free to contact me via email, so I can help you if
needed

This app is no longer maintained ,and I think it stopped working

Licence
----------
This application was created only be me , if you would like to change something , please notify me . I would love to see
it :) All application is under GNU GPL License and uses some components under Apache License

Please note that application is not unique, there are other application similar in both behaviour and look ( angular
material)
Purpose of this application was only for my usage and self development
