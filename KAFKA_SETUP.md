# Kafka Setup with Docker

## Quick Start

```bash
# Start Kafka, Zookeeper, and Kafka UI
docker-compose -f docker-compose.local.yml up kafka zookeeper kafka-ui -d

# Wait 30 seconds for Kafka to be ready
sleep 30

# Verify Kafka is running
docker ps | grep kafka
```

## Access Kafka UI

Open browser: **http://localhost:8090**

You'll see:
- All Kafka topics
- Messages in each topic
- Consumer groups
- Real-time message flow

## View Specific Topics

### Using Kafka UI (Visual - Easiest)
1. Open http://localhost:8090
2. Click on "Topics"
3. Click on topic name (e.g., "order.created")
4. See all messages

### Using Command Line

```bash
# List all topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# View messages in order.created topic
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order.created \
  --from-beginning

# View with message keys
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order.created \
  --from-beginning \
  --property print.key=true
```

## Test Kafka Events

1. Start services:
   ```bash
   docker-compose -f docker-compose.local.yml up -d
   ```

2. Create an order via API

3. View in Kafka UI:
   - Go to http://localhost:8090
   - Click "Topics" → "order.created"
   - See your order event

## Stop Kafka

```bash
docker-compose -f docker-compose.local.yml down
```

