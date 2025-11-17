.PHONY: up down logs test build topics

build:
	mvn -q -T1C -f platform-infra/pom.xml clean package -DskipTests

up: build
	docker compose up -d --build

down:
	docker compose down -v

logs:
	docker compose logs -f --tail=200

test:
	mvn -q -T1C test

topics:
	docker compose exec kafka bash /init/kafka/create-topics.sh