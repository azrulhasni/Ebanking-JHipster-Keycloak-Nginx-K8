Micro-services with JHipster, Keycloak and Kubernetes
=====================================================

 

March 2020

Azrul Hasni MADISA

 

 

Introduction<br>
----------------

Learn to create micro services using JHipster (i.e. Spring Boot + Eureka +
Zuul), protect it with authentication from Keycloak, deploy it to Kubernetes and
protect it using SSL through Nginx Ingress

 

Architecture
------------

 

### Overall

Spring Boot is one of the ways to implement micro-services architecture in Java.
JHipster allow us to get up and running with our SpringBoot micro-services
quickly.

 

![](README.images/nRPo3i.jpg)

 

### Zuul Gateway

-   Zuul is part of Netflix OSS stack. It is a high performance API
    gateway/reverse proxy for Spring Boot micro-services. All traffic will come
    through the Zuul gateway. Zuul gateway will be responsible for
    authentication and authorisation. We can also use Zuul for lots of other
    functions but that is for another tutorial.

-   To pass through Zuul, we need to first obtain an access token. The access
    token will be provided by Keycloak once username and password is validated.
    An access token can then be used to call the Banking micro-service (through
    the Zuul gateway).

 

### JHipster Registry

-   JHipter Registry is an implementation of the Registry
    Pattern\<[https://www.martinfowler.com/eaaCatalog/registry.html]\>
    implemented using Eureka, another component of Netflix OSS. It is used to
    look up individual services based on path.

 

### Keycloak

-   Keycloak is an open source identity broker. It is OIDC compliant and allow
    us to manage the authentication and authorisation of users in a standard
    way. It also allows integration to other authentication sources such as
    Active Directory or OpenID compatible social login providers.

 

### Kubernetes

-   Kubernetes is the runtime environment we choose for our micro-services. When
    we deploy our services in Kubernetes, we would have 3 distinct forms: a
    deployment, pods and services.

-   A deployment is comparable to an executable file (an \*.exe file for
    example). Pods can be considered as running programs. There can be many pods
    per deployment and therefore load can be shared among the pods. Another
    element is a service. A service is the entry point to pods and can do things
    like load balance incoming traffic to multiple pods.

![](README.images/X8cvpK.jpg)

### Ingress

-   An ingress is basically an entry point into our services. Just like our Zuul
    gateway, an ingress can be customised to help with authentication and
    routing. We will still stick with our Zuul gateway as the logic we will be
    putting could be complex, but do note that, in some cases, an ingress is
    more than enough.

-   Traffic will flow from the external world to our Ingress Service, to our
    Ingress Controller before hitting our Gateway. Along side the Ingress
    Controller, we have the Ingress file (called Ingress Resource). The Ingress
    Resource is where we specify the routing rules and SSL rule for our
    micro-services.

-   There are many implementations of Ingress, for this tutorial, we will be
    using Nginx.

-   Do note that our SSL (HTTPS) terminates at the Ingress Controller level. For
    us in this tutorial, this is sufficient because the rest of the way, the
    traffic is internal and do not need to be encrypted. If you need SSL to go
    all the way, please refer to
    <https://kubernetes.github.io/ingress-nginx/user-guide/tls/>

 

 

 

Pre-requisite
-------------

-   Java 11
    [<https://www.oracle.com/java/technologies/javase-jdk11-downloads.html>]

-   Maven [<https://maven.apache.org/>]

    -   Installation instructions are here
        [\<http://maven.apache.org/install.html]\>

     

 

Directory Organization
----------------------

There are many ways to organise your directory and you do not have to follow
what is proposed here. But I find this easy enough to navigate.

 

-   Put your project under a particular folder. E.g. /users/\<user
    name\>/myprojects or C:\\Users\\\<user name\>\\my-sandbox. Lets call this
    project folder as \$PROJECTS.

-   Under there, put a specific project folder called ebanking. Under ebanking,
    create 3 more folders: banking, gateway and keycloak.

 

\$PROJECTS

\+--ebanking

\| +--gateway

\| +--banking

\| +--keycloak

\| +--central_config

\| +--k8s

\| +--key

 

Install Docker Desktop
----------------------

-   Follow the tutorial here to get Docker Desktop up and running

  For Mac:

<https://docs.docker.com/docker-for-mac/install/>

  For Windows:

<https://docs.docker.com/docker-for-windows/install/>

 

-   Follow the tutorial here to install Minikube

<https://kubernetes.io/docs/tasks/tools/install-minikube/>

 

-   Next, you need to add Kubernetes support. From Docker menu, choose
    Dashboard.

![](README.images/DOKQgc.jpg)

-   On the dashboard, click on the Gear icon (top right), choose Kubernetes in
    the left-side menu

![](README.images/qhYBmU.jpg)

-   Tick ‘Enable Kubernetes’ and ‘Deploy Docker Stacks to Kubernetes by
    Default’. Click on the ‘Apply’ button and wait for the status to be shown as
    ‘Running'

![](README.images/yNhtK7.jpg)

-   Congratulations! You have a running Kubernetes in your machine now.

 

Ingress setup and security
--------------------------

Recall that the communication to our Ingress is done through HTTPS. Therefore we
need to create our self-signed certificate for this. You can also use a full
fledge CA certificate.

 

### Setup a self signed certificate

-   Go to the \$PROJECTS/ebanking/key. In that folder, run the command below.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
>openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ./mybanking.svc.my.key -out ./mybanking.svc.my.crt
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   There will be a list of questions asked by the system. Answer them and 2
    files (mybanking.svc.my.key and mybanking.svc.my.crt) will be created for
    you. Please make sure that Common Name is put as `localhost` if you are
    deploying this locally.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Generating a 2048 bit RSA private key
....+++
..........................................................+++
writing new private key to './mybanking.svc.my.key'
-----
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) []:my
State or Province Name (full name) []:WP
Locality Name (eg, city) []:KL
Organization Name (eg, company) []:MyOrg
Organizational Unit Name (eg, section) []:MyDep
Common Name (eg, fully qualified host name) []:localhost
Email Address []:azrul@myorg.com
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### Importing certificate into Kubernetes secret

-   Next, we need to import these certificates as kubernetes secret. Run the
    command below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl create secret generic my-gw-certs --from-file=./mybanking.svc.my.key --from-file=./mybanking.svc.my.crt
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   This will create a secret called my-gw-certs in Kubernetes. To check, you
    can run

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl get secrets
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   The result should show:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
NAME                             TYPE                                  DATA   AGE
banking-postgresql               Opaque                                1      12h
default-token-9prpb              kubernetes.io/service-account-token   3      22h
gateway-postgresql               Opaque                                1      12h
keycloak-http                    Opaque                                1      12h
keycloak-postgresql              Opaque                                1      12h
my-gw-certs                      Opaque                                2      3h21m
registry-secret                  Opaque                                1      12h
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### Setup Nginx ingress controller

-   Firstly, we need to install some mandatory settings and components. Run the
    command

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/mandatory.yaml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Then, we need to install the actual Nginx ingress controller itself. Run the
    command

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/cloud-generic.yaml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Congratulation, your ingress controller is now ready to be used.

 

 

Install and configure Keycloak
------------------------------

### How Keycloak works

-   Before we start, we should learn a bit on how Keycloak authentication works.
    We are using OAuth2 standard to control our access. Under OAuth2, there are
    several types of grants. Here, we will be using ‘Direct Access Grant’ - this
    will allow us to pass username and password to Keycloak to obtain access
    token and refresh token. To learn more about OAuth please visit this link
    [<https://developer.okta.com/blog/2017/06/21/what-the-heck-is-oauth>]

-   The flow of authentication and micro-service call is illustrated below. In
    the flow below we wanted to query the Banking micro-services for Customers
    data:

 

![](README.images/8nt79E.jpg)

 

-   Notice that there are two APIs for Keycloak, one to get an access token,
    another to validate issuer URL.

 

### A few words about Keycloak URL

-   We need to dive a bit on how Keycloak is accessed. When we install Keycloak
    later, it will have an internal URL
    (http://keycloak-http.default.svc.cluster.local:8090). This internal URL is
    automatically assigned and will be used by our micro-services to access
    Keycloak.

-   As noted before, there are 2 APIs - getting access token and validating
    issuer URL. If you notice, validating issuer URL is called internally by the
    micro-services and getting access token is called by a client.

-   Unfortunately, the internal call URL
    (http://keycloak-http.default.svc.cluster.local:8090) cannot be the same as
    the external call URL. This is because, externally, I would call Keycloak
    using a more public URL (e.g. http://keycloak.mycompany.com). Unless I own a
    domain called keycloak-http.default.svc.cluster.local, then I would need 2
    domains for my Keycloak

-   This is actually a problem because Keycloak embed the URL used to get the
    access token into the token itself (issuer-url field), if this URL is not
    the same as the issuer URL, Keycloak will fail.

-   **TL;DR:** Both get access token and validate issuer URL must use the same
    URL

-   In our ingress setup later, we will try to solve this problem

 

![](README.images/VX8qkE.jpg)

 

 

### Installing Helm

-   We will now start installing Keycloak by first installing Helm. Helm is a
    package manager for Kubernetes, the way Maven is for Java and NPM is for
    Javascript. Follow the instructions here
    [<https://helm.sh/docs/intro/install/>] to install Helm. On a Mac, we can
    use brew:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 > brew install helm
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### Customise Keycloak Helm configuration

-   Before we install Keycloak, we need to customise its installation
    configuration. This is to allow persistence. Keycloak instance, by default,
    is using in-memory H2 database. This means that, all Keycloak configuration
    and data will disappear when we restart it. This is not desirable. Because
    of this, we will customise Keycloak to use Postgresql instead.

 

-   Firstly visit the Keycloak Helm chart page.

    <https://github.com/codecentric/helm-charts/tree/master/charts/keycloak>

 

-   Download values.yaml into \$PROJECTS/ebanking/keycloak and rename it
    config.yaml

![](README.images/Zosp1z.jpg)

-   Open up config.yaml,

    -   For persistence, find deployPostgres property and set it to true. This
        will tell Helm to also install Postgresql alongside Keycloak.

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     persistence:
        # If true, the Postgres chart is deployed
        deployPostgres: true
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    -   We also would like to customise the port (this is purely optional). We
        change the httpPort from 80 to 8090

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    httpPort: 8090
        httpNodePort: ""
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

     

### Install Keycloak using Helm

-   Next, open up a command line terminal and go to the folder
    \$PROJECTS/ebanking/keycloak. We are going to use Helm to install Keycloak.
    First, add code centric repository

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> helm repo add codecentric https://codecentric.github.io/helm-charts
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Next, install Keycloak. Run the command line below and make sure config.yaml
    created above is accessible through the option -f.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> helm install keycloak -f config.yaml codecentric/keycloak
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Once set up is done, you will get the following message

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
NAME: keycloak
LAST DEPLOYED: Mon Mar 23 22:56:03 2020
NAMESPACE: default
STATUS: deployed
REVISION: 1
NOTES:
Keycloak can be accessed:
* Within your cluster, at the following DNS name at port 8090:
  keycloak-http.default.svc.cluster.local
* From outside the cluster, run these commands in the same shell:
  export POD_NAME=$(kubectl get pods --namespace default -l app.kubernetes.io/instance=keycloak -o jsonpath="{.items[0].metadata.name}")
  echo "Visit http://127.0.0.1:8080 to use Keycloak"
  kubectl port-forward --namespace default $POD_NAME 8080
Login with the following credentials:
Username: keycloak
To retrieve the initial user password run:
kubectl get secret --namespace default keycloak-http -o jsonpath="{.data.password}" | base64 --decode; echo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Please notice the internal URL for Keycloak
    `http://keycloak-http.default.svc.cluster.local:8090`

###  

### Exposing Keycloak through Ingress

-   Next, go to our \$PROJECTS/ebanking/k8s folder. In there, create a file
    called **my-ingress.yaml** with the content below. This will be our Ingress
    Resource that we mentioned earlier. Please note that

    -   The `hosts` must contain the common name of the certificate set in the
        paragraph - Ingress Setup and Security.

    -   The `secretName` property must contain the name of our secret created in
        the paragraph - Ingress Setup and Security.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
   kubernetes.io/ingress.class: nginx
   nginx.ingress.kubernetes.io/server-snippet: |
      location /keycloak-auth/ {
        proxy_pass http://keycloak-http.default.svc.cluster.local:8090/auth/;
      }
spec:
  tls:
    - hosts:
      - localhost
      secretName: my-gw-certs
  rules:
  - http:
      paths:
        - path: /auth
          backend:
            serviceName: keycloak-http
            servicePort: 8090
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Some explanation is warranted here:

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    nginx.ingress.kubernetes.io/server-snippet: |
          location /keycloak-auth/ {
            proxy_pass http://keycloak-http.default.svc.cluster.local:8090/auth/;
          }
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    -   This part of the configuration says, if the incoming URL is
        `https://some.random.url/keycloak-auth/realms/…`, this URL will be
        mapped to
        `http://keycloak-http.default.svc.cluster.local:8090/auth/realms/…` .
        This mapping is what solves our problem stated before in the paragraph
        "A few words about Keycloak URL”.

![](README.images/uaWMba.jpg)

-   Because of this, our public URL to get access token would be
    **https://localhost/keycloak-auth/realms/ebanking/protocol/openid-connect/token**

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    path: /auth
              backend:
                serviceName: keycloak-http
                servicePort: 8090
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    -   This is another mapping for Keycloak. We have this mapping to allow
        Keycloak portal to be accessed.

 

-   Continuing, save the file, and in the same folder run :

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl apply -f my-ingress.yaml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Once the command is successful, you are now done with SSL ingress setup for
    Keycloak.

 

### Access Keycloak and setup a realm

-   Next, you will need the current Keycloak administration password. Run the
    command to retrieve the initial password.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl get secret --namespace default keycloak-http -o jsonpath="{.data.password}" | base64 --decode; echo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Visit the site <https://localhost/auth>. You will get a page below. Note
    that the Keycloak Admin. Portal is on `https://localhost/auth`and the token
    is on
    `https://localhost/keycloak-auth/realms/ebanking/protocol/openid-connect/token.`

![](README.images/Wo6qBK.jpg)

-   Click on ‘Administration Console’ and use the password you retrieve.

-   Under ‘Master’ realm, click on the dropdown button and choose ‘Add Realm’.

![](README.images/dDrOHn.jpg)

-   Enter the name new realm name ‘ebanking’ and create

-   Click on ‘Clients’ and click on the button ‘Create’ to create a client

![](README.images/vLbqxJ.jpg)

-   In the client id, put ‘ebankingclient’ and click ’Save'

-   You will land on the client configuration page. Make sure that your

    -   Access Type is set on confidential

    -   Direct Access Grant set to ON

    -   Root URL and Valid Redirect URIs set to http://localhost:18080

        Note that in the Valid Redirect URIs field, you need to fill it and
        click on the + button.

    -   Web Origins is set to ‘+’. Web Origins designate allowed CORS origin.
        The ‘+’ sign means that anything in Valid Redirect, should also be valid
        web origins.

        Note that in the Valid Redirect URIs field, you need to fill it and
        click on the + button.

    -   Note that http://localhost:18080 will be our Zuul URL.

    Once done, click on the button Save.

![](README.images/KUeWX0.jpg)

-   Once saved, on the same page, go back to the top and choose the Confidential
    tab. Note that the Confidential tab will only appear if you have chosen your
    Access Type as confidential and you have click on Save.

![](README.images/1ccQI9.jpg)

-   Note the client secret (596f1a…) and copy it somewhere.

 

### Create roles and users in Keycloak

-   Next, choose Roles and create two roles: ROLE_ADMIN and ROLE_USER

![](README.images/2DbWT7.jpg)

-   Under Users, create 2 users:

    -   donald.duck with the role ROLE_USER

    -   admin with the role ROLE_ADMIN and ROLE_USER

-   To create Users, click on Users on the side menu and the list of Users will
    appear. Click on the ‘Add User’ button. The page below will appear. Fill in
    the information and click on Save.

![](README.images/90tgcj.jpg)

-   Once saved, a tab called ‘Credential’ will appear. This is where we put in
    the user password

![](README.images/UYsfZJ.jpg)

-   Enter the password and click on Set Password

![](README.images/EEFvkZ.jpg)

-   A dialog will appear. Confirm the Set Password and your user is created.

 

![](README.images/jNhnNl.jpg)

 

-   IMPORTANT: Please make sure that the donald.duck user DO NOT have ‘Required
    User Action’ set

![](README.images/Iub4cB.jpg)

-   We are now done with Keycloak

 

### Keycloak URLs

-   Before we move on, lets recap all the URLs involved:

| **URL**                                                                                           | **What is it for**                                         | **Who calls it** |
|---------------------------------------------------------------------------------------------------|------------------------------------------------------------|------------------|
| http://keycloak-http.default.svc.cluster.local:8090/auth/realms/ebanking                          | Keycloak URL internal to Kubernetes                        | Micro-services   |
| https://localhost/keycloak-auth/realms/ebanking OR http://localhost/keycloak-auth/realms/ebanking | Client facing (public) URL of Keycloak to get access token | Client           |
| http://localhost/auth OR https://localhost/auth                                                   | Client facing (internal users) Keycloak admin portal       | Client / Admin   |

 

Use JHipster to create our micro-services
-----------------------------------------

-   We will use JHipster to create our micro-services. To start we will need to
    install JHipster. Please follow the tutorial here to install JHipster
    [\<https://www.jhipster.tech/installation/]\>

 

### Set up JHipster Registry

-   If you notice in the architecture, we have a component called ‘JHipster
    Registry’. We will need to download this registry and run it. Visit the
    GitHub page [<https://github.com/jhipster/jhipster-registry/releases>] and
    download the latest release (as of this article, the latest release is
    6.1.2) and place it under the \$PROJECTS/ebanking folder.

-   Under the \$PROJECTS/ebanking/central_config folder, create a file called
    application.yaml and put in there the default configuration.

-   Please note: for production, change the `secret` property (i.e. plain text
    secret) to `base64-secret` and put a base64 encoded secret in that property.
    To obtain a base64 string, you can visit the site
    <https://www.base64encode.org/>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# ===================================================================
# JHipster Sample Spring Cloud Config.
# ===================================================================

# Property used on app startup to check the config server status
configserver:
    name: JHipster Registry config server
    status: Connected to the JHipster Registry config server!

# Default JWT secret token (to be changed in production!)
jhipster:
    security:
        authentication:
            jwt:
                secret: my-secret-key-which-should-be-changed-in-production-and-be-base64-encoded
   
# Enable /management/logfile endpoint for all apps
logging:
    path: /tmp
    file: ${spring.application.name}.log
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   To run the registry, open up a command line in the \$PROJECTS/ebanking and
    run:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> java -jar jhipster-registry-6.1.2_2.jar --spring.security.user.password=admin --jhipster.security.authentication.jwt.secret=my-secret-key-which-should-be-changed-in-production-and-be-base64-encoded --spring.cloud.config.server.composite.0.type=native --spring.cloud.config.server.composite.0.search-locations=file:./central-config
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Please note: IN PRODUCTION, please change

    -   The password (spring.security.user.password — currently set to ‘admin’)
        and

    -   The secret (jhipster.security.authentication.jwt.secret — currently set
        to
        'my-secret-key-which-should-be-changed-in-production-and-be-base64-encoded’.
        Please note also if you encode your string as base64, you would need to
        change the property to
        jhipster.security.authentication.jwt.base64-secret

-   Note: The properties in the command line above are similar to the ones in
    the \$PROJECTS/ebanking/central_config/application.yaml file. The command
    line properties will override the ones in the application.yaml. For more on
    properties priority, please see:

    <https://docs.spring.io/spring-boot/docs/1.0.1.RELEASE/reference/html/boot-features-external-config.html>

-   Once successfully run, you will see the screen below in your console

![](README.images/pQwsLk.jpg)

-   We have successfully run JHipster Registry

 

### Set up Zuul Gateway using JHipster

-   Go to the \$PROJECTS/ebanking/gateway folder and fire up a command line tool
    and run

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> jhipster
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   JHipster interface will allow you to pick and choose your setup. To create a
    gateway choose Microservice Gateway as below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
? Which *type* of application would you like to create? Microservice gateway
? [Alpha] Do you want to make it reactive with Spring WebFlux? No
? What is the base name of your application? gateway
? As you are running in a microservice architecture, on which port would like your server to run? It should be unique to a
void port conflicts. 18080
? What is your default Java package name? com.azrul.ebanking.gateway
? Which service discovery server do you want to use? JHipster Registry (uses Eureka, provides Spring Cloud Config support 
and monitoring dashboards)
? Which *type* of authentication would you like to use? OAuth 2.0 / OIDC Authentication (stateful, works with Keycloak and
 Okta)
? Which *type* of database would you like to use? SQL (H2, MySQL, MariaDB, PostgreSQL, Oracle, MSSQL)
? Which *production* database would you like to use? PostgreSQL
? Which *development* database would you like to use? H2 with in-memory persistence
? Do you want to use the Spring cache abstraction? No - Warning, when using an SQL database, this will disable the Hiberna
te 2nd level cache!
? Do you want to use Hibernate 2nd level cache? No
? Would you like to use Maven or Gradle for building the backend? Maven
? Which other technologies would you like to use? (Press <space> to select, <a> to toggle all, <i> to invert selection)
? Which *Framework* would you like to use for the client? Angular
? Would you like to use a Bootswatch theme (https://bootswatch.com/)? Default JHipster
? Would you like to enable internationalization support? No
? Besides JUnit and Jest, which testing frameworks would you like to use? (Press <space> to select, <a> to toggle all, <i>
 to invert selection)
? Would you like to install other generators from the JHipster Marketplace? (y/N) 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Open up the file located at
    \$PROJECTS/ebanking/gateway/src/main/resources/config/application.yaml

-   Find the properties

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
security:
    oauth2:
      client:
        provider:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Customise it as per below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  security:
    oauth2:
      client:
        provider:
          oidc:
            issuer-uri: http://localhost/keycloak-auth/realms/ebanking/auth/realms/ebanking
        registration:
          oidc:
            client-id: ebankingclient
            client-secret: <client secret>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Copy the client secret we note down in the paragraph 'Install and configure
    Keycloak on Kubernetes’ above into the properties `client-secret`

-   Notice that we are using the client facing public Keycloak URL as the issuer
    URI as this is run outside of Kubernetes for now.

-   Save the file application.yaml

 

 

### Running Zuul Gateway

-   Fire up a command line console in the \$PROJECTS/ebanking/gateway folder and
    run:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> ./mvnw
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Upon completion, the console should show the status as per below:

![](README.images/ln7aE8.jpg)

-   We have successfully run our gateway

 

### Create a micro-service using JHipster

-   Firstly, go to the \$PROJECTS/ebanking/banking folder and fire up a command
    line tool there

-   Run the command

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> jhipster
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Choose the options as per shown below

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
? Which *type* of application would you like to create? Microservice application
? [Alpha] Do you want to make it reactive with Spring WebFlux? No
? What is the base name of your application? banking
? As you are running in a microservice architecture, on which port would like your server to run? It should be unique to avoid port conflicts. 18081
? What is your default Java package name? com.azrul.ebanking.banking
? Which service discovery server do you want to use? JHipster Registry (uses Eureka, provides Spring Cloud Config support and monitoring dashboards)
? Which *type* of authentication would you like to use? OAuth 2.0 / OIDC Authentication (stateful, works with Keycloak and Okta)
? Which *type* of database would you like to use? SQL (H2, MySQL, MariaDB, PostgreSQL, Oracle, MSSQL)
? Which *production* database would you like to use? PostgreSQL
? Which *development* database would you like to use? H2 with in-memory persistence
? Do you want to use the Spring cache abstraction? No - Warning, when using an SQL database, this will disable the Hibernate 2nd level cache!
? Would you like to use Maven or Gradle for building the backend? Maven
? Which other technologies would you like to use? (Press <space> to select, <a> to toggle all, <i> to invert selection)
? Would you like to enable internationalization support? No
? Besides JUnit and Jest, which testing frameworks would you like to use? (Press <space> to select, <a> to toggle all, <i> to invert selection)
? Would you like to install other generators from the JHipster Marketplace? (y/N) 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Please note that we are using persistent H2 database for development

-   Open up the file located at
    \$PROJECTS/ebanking/banking/src/main/resources/config/application.yaml

-   Find the properties

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
security:
    oauth2:
      client:
        provider:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Customise it as per below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  security:
    oauth2:
      client:
        provider:
          oidc:
            issuer-uri: http://localhost/keycloak-auth/realms/ebanking/auth/realms/ebanking
        registration:
          oidc:
            client-id: ebankingclient
            client-secret: <client secret>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Copy the client secret we note down in the paragraph 'Install and configure
    Keycloak on Kubernetes’ above into the properties `client-secret`

-   Notice that we are using the client facing public Keycloak URL as the issuer
    URI as this is run outside of Kubernetes for now

-   Once done, save the file application.yaml

 

### Setup a data model for the micro-service

-   Next, we need to apply a data model to the micro service. JHipster has a
    data modelling tool. Point your browser to
    <https://start.jhipster.tech/jdl-studio/>

-   Create a model as per below. Once done, click on the download button. Rename
    the file to ‘banking.jh’ and save the file in \$PROJECTS/ebanking/banking
    folder. Alternatively, you can just create a file called ‘banking.jh’ and
    copy paste the content as per below

![](README.images/zKJ6zl.jpg)

-   The structure:

![](README.images/oStIP4.jpg)

-   The ‘banking.jh’ content

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
enum Currency{
    USD, MYR, SGD
}

entity Customer{
    cifNumber String,
    firstName String,
    lastName String,
    icNumber String,
    phoneNumber String
}

entity Transaction{
    transactionId String,
    transactionType String,
    amount BigDecimal,
    time ZonedDateTime,
    currency Currency,
}

entity ProductAccount{
    accountNumber String,
    productId String,
    openingDate ZonedDateTime,
    status Integer,
    balance BigDecimal
}

relationship OneToMany {
    Customer{accounts} to ProductAccount
}

relationship ManyToOne {
    Transaction{source} to ProductAccount
}

relationship ManyToOne {
    Transaction{target} to ProductAccount
}

// Set pagination options
paginate ProductAccount, Transaction with infinite-scroll
paginate Customer with pagination

dto * with mapstruct

// Set service options to all except few
service all with serviceImpl

// Set an angular suffix
angularSuffix * with banking
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Apply the banking.jh data model to the micro-service. Fire up a command line
    console in the \$PROJECTS/ebanking/banking folder and run the command:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> jhipster import-jdl banking.jh
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### Running the micro-service

-   Once that is done, run the command below

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> ./mvnw
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Upon completion, the console should show the status as per below:

![](README.images/8iBlBo.jpg)

-   Congratulations, we have set up our micro-service

 

### Test our micro-service from JHipster gateway portal

-   Point your browser to the gateway: http://localhost:18080

-   You will see the screen below. Go to Account and click Sign In

![](README.images/YPXNDw.jpg)

-   You will be directed to Keycloak login page. Enter ‘admin’ as username and
    use the password set for the user ‘admin’ as in the paragraph 'Install and
    configure Keycloak on Kubernetes’.

![](README.images/2xw22z.jpg)

-   Once you log in, you will land back to JHipster page. You should have a new
    ‘Administration’ menu. Choose API. Note that if you do not have the
    ‘Administration’ menu, you have not associated ‘ROLE_ADMIN’ to your ‘admin’
    user.

![](README.images/MKID73.jpg)

-   Next, you will see a list of available APIs as per the screen below

![](README.images/3cOKCs.jpg)

-   Click on GET /api/customers getAllCustomers API. A more detailed page on the
    API will appear. Click on the button ’Try Out’ on the top right. And then,
    click on ‘Execute’ button:

![](README.images/6qoQ7M.jpg)

-   We should be getting a response code 200 back with some dummy data generated
    automatically by JHipster.

![](README.images/zgqxJW.jpg)

 

### Test our micro-service using curl

-   While it is useful to test your API directly from portal, it is not very
    useful. In order for the APIs to be useful, it needs to be callable from a
    client (mobile app, angular app, reacts app etc.)

-   To help us with this, we will use curl for this.

-   Recall the authentication flow:

![](README.images/ZaZwDr.jpg)

-   In this case, we will concentrate on the ‘Usage’ part of the flow. So
    firstly, we will need to create an API call to get the access token. Use the
    curl command below. Please make sure that

    -   You change `<password>` to what ever password that was set up for the
        user donald.duck in the paragraph 'Install and configure Keycloak on
        Kubernetes’.

    -   You change `<client secret>` to what we copied from Keycloak before

-   Note that since client is external, we use the external API of key cloak
    which is

`https://localhost/keycloak-auth/realms/ebanking/protocol/openid-connect/token`

-   Please recall that

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> curl -kX POST -vu client:ebankingclient https://localhost/keycloak-auth/realms/ebanking/protocol/openid-connect/token -H "Accept: application/json" -d "username=donald.duck&password=<password>&grant_type=password&scope=read&client_id=ebankingclient&client_secret=<client secret>"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A very lengthy reply will appear. Something similar to this:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
{
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDT2s1SUpCOVdmNEZEY1B5eXE2aWwxS3EyU2ZuMTZoNWhTbnM3WFY0MTlRIn0.eyJqdGkiOiJlNjVkOGYwMC1iOTZlLTRlMDQtOTNkNy1iY2EyMjFmYWE5YmEiLCJleHAiOjE1ODYxODg2NjIsIm5iZiI6MCwiaWF0IjoxNTg2MTg4MzYyLCJpc3MiOiJodHRwOi8va2V5Y2xvYWstaHR0cC5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsOjgwOTAvYXV0aC9yZWFsbXMvZWJhbmtpbmciLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMDgzNTRlYzItNDIyYS00NzIyLTkxOWQtZTcyZmRkYzdhODYyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZWJhbmtpbmdjbGllbnQiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJjOGQ4MzcxMy0zNTA5LTQwNzQtOWQzNC0xZTMxMWIzNTM3M2IiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6MTgwODAiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIlJPTEVfVVNFUiIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJEb25hbGQgRHVjayIsInByZWZlcnJlZF91c2VybmFtZSI6ImRvbmFsZC5kdWNrIiwiZ2l2ZW5fbmFtZSI6IkRvbmFsZCIsImZhbWlseV9uYW1lIjoiRHVjayIsImVtYWlsIjoiZG9uYWxkLmR1Y2tAY29tcGFueS5jb20ifQ.EVKdQV_RTZrz3XX2zBBsKynAX05IxmEHYXw5r58a1VSCJcC1Y-f4kSscgT0BoWFIh_bInKqhYsnFpkdlKs_prJxcVOwX4a3-obQ8QXreBu_hnHEyE1KsoGLz4mUPXGM5SrDpry-x2FjJUDto5veNXCnsnUlQEgBRhXGe5VjZpE5VscxyBjcp3nYg2kADXCplW3bEMKw2i-Txlz_Z_zjFPfR49Z-Zq38wcRZZjHkDL8bqpt5yBo7NmdMBpVTPfd-QOIaqIT9VRGYy5mxZxYgXC1Mqqc8VntFg_cLYIdIz8IB3u7d7jkAN9EtD6Vf-lCI1pLX03Hpi-I9XTFRW4YpstA",
    "expires_in": 300,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmYTBhNzhkZC1mMDA0LTQxZWQtOWU1Zi0xY2YwYThjNzA2MWQifQ.eyJqdGkiOiI2OGIwNzFiNC1jOGUwLTRlZjgtYmU2ZC0xY2E4ZTZmZjJiMTUiLCJleHAiOjE1ODYxOTAxNjIsIm5iZiI6MCwiaWF0IjoxNTg2MTg4MzYyLCJpc3MiOiJodHRwOi8va2V5Y2xvYWstaHR0cC5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsOjgwOTAvYXV0aC9yZWFsbXMvZWJhbmtpbmciLCJhdWQiOiJodHRwOi8va2V5Y2xvYWstaHR0cC5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsOjgwOTAvYXV0aC9yZWFsbXMvZWJhbmtpbmciLCJzdWIiOiIwODM1NGVjMi00MjJhLTQ3MjItOTE5ZC1lNzJmZGRjN2E4NjIiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoiZWJhbmtpbmdjbGllbnQiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJjOGQ4MzcxMy0zNTA5LTQwNzQtOWQzNC0xZTMxMWIzNTM3M2IiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiUk9MRV9VU0VSIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCJ9.CLM3qV0be3e9iMcgq7iKg09njjucJOQW398eFpC7Aio",
    "token_type": "bearer",
    "not-before-policy": 0,
    "session_state": "c8d83713-3509-4074-9d34-1e311b35373b",
    "scope": "profile email"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Next, copy the access_token - without quotes (`eyJhb…FRW4YpstA`) and paste
    it in the `<access token>`field below and run the command that calls the
    gateway. The gateway will then call the corresponding service.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> curl http://localhost:18080/services/banking/api/customers -H "Authorization: Bearer <access token>"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   If all goes well, you should see the dummy data just like we had before

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[ {
  "id" : 1,
  "cifNumber" : "Assistant",
  "firstName" : "Kiana",
  "lastName" : "Bosco",
  "icNumber" : "heuristic wireless",
  "phoneNumber" : "orchid"
}, {
  "id" : 2,
  "cifNumber" : "Bedfordshire Security",
  "firstName" : "Breanna",
  "lastName" : "Flatley",
  "icNumber" : "firmware index mission-critical",
  "phoneNumber" : "Music functionalities Implementation"
}, {
  "id" : 3,
  "cifNumber" : "Consultant Malta",
  "firstName" : "Gladyce",
  "lastName" : "Rippin",
  "icNumber" : "Licensed Rubber Bacon relationships",
  "phoneNumber" : "dedicated" 

....
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   With that, we have successfully created a micro-service working under a
    gateway and authenticated using Keycloak.

 

 

Deployment to Kubernetes
------------------------

 

### Deployment concept

-   We have already seen how our Keycloak was deployed to Kubernetes. Now, we
    are going to deploy our micro-services there also.

-   The deployment we do here will try to emulate production deployment as much
    as we can.

-   Firstly, let us visit the concept behind Kubernetes deployment. We have
    already deployed Keycloak using Helm. Fortunately, JHipster comes with a
    sleuth of its own tools for Kubernetes deployment. In a real CI/CD set up,
    source code would be pushed to a source code repository. From there we would
    build it and submit it to Docker Hub (https://hub.docker.com). From Docker
    Hub, we would then deploy the binaries into a Kubernetes cluster.

-   In our tutorial set up, we do not have all the CI/CD components.
    Nonetheless, we will still push, from our development machine, to Docker
    Hub, before deploying to our Kubernetes cluster.

![](README.images/OE1ASU.jpg)

 

 

### Deployment setup

-   To avoid port conflict, please shut down both banking and gateway
    micro-services

-   Firstly, we need to change the issuer-url to point to Keycloak from inside
    Kubernetes. For both
    \$PROJECTS/ebanking/**gateway**/src/main/resources/config/application.yaml
    **and**
    \$PROJECTS/ebanking/**banking**/src/main/resources/config/application.yaml

-   Find the properties

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
security:
    oauth2:
      client:
        provider:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Customise it as per below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  security:
    oauth2:
      client:
        provider:
          oidc:
            issuer-uri: http://keycloak-http.default.svc.cluster.local:8090/auth/realms/ebanking
        registration:
          oidc:
            client-id: ebankingclient
            client-secret: <client secret>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Notice that issuer URI is now pointing to the internal (to Kubernetes)
    Keycloak URL. Save the files.

-   To start deployment setup, we need a Docker Hub id. Go to
    <https://hub.docker.com>. Register for an account.

-   Then, fire up a command line console in the \$PROJECTS/ebanking/k8s folder.
    Run the command

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> jhipster kubernetes
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   The choices presented are:

    -   Which \* type \*- choose Microservice application

    -   Enter the root directory - put your \$PROJECTS/ebanking directory

    -   When asked which application do you want to include - choose both
        banking and gateway

    -   Make sure you enter the registry admin password

    -   For Kubernetes namespace - choose default

    -   For base Docker repository - use your Docker Hub username

    -   To push docker images - choose docker push

    -   For istio - set to No

    -   For Kubernetes service type for edge service - choose NodePort

-   Once successful you will see the screen below

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
WARNING! Kubernetes configuration generated, but no Jib cache found
If you forgot to generate the Docker image for this application, please run:
To generate the missing Docker image(s), please run:
  ./mvnw -ntp -Pprod verify jib:dockerBuild in $PROJECTS/ebanking/banking
  ./mvnw -ntp -Pprod verify jib:dockerBuild in $PROJECTS/ebanking/gateway


WARNING! You will need to push your image to a registry. If you have not done so, use the following commands to tag and push the images:
  docker image tag banking <your Docker Hub id>/banking
  docker push <your Docker Hub id>/banking
  docker image tag gateway <your Docker Hub id>/gateway
  docker push <your Docker Hub id>/gateway

INFO! Alternatively, you can use Jib to build and push image directly to a remote registry:
  ./mvnw -ntp -Pprod verify jib:build -Djib.to.image=<your Docker Hub id>/banking in $PROJECTS/ebanking/banking
  ./mvnw -ntp -Pprod verify jib:build -Djib.to.image=<your Docker Hub id>/gateway in $PROJECTS/ebanking/gateway

You can deploy all your apps by running the following kubectl command:
  bash kubectl-apply.sh -f

[OR]

If you want to use kustomize configuration, then run the following command:
  bash kubectl-apply.sh -k

Use these commands to find your application's IP addresses:
  kubectl get svc gateway -n ebanking
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### Push image to Docker Hub

-   Go to your \$PROJECTS/ebanking/banking folder and fire up a command line
    console. Run the command below. This will compile your code (if need be) and
    deploy it to Docker Hub.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
>./mvnw -ntp -Pprod verify jib:build -Djib.to.image=<your Docker Hub id>/banking
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Next, go to your \$PROJECTS/ebanking/gateway folder and fire up a command
    line console. Run the command below. This will compile your code (if need
    be) and deploy it to Docker Hub.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
>./mvnw -ntp -Pprod verify jib:build -Djib.to.image=<your Docker Hub id>/gateway
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Lets verify that both docker images have been pushed to Docker Hub . Go to
    Docker Hub (go to hub.docker.com or use Docker Desktop menu) and log in.

![](README.images/FB2XGm.jpg)

-   You should be able to see your Docker images.

![](README.images/0hAOk1.jpg)

 

### Download image from Docker Hub to Kubernetes

-   Now it is time to pull back those Docker images into our Kubernetes
    container. In the \$PROJECTS/ebanking/k8s folder, run the command below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> bash kubectl-apply.sh -f
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   To see if your images are pulled and deployed properly, run the command
    line:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl get pods
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   You will get the output below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
NAME                                  READY   STATUS    RESTARTS   AGE
banking-bbdf47c5b-stfg9               1/1     Running   0          133m
banking-postgresql-646f5f8d5c-xnwgm   1/1     Running   0          133m
gateway-77697476b7-wbm2q              1/1     Running   0          133m
gateway-postgresql-8687dd66b5-dcgdf   1/1     Running   0          133m
jhipster-registry-0                   1/1     Running   0          133m
jhipster-registry-1                   1/1     Running   0          133m
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   You can see all our components are there. Keycloak, JHipster registry,
    banking and gateway. Make sure the status is ‘Running'

-   We have now completed our deployment to Kubernetes

 

### Setup Ingress for micro-services

-   Lastly, we need to setup Ingress for our micro-services. Firstly, recall
    that we already setup an Ingress for our Keycloak. We will edit that Ingress
    and add a path for our Gateway

-   Go to our \$PROJECTS/ebanking/k8s folder. In there, open up our file
    **my-ingress.yaml**. Put in the content below

    -   The `hosts` must contain the common name of the certificate set in the
        paragraph - Ingress Setup and Security.

    -   The `secretName` property must contain the name of our secret created in
        the paragraph - Ingress Setup and Security.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
   kubernetes.io/ingress.class: nginx
   nginx.ingress.kubernetes.io/server-snippet: |
      location /keycloak-auth/ {
        proxy_pass http://keycloak-http.default.svc.cluster.local:8090/auth/;
      }
spec:
  tls:
    - hosts:
      - localhost
      secretName: my-gw-certs
  rules:
  - http:
      paths:
        - path: /
          backend:
            serviceName: gateway
            servicePort: 18080
        - path: /auth
          backend:
            serviceName: keycloak-http
            servicePort: 8090
        
        
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Notice that we have added the gateway service:

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    path: /
              backend:
                serviceName: gateway
                servicePort: 18080
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Save the file

-   Delete the old ingress

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl delete ingress my-ingress
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   In the same folder run :

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl apply -f my-ingress.yaml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Once the command is successful, you are now done with SSL ingress setup for
    both Keycloak and micro-services.

 

Testing micro-services running in Kubernetes
--------------------------------------------

-   Firstly, lets get our access key. Run the command:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> curl -kX POST -vu client:ebankingclient https://localhost/keycloak-auth/realms/ebanking/protocol/openid-connect/token -H "Accept: application/json" -d "username=donald.duck&password=<password>&grant_type=password&scope=read&client_id=ebankingclient&client_secret=<client secret>"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Make sure

    -   You change `<password>` to what ever password that was set up for the
        user donald.duck in the paragraph 'Install and configure Keycloak on
        Kubernetes - Create roles and users in Keycloak’.

    -   You change `<client secret>` to what we copied from Keycloak before

-   Again, you will get a long response:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to keycloak-http.default.svc.cluster.local (127.0.0.1) port 8443 (#0)
* ALPN, offering h2
* ALPN, offering http/1.1
* Cipher selection: ALL:!EXPORT:!EXPORT40:!EXPORT56:!aNULL:!LOW:!RC4:@STRENGTH
* successfully set certificate verify locations:
*   CAfile: /etc/ssl/cert.pem
  CApath: none
* TLSv1.2 (OUT), TLS handshake, Client hello (1):
* TLSv1.2 (IN), TLS handshake, Server hello (2):
* TLSv1.2 (IN), TLS handshake, Certificate (11):
* TLSv1.2 (IN), TLS handshake, Server key exchange (12):
* TLSv1.2 (IN), TLS handshake, Server finished (14):
* TLSv1.2 (OUT), TLS handshake, Client key exchange (16):
* TLSv1.2 (OUT), TLS change cipher, Client hello (1):
* TLSv1.2 (OUT), TLS handshake, Finished (20):
* TLSv1.2 (IN), TLS change cipher, Client hello (1):
* TLSv1.2 (IN), TLS handshake, Finished (20):
* SSL connection using TLSv1.2 / ECDHE-RSA-AES256-GCM-SHA384
* ALPN, server accepted to use h2
* Server certificate:
*  subject: CN=localhost
*  start date: Mar 31 04:06:30 2020 GMT
*  expire date: Mar 29 04:06:30 2030 GMT
*  issuer: CN=localhost
*  SSL certificate verify result: self signed certificate (18), continuing anyway.
* Using HTTP2, server supports multi-use
* Connection state changed (HTTP/2 confirmed)
* Copying HTTP/2 data in stream buffer to connection buffer after upgrade: len=0
* Server auth using Basic with user 'client'
* Using Stream ID: 1 (easy handle 0x7fc09380ec00)
> POST /auth/realms/ebanking/protocol/openid-connect/token HTTP/2
> Host: keycloak-http.default.svc.cluster.local:8443
> Authorization: Basic Y2xpZW50OmViYW5raW5nY2xpZW50
> User-Agent: curl/7.54.0
> Accept: application/json
> Content-Length: 143
> Content-Type: application/x-www-form-urlencoded
> 
* We are completely uploaded and fine
* Connection state changed (MAX_CONCURRENT_STREAMS updated)!
< HTTP/2 200 
< cache-control: no-store
< set-cookie: KEYCLOAK_LOCALE=; Version=1; Comment=Expiring cookie; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Max-Age=0; Path=/auth/realms/ebanking/; HttpOnly
< set-cookie: KC_RESTART=; Version=1; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Max-Age=0; Path=/auth/realms/ebanking/; HttpOnly
< pragma: no-cache
< content-type: application/json
< content-length: 2604
< date: Tue, 31 Mar 2020 09:13:15 GMT
< 
* Connection #0 to host keycloak-http.default.svc.cluster.local left intact
{"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDRk1Gc3JreDhRRjF6bm5RM2JzSHo1VUxGeE9HNkxKbkJsWGV4c09pb1ZjIn0.eyJqdGkiOiJmOTk1ZDhlYi0xYjU1LTQ1ZWYtYTVkMy1hNjY4ZjE0YzE3NzAiLCJleHAiOjE1ODU2NDYyOTUsIm5iZiI6MCwiaWF0IjoxNTg1NjQ1OTk1LCJpc3MiOiJodHRwczovL2tleWNsb2FrLWh0dHAuZGVmYXVsdC5zdmMuY2x1c3Rlci5sb2NhbDo4NDQzL2F1dGgvcmVhbG1zL2ViYW5raW5nIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImY0NTY2MTUzLTM5NDEtNGYzNC04MWNlLWIxMTcwYTc1NzlhZSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImViYW5raW5nY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiN2U2NWY3NTktM2E4Ny00YWVlLWE3ODktZjBiMGNjYWYwN2YwIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjE4MDgwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJST0xFX1VTRVIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiRG9uYWxkIER1Y2siLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJkb25hbGQuZHVjayIsImdpdmVuX25hbWUiOiJEb25hbGQiLCJmYW1pbHlfbmFtZSI6IkR1Y2siLCJlbWFpbCI6ImRvbmFsZC5kdWNrQGNvbXBhbnkuY29tIn0.EF4KxcCvOqje4NFfE4-Zqz4QV4QmzwG_bQF2iotkRZgp-m1F1LMVq_ZyFVMM7okAmmMpeZAdFMykRMf4afPNH5vuF3Oxih7CW6_7oYMjf6mjqMskA0Yi_qALf7Bdg8KIDnAVyHvKHTYQWLRagg-W0lxo8Pf7FL0SSyo9Xfaxybp1rQMP4Pc7TIhrQdHourV485xASxGcHsPeNM92cbW7SoSPEPdw_AQ7U7M2T0fklFUIXNpfRBHyAQYrmsdjzd-MiAUzmu10l3jwxTcnKJDMF-xtd4BgBQLLzJC9ulDF74jrvVHCunZJ22R6_1R1fe4WwVAcFcEvAdko7U--YUvwJg","expires_in":300,"refresh_expires_in":1800,"refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4ZjQzZmYxZS1lNTkwLTRlMTUtOTZhYi0yZTJkZTQ1ZjM3OWIifQ.eyJqdGkiOiJkYTI4ZjRkZS01ZDI3LTQ2ZTUtYmE5MS0wNjNhZWFhNWQxYmEiLCJleHAiOjE1ODU2NDc3OTUsIm5iZiI6MCwiaWF0IjoxNTg1NjQ1OTk1LCJpc3MiOiJodHRwczovL2tleWNsb2FrLWh0dHAuZGVmYXVsdC5zdmMuY2x1c3Rlci5sb2NhbDo4NDQzL2F1dGgvcmVhbG1zL2ViYW5raW5nIiwiYXVkIjoiaHR0cHM6Ly9rZXljbG9hay1odHRwLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWw6ODQ0My9hdXRoL3JlYWxtcy9lYmFua2luZyIsInN1YiI6ImY0NTY2MTUzLTM5NDEtNGYzNC04MWNlLWIxMTcwYTc1NzlhZSIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJlYmFua2luZ2NsaWVudCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjdlNjVmNzU5LTNhODctNGFlZS1hNzg5LWYwYjBjY2FmMDdmMCIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJST0xFX1VTRVIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIn0.S2a2GL1kx5kvEfk4xP1a1vNUZFhLItXInMAj0pQHfpY","token_type":"bearer","not-before-policy":0,"session_state":"7e65f759-3a87-4aee-a789-f0b0ccaf07f0","scope":"profile email"}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   To add data, run the command below. Make sure

    -   `<access token>` is replaced with the access token obtained above

    -   You do not forget the single quote in the end of the command below

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
curl -X POST \
  https://localhost/services/banking/api/customers \
  -H 'Authorization: Bearer <access token>' \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: da78b930-e79b-45cc-ad4d-0b6930b4c825' \
  -H 'cache-control: no-cache' \
  -d '{  
  "cifNumber" : "ABCD123456789",  
  "firstName" : "Kiana",  
  "lastName" : "Bosco",  
  "icNumber" : "111111-11-1111",  
  "phoneNumber" : "+6012345678"  
}
'
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   If all goes well, the command will return you the data created - with id
    populated as per below:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
{"id":1051,"cifNumber":"ABCD123456789","firstName":"Kiana","lastName":"Bosco","icNumber":"111111-11-1111","phoneNumber":"+6012345678"}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

-   To read back data, use the command below. Make sure

    -   `<access token>` is replaced with the access token obtained above

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> curl https://localhost/services/banking/api/customers -H "Authorization: Bearer <access token>"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

-   If all goes well, you should be able to see the data we saved just now:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[{"id":1051,"cifNumber":"ABCD123456789","firstName":"Kiana","lastName":"Bosco","icNumber":"111111-11-1111","phoneNumber":"+6012345678"}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

-   Congratulations! You have developed, tested, deployed and run your
    micro-services successfully. Unfortunately we are still running our services
    without SSL (plain HTTP instead of HTTPS). Next we will see how to secure it
    with self signed certificate and ingress.

 

 

 

Tips and tricks
---------------

 

### Deleting your work in Kubernetes

-   If you have made a mistake and accidentally uploaded the wrong container to
    Kubernetes, you can simply delete that deployment

-   Note: You do not just delete a pod. A pod is ephemera and will be restarted
    by the system. You need to delete a deployment instead

-   To get all deployments in the “default” namespace

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl get deployments
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   To delete

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl delete deployment <deployment name>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

 

### Problem connecting to the database

-   In a real production environment, we would recommend database to be set up
    outside of the cluster. In a cloud environment, it is better to use cloud
    based database services such as RDS

-   This is because, there are times when connectivity between services to
    database is problematic. One way to get over this is to delete the
    persistence volume of the database. Use the command below to see all
    persistence volume in the “default” namespace

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl get persistentVolumeClaim
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   Use the command below to delete. Make sure deployment is deleted first

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl delete persistentVolumeClaim <persistence volume name>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   You can then recreate the persistence volume by going to
    \$PROJECTS/ebanking/k8s and running

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> bash kubectl-apply.sh -f
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

 

### My Ingress not working

-   If you have created your Ingress before you setup your Ingress controller,
    your Ingress will not work

-   Delete the Ingress and recreate it

 

 

### Uninstall using Helm

-   If you have installed something using Helm, you can also use Helm to
    uninstall

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> helm uninstall <the name of chart installed>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

 

### Troubleshoot my URL transformation

-   Sometimes you need to see how your URL or request transformation happens.

-   You can install an echo service that will return you the exact thing you
    send to
    it<https://github.com/kubernetes/ingress-nginx/blob/master/docs/examples/customization/external-auth-headers/deploy/echo-service.yaml>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
> kubectl apply -f https://github.com/kubernetes/ingress-nginx/blob/master/docs/examples/customization/external-auth-headers/deploy/echo-service.yaml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-   To integrate it to your ingress, just add another path in my-ingress.yaml
    file. Delete the old ingress and apply the new one:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   - path: /echo
          backend:
            serviceName: echo-service 
            servicePort: 80
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
