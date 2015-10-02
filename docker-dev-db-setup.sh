docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q) && docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres \
-v `pwd`/test/resources/Initialize-test-db.sql:/docker-entrypoint-initdb.d/Initialize-test-db.sql  \
-v `pwd`/conf/Initialize.sql:/docker-entrypoint-initdb.d/Initialize.sql  --name db postgres:9.4.4