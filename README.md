# HighLoad

Веб-сервер для отдачи статики на java с использованием пула потоков.

#### Запуск:

- Собрать образ:  
`sudo docker build -t server .`
 
- Создать контейнер и запустить:  
 `sudo docker run -p 80:80 --rm -v /etc/httpd.conf:/etc/httpd.conf:ro -v /var/www/html:/var/www/html:ro --name server -t server`

#### Нагрузочное тестирование:
Замеры проводились с помощью утилиты ApacheBench.
> This is ApacheBench, Version 2.3 <$Revision: 1807734 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

В рамках тестирования бвыполнялось 10000 запросов к странице httptest/wikipedia_russia.htm,
 одновремнно проводилось 500 запросов. Сервер сконфигурирован так, чтобы обрабатывать 256 потоков
 одновременно. 

`ab -n 10000 -c 500 http://localhost:8081/httptest/wikipedia_russia.html`


|                       |                                                   |
|-----------------------|---------------------------------------------------|
| Server Software:      | Java                                              |
| Server Hostname:      | localhost                                         |
| Server Port:          | 8081                                              |
| Document Path:        | /httptest/wikipedia_russia.html                   |
| Document Length:      | 954824 bytes                                      |
| Concurrency Level:    | 500                                               |
| Time taken for tests: | 38.226 seconds                                    |
| Complete requests:    | 10000                                             |
| Failed requests:      | 0                                                 |
| Total transferred:    | 9549940000 bytes                                  |
| HTML transferred:     | 9548240000 bytes                                  |
| Requests per second:  | 261.60 [#/sec] (mean)                             |
| Time per request:     | 1911.324 [ms] (mean)                              |
| Time per request:     | 3.823 [ms] (mean, across all concurrent requests) |
| Transfer rate:        | 243969.99 [Kbytes/sec] received                   |

Connection Times (ms)


|             |               |      |         |        |      |
|-------------|---------------|------|---------|--------|------|
|             | min           | mean | [+/-sd] | median | max  |
| Connect:    | 0             | 0    | 2.0     | 0      | 13   |
| Processing: | 6 1857 262.5  | 1864 | 2133    | 1864   | 2133 |
| Waiting:    | 5 1854 262.4  | 1860 | 2128    | 1860   | 2128 |
| Total:      | 19 1858 260.7 | 1864 | 2133    | 1864   | 2133 |

Percentage of the requests served within a certain time (ms)

|      |                        |
|------|------------------------|
| 50%  | 1864                   |
| 66%  | 1924                   |
| 75%  | 1978                   |
| 80%  | 2013                   |
| 90%  | 2080                   |
| 95%  | 2105                   |
| 98%  | 2116                   |
| 99%  | 2121                   |
| 100% | 2133 (longest request) |
|      |                        |

Таблицы здесь и далее преобразованы в markdown с помощью чудесного сервиса https://tablesgenerator.com/markdown_tables

#### Сравнение с nginx
Для сравнения аналогичное тестирование производилось с использованием nginx.

Конфигурация nginx `/etc/nginx/nginx.conf` 

>...  
worker_processes 256;  
...  
> events {
    ...  
	 worker_connections 2560;  
>  ...  
}  
>...
 
`ab -n 10000 -c 500 http://localhost:80/httptest/wikipedia_russia.html`

|                       |                                                   |
|-----------------------|---------------------------------------------------|
| Server Software:      | nginx/1.16.1                                      |
| Server Hostname:      | localhost                                         |
| Server Port:          | 80                                                |
| Document Path:        | /httptest/wikipedia_russia.html                   |
| Document Length:      | 954824 bytes                                      |
| Concurrency Level:    | 500                                               |
| Time taken for tests: | 3.552 seconds                                     |
| Complete requests:    | 10000                                             |
| Failed requests:      | 0                                                 |
| Total transferred:    | 9550620000 bytes                                  |
| HTML transferred:     | 9548240000 bytes                                  |
| Requests per second:  | 2815.03 [#/sec] (mean)                            |
| Time per request:     | 177.618 [ms] (mean)                               |
| Time per request:     | 0.355 [ms] (mean, across all concurrent requests) |
| Transfer rate:        | 2625516.55 [Kbytes/sec] received                  |

Connection Times (ms)

|             | min | mean | [+/-sd] | median | max       |
|-------------|-----|------|---------|--------|-----------|
| Connect:    | 2   | 3    | 1.6     | 3      | 14        |
| Processing: | 82  | 173  | 17.6    | 173    | 247       |
| Waiting:    | 1   | 4    | 6.3     | 3      | 54        |
| Total:      | 86  | 176  | 17.5    | 176    | 252 |

Percentage of the requests served within a certain time (ms)

|        |                       |
|--------|-----------------------|
| 66%    | 180                   |
| 75%    | 182                   |
| 80%    | 184                   |
| 90%    | 191                   |
| 95%    | 196                   |
| 98%    | 219                   |
| 99%    | 238                   |
| 100%   | 252 (longest request) |

