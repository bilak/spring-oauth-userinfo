to reproduce:
 - start both applications
 - in command line execute `curl -H 'Authorization: Bearer whatever' localhost:8080/hello` (now you can see hello world)

then remove [this](https://github.com/bilak/spring-oauth-userinfo/blob/master/spring-oauth-userinfo-api/src/main/java/com/github/bilak/poc/SecurityConfiguration.java#L44) 
line and repeat previous steps (you should not be able to see hello world). 