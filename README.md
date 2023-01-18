# Reminder App

- Application serves REST API using java servlet (javax.servlet-api v3.0.1)
- Servlet served using Apache Tomcat v9.0
- [Click here](https://documenter.getpostman.com/view/25211656/2s8ZDVa3v5) for API documentation

### Installation

```bash
# create dir
mkdir reminder
cd reminder 

# clone into repository
git clone https://github.com/abhishek-bsr/reminder-app.git

# build using maven
mvn clean package

# copy target to /apache-tomcat-${version}/bin/webapps 
# inside /apache-tomcat-${version}/bin
# run command
./startup.sh

# to end, run command
./shutdown.sh
```
