# HighLoad

Веб-сервер для отдачи статики на java с использованием пула потоков(ThreadPool)

Собрать образ:
sudo docker build -t server .
Создать контейнер и запустить:
sudo docker run -p 80:80 --rm -v /etc/httpd.conf:/etc/httpd.conf:ro -v /var/www/html:/var/www/html:ro --name server -t server
