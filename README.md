URL shortener with redis as backend. Will try to use Redisson, Spring data and Jedis as backend implementation for redis access

### We should be able to:
1. Create new mapping between long and short url
2. Prohibit shortening urls from this service
3. Count visitors for short url, deal with concurrency issues
4. Automatically expire (remove) short urls
5. Count total number of created mapping urls
6. Count total number of visitors for all urls
7. For short urls store information about user agents
8. Store most viewed short urls

## REST API

1. Create new mapping entity. Expire with 0 seconds means do not expire. Check that expire >= 0. 
#### Request
``` 
   POST /create
   {
      "url" : "http://some_long_url",
      "expire" : "seconds from now",
   }
```
#### Response
```
{
   "shortUrl" : "http://localhost:8001/dfgfdger",
   "deleteToken" : "token for removal"
}
```

2. Redirect to original (long) url.
#### Request
```
GET /{shortUrl}
```
#### Response
Redirect to short url


3. Get information about short url (visitors, expiration date, original url, etc)
#### Request
```
GET /info/{shortUrl}
```
#### Response
```
{
  "originalUrl: "http://some_long_url",
  "expire": "expiration date and time",
  "visitors": "count of visitors",
  "created" : "date and time when the short url was created",
  "user_agents" : [
     "Google Chrome",
     "Safari"
  ]
}
```
4. Delete information about mapping (the token for removal)
#### Request
```
DELETE /url?token=token_for_removal
```
#### Response
200 - OK
404 - no such token found
404 - URL is already expired

5. Get dashboard information about the service
#### Request
```
GET /summary
```
#### Response
```
{
  "visitors" : 555,
  "urls" : 99
}
```

## Redis implementation and keys

1. url#short_url
  originalUrl - original "long" url
  expire - when to expire - null if never
  created - when it was created
  visitors - number of visitors - 0 by default
2. url:user_agents#short_url
The set of user agents used to access the given short url
3. total_urls - increment when a new url is created
4. unique_visitors - store unique visitors (by ip) for a url (based on hyperloglog)
5. total_visitors - increment when someone visits a short url
6. most_viewed_urls - sorted set with most viewed urls
7. Count of views for short url per day/week/month - try to use redis time series